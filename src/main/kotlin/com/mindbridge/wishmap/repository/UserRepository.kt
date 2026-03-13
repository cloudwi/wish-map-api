package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.user.AuthProvider
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}
