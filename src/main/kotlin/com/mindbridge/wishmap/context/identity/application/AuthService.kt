package com.mindbridge.wishmap.context.identity.application

import com.mindbridge.wishmap.common.error.DuplicateResourceException
import com.mindbridge.wishmap.common.error.ResourceNotFoundException
import com.mindbridge.wishmap.context.identity.api.dto.*
import com.mindbridge.wishmap.context.identity.domain.NicknameGenerator
import com.mindbridge.wishmap.context.identity.domain.User
import com.mindbridge.wishmap.context.identity.domain.UserRepository
import com.mindbridge.wishmap.context.identity.domain.PhoneVerificationRepository
import com.mindbridge.wishmap.context.moderation.domain.BlockedUserRepository
import com.mindbridge.wishmap.context.moderation.domain.ReportRepository
import com.mindbridge.wishmap.context.moderation.domain.UserAgreementRepository
import com.mindbridge.wishmap.context.notification.domain.NotificationRepository
import com.mindbridge.wishmap.infrastructure.security.JwtTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val phoneVerifications: PhoneVerificationRepository,
    private val phoneCodes: PhoneCodeService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val notificationRepository: NotificationRepository,
    private val reportRepository: ReportRepository,
    private val blockedUserRepository: BlockedUserRepository,
    private val userAgreementRepository: UserAgreementRepository,
    @Value("\${jwt.access-token-expiration}") private val accessTokenExpiration: Long
) {
    fun requestCode(phone: String) = phoneCodes.request(phone)

    @Transactional
    fun phoneLogin(phoneInput: String, code: String): TokenResponse {
        val phone = phoneCodes.normalize(phoneInput)
        require(phoneCodes.consume(phone, code)) { "인증번호가 올바르지 않거나 만료되었습니다." }
        val user = userRepository.findByPhone(phone) ?: userRepository.save(User(generateUniqueNickname(), phone = phone))
        return generateTokenResponse(user)
    }

    @Transactional(readOnly = true)
    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        require(jwtTokenProvider.validateRefreshToken(request.refreshToken)) { "유효하지 않은 로그인 정보입니다." }
        val userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken)
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        require(user.phone != null) { "휴대폰 인증이 필요합니다." }
        return generateTokenResponse(user)
    }

    @Transactional
    fun deleteAccount(userId: Long) {
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다.") }
        notificationRepository.deleteAllByUserId(userId)
        reportRepository.deleteAllByReporterId(userId)
        blockedUserRepository.deleteAllByBlockerIdOrBlockedId(userId, userId)
        userAgreementRepository.deleteAllByUserId(userId)
        user.phone?.let { phoneVerifications.deleteById(it) }
        userRepository.delete(user)
    }

    private fun generateTokenResponse(user: User) = TokenResponse(
        accessToken = jwtTokenProvider.generateAccessToken(user.id),
        refreshToken = jwtTokenProvider.generateRefreshToken(user.id),
        expiresIn = accessTokenExpiration / 1000,
        user = UserResponse(user.id, user.nickname, user.profileImage, user.role.name)
    )

    @Transactional
    fun updateNickname(userId: Long, newNickname: String): UserResponse {
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다.") }
        if (user.nickname != newNickname) {
            if (userRepository.existsByNickname(newNickname)) throw DuplicateResourceException("이미 사용 중인 닉네임입니다")
            user.nickname = newNickname
        }
        return UserResponse(user.id, user.nickname, user.profileImage, user.role.name)
    }

    @Transactional
    fun updatePushToken(userId: Long, pushToken: String) {
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다.") }
        user.pushToken = pushToken
    }

    private fun generateUniqueNickname(): String {
        repeat(10) {
            val nickname = NicknameGenerator.generate()
            if (!userRepository.existsByNickname(nickname)) return nickname
        }
        return "${NicknameGenerator.generate()}${System.currentTimeMillis() % 10000}"
    }
}
