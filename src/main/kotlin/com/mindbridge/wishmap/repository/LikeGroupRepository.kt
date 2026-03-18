package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.restaurant.LikeGroup
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LikeGroupRepository : JpaRepository<LikeGroup, Long> {
    fun findByUser(user: User): List<LikeGroup>
    fun findByUserAndName(user: User, name: String): Optional<LikeGroup>
    fun existsByUserAndName(user: User, name: String): Boolean
}
