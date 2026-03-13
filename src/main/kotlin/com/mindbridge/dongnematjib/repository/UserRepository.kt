package com.mindbridge.dongnematjib.repository

import com.mindbridge.dongnematjib.domain.user.AuthProvider
import com.mindbridge.dongnematjib.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}
