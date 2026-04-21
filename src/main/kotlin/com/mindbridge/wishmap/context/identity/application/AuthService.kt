package com.mindbridge.wishmap.context.identity.application

import com.mindbridge.wishmap.context.identity.api.dto.*
import com.mindbridge.wishmap.context.identity.infrastructure.OAuthService

import com.mindbridge.wishmap.context.identity.domain.AuthProvider
import com.mindbridge.wishmap.context.identity.domain.NicknameGenerator
import com.mindbridge.wishmap.context.identity.domain.SocialAccount
import com.mindbridge.wishmap.context.identity.domain.User
import com.mindbridge.wishmap.exception.DuplicateResourceException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.context.moderation.domain.BlockedUserRepository
import com.mindbridge.wishmap.context.review.domain.CommentRepository
import com.mindbridge.wishmap.context.social.domain.FriendRepository
import com.mindbridge.wishmap.context.social.domain.GroupMemberRepository
import com.mindbridge.wishmap.context.social.domain.GroupRepository
import com.mindbridge.wishmap.context.notification.domain.NotificationRepository
import com.mindbridge.wishmap.context.moderation.domain.ReportRepository
import com.mindbridge.wishmap.context.identity.domain.SocialAccountRepository
import com.mindbridge.wishmap.context.moderation.domain.UserAgreementRepository
import com.mindbridge.wishmap.context.identity.domain.UserRepository
import com.mindbridge.wishmap.context.review.domain.VisitRepository
import com.mindbridge.wishmap.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val socialAccountRepository: SocialAccountRepository,
    private val oAuthService: OAuthService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val notificationRepository: NotificationRepository,
    private val visitRepository: VisitRepository,
    private val commentRepository: CommentRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val groupRepository: GroupRepository,
    private val friendRepository: FriendRepository,
    private val reportRepository: ReportRepository,
    private val blockedUserRepository: BlockedUserRepository,
    private val userAgreementRepository: UserAgreementRepository,
    @Value("\${jwt.access-token-expiration}") private val accessTokenExpiration: Long
) {

    private val log = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun socialLogin(provider: AuthProvider, request: SocialLoginRequest): TokenResponse {
        val oAuthUserInfo = oAuthService.verifyTokenAndGetUserInfo(provider, request.accessToken)

        val socialAccount = socialAccountRepository
            .findByProviderAndProviderId(provider, oAuthUserInfo.providerId)

        val user = if (socialAccount.isPresent) {
            log.info("로그인: provider={}, userId={}", provider, socialAccount.get().user.id)
            socialAccount.get().user
        } else {
            try {
                // 신규 사용자 생성 + 소셜 계정 연결
                val nickname = generateUniqueNickname()
                val newUser = User(
                    nickname = nickname,
                    profileImage = oAuthUserInfo.profileImage
                )
                userRepository.save(newUser)

                val newSocialAccount = SocialAccount(
                    user = newUser,
                    provider = provider,
                    providerId = oAuthUserInfo.providerId,
                    email = oAuthUserInfo.email
                )
                socialAccountRepository.save(newSocialAccount)
                newUser.addSocialAccount(newSocialAccount)

                log.info("회원가입: provider={}, userId={}, nickname={}", provider, newUser.id, newUser.nickname)
                newUser
            } catch (e: DataIntegrityViolationException) {
                // 동시 요청으로 이미 생성된 경우 기존 계정으로 로그인
                socialAccountRepository
                    .findByProviderAndProviderId(provider, oAuthUserInfo.providerId)
                    .orElseThrow { e }
                    .user
            }
        }

        return generateTokenResponse(user)
    }

    @Transactional(readOnly = true)
    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken)
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        return generateTokenResponse(user)
    }

    @Transactional
    fun deleteAccount(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        // Apple 토큰 해지 시도
        user.socialAccounts
            .filter { it.provider == AuthProvider.APPLE && it.refreshToken != null }
            .forEach { account ->
                try {
                    oAuthService.revokeAppleToken(account.refreshToken!!)
                } catch (e: Exception) {
                    log.warn("Apple 토큰 해지 실패 (userId={}): {}", userId, e.message)
                }
            }

        // 알림 삭제
        notificationRepository.deleteAllByUserId(userId)

        // 방문 기록 삭제
        visitRepository.deleteAllByUser(user)

        // 댓글 소프트 삭제
        commentRepository.softDeleteAllByUser(user)

        // 그룹 멤버십 처리 (리더인 그룹은 다음 멤버에게 양도 또는 삭제)
        val memberships = groupMemberRepository.findAllByUserAndStatus(
            user, com.mindbridge.wishmap.context.social.domain.MemberStatus.ACCEPTED
        )
        for (membership in memberships) {
            val group = membership.group
            if (group.leader.id == userId) {
                val otherMembers = groupMemberRepository.countAcceptedMembers(group) - 1
                if (otherMembers > 0) {
                    // 다른 멤버에게 리더 양도
                    val nextLeader = group.members
                        .filter { it.user.id != userId && it.status == com.mindbridge.wishmap.context.social.domain.MemberStatus.ACCEPTED }
                        .firstOrNull()
                    if (nextLeader != null) {
                        group.leader = nextLeader.user
                        nextLeader.role = com.mindbridge.wishmap.context.social.domain.GroupRole.LEADER
                        groupRepository.save(group)
                    }
                } else {
                    // 혼자 남은 그룹 삭제
                    groupRepository.delete(group)
                    continue
                }
            }
        }
        groupMemberRepository.deleteAllByUser(user)

        // 친구 관계 삭제
        friendRepository.deleteAllByUserId(userId)

        // 신고/차단/동의 데이터 삭제
        reportRepository.deleteAllByReporterId(userId)
        blockedUserRepository.deleteAllByBlockerIdOrBlockedId(userId, userId)
        userAgreementRepository.deleteAllByUserId(userId)

        // 사용자 삭제 (socialAccounts는 cascade로 함께 삭제)
        userRepository.delete(user)

        log.info("계정 삭제 완료: userId={}", userId)
    }

    private fun generateTokenResponse(user: User): TokenResponse {
        val accessToken = jwtTokenProvider.generateAccessToken(user.id)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = accessTokenExpiration / 1000,
            user = UserResponse(
                id = user.id,
                nickname = user.nickname,
                profileImage = user.profileImage,
                role = user.role.name
            )
        )
    }

    @Transactional
    fun updateNickname(userId: Long, newNickname: String): UserResponse {
        log.info("닉네임 변경 요청: userId={}, newNickname={}", userId, newNickname)
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        if (user.nickname == newNickname) {
            return UserResponse(id = user.id, nickname = user.nickname, profileImage = user.profileImage, role = user.role.name)
        }

        if (userRepository.existsByNickname(newNickname)) {
            throw DuplicateResourceException("이미 사용 중인 닉네임입니다")
        }

        user.nickname = newNickname
        userRepository.save(user)

        return UserResponse(id = user.id, nickname = user.nickname, profileImage = user.profileImage, role = user.role.name)
    }

    @Transactional
    fun updatePushToken(userId: Long, pushToken: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }
        user.pushToken = pushToken
        userRepository.save(user)
    }

    private fun generateUniqueNickname(): String {
        repeat(10) {
            val nickname = NicknameGenerator.generate()
            if (!userRepository.existsByNickname(nickname)) {
                return nickname
            }
        }
        // 충돌이 계속되면 타임스탬프 추가
        return "${NicknameGenerator.generate()}${System.currentTimeMillis() % 10000}"
    }
}
