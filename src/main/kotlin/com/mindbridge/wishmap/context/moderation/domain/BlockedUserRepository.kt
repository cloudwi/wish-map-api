package com.mindbridge.wishmap.context.moderation.domain

import com.mindbridge.wishmap.context.moderation.domain.BlockedUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BlockedUserRepository : JpaRepository<BlockedUser, Long> {
    fun existsByBlockerIdAndBlockedId(blockerId: Long, blockedId: Long): Boolean
    fun findByBlockerIdAndBlockedId(blockerId: Long, blockedId: Long): BlockedUser?
    fun findAllByBlockerId(blockerId: Long): List<BlockedUser>

    @Query("SELECT b.blocked.id FROM BlockedUser b WHERE b.blocker.id = :blockerId")
    fun findBlockedUserIds(blockerId: Long): List<Long>

    fun deleteAllByBlockerIdOrBlockedId(blockerId: Long, blockedId: Long)
}
