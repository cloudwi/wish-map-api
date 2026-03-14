package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByNickname(nickname: String): Boolean
}
