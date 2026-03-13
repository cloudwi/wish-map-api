package com.mindbridge.dongnematjib.dto

import com.mindbridge.dongnematjib.domain.user.AuthProvider

data class OAuthUserInfo(
    val provider: AuthProvider,
    val providerId: String,
    val email: String,
    val nickname: String,
    val profileImage: String?
)
