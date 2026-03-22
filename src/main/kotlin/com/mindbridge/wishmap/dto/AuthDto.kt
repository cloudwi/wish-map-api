package com.mindbridge.wishmap.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

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
    val nickname: String,
    val profileImage: String?,
    val role: String
)

data class UpdateNicknameRequest(
    @field:NotBlank(message = "닉네임을 입력해주세요")
    @field:Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다")
    val nickname: String
)

data class UpdatePushTokenRequest(
    val pushToken: String
)
