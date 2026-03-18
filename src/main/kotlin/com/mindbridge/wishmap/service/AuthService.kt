package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.user.AuthProvider
import com.mindbridge.wishmap.domain.user.NicknameGenerator
import com.mindbridge.wishmap.domain.user.SocialAccount
import com.mindbridge.wishmap.domain.user.User
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.repository.SocialAccountRepository
import com.mindbridge.wishmap.repository.UserRepository
import com.mindbridge.wishmap.security.JwtTokenProvider
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
    @Value("\${jwt.access-token-expiration}") private val accessTokenExpiration: Long
) {

    @Transactional
    fun socialLogin(provider: AuthProvider, request: SocialLoginRequest): TokenResponse {
        val oAuthUserInfo = oAuthService.verifyTokenAndGetUserInfo(provider, request.accessToken)

        val socialAccount = socialAccountRepository
            .findByProviderAndProviderId(provider, oAuthUserInfo.providerId)

        val user = if (socialAccount.isPresent) {
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
