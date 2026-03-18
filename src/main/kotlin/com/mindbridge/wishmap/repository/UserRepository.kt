package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByNickname(nickname: String): User?
    fun existsByNickname(nickname: String): Boolean
    fun findByNicknameContainingIgnoreCase(nickname: String): List<User>
    fun findByIdIn(ids: List<Long>): List<User>
}
