package com.mindbridge.wishmap.dto

import com.mindbridge.wishmap.domain.user.AuthProvider

data class OAuthUserInfo(
    val provider: AuthProvider,
    val providerId: String,
    val email: String,
    val nickname: String,
    val profileImage: String?
)
