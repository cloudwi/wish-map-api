package com.mindbridge.wishmap.context.identity.api.dto

import com.mindbridge.wishmap.context.identity.domain.AuthProvider

data class OAuthUserInfo(
    val provider: AuthProvider,
    val providerId: String,
    val email: String,
    val nickname: String,
    val profileImage: String?
)
