package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.user.AuthProvider
import com.mindbridge.wishmap.domain.user.SocialAccount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SocialAccountRepository : JpaRepository<SocialAccount, Long> {
    fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): Optional<SocialAccount>
}
