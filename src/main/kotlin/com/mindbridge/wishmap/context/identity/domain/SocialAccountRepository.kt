package com.mindbridge.wishmap.context.identity.domain

import com.mindbridge.wishmap.context.identity.domain.AuthProvider
import com.mindbridge.wishmap.context.identity.domain.SocialAccount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SocialAccountRepository : JpaRepository<SocialAccount, Long> {
    fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): Optional<SocialAccount>
}
