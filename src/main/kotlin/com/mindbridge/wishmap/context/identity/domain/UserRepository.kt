package com.mindbridge.wishmap.context.identity.domain

import com.mindbridge.wishmap.context.identity.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByNickname(nickname: String): User?
    fun existsByNickname(nickname: String): Boolean
    fun findByNicknameContainingIgnoreCase(nickname: String): List<User>
    fun findByIdIn(ids: List<Long>): List<User>
}
