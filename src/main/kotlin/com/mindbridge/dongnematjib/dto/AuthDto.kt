package com.mindbridge.dongnematjib.dto

import jakarta.validation.constraints.NotBlank

data class SocialLoginRequest(
    @field:NotBlank(message = "Access token is required")
    val accessToken: String,
    
    val nickname: String? = null // Apple 첫 로그인 시 이름 전달용
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserResponse
)

data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val profileImage: String?,
    val role: String
)
