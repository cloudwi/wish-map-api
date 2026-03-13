package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.user.AuthProvider
import com.mindbridge.wishmap.domain.user.User
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.repository.UserRepository
import com.mindbridge.wishmap.security.JwtTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val oAuthService: OAuthService,
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${jwt.access-token-expiration}") private val accessTokenExpiration: Long
) {

    @Transactional
    fun socialLogin(provider: AuthProvider, request: SocialLoginRequest): TokenResponse {
        val oAuthUserInfo = oAuthService.verifyTokenAndGetUserInfo(provider, request.accessToken)

        val user = userRepository.findByProviderAndProviderId(provider, oAuthUserInfo.providerId)
            .orElseGet {
                // 신규 사용자 생성
                val newUser = User(
                    email = oAuthUserInfo.email,
                    nickname = request.nickname ?: oAuthUserInfo.nickname,
                    profileImage = oAuthUserInfo.profileImage,
                    provider = provider,
                    providerId = oAuthUserInfo.providerId
                )
                userRepository.save(newUser)
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
            expiresIn = accessTokenExpiration / 1000, // seconds
            user = UserResponse(
                id = user.id,
                email = user.email,
                nickname = user.nickname,
                profileImage = user.profileImage,
                role = user.role.name
            )
        )
    }
}
