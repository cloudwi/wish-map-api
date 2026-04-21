package com.mindbridge.wishmap.context.moderation.domain

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.context.identity.domain.User
import jakarta.persistence.*

@Entity
@Table(
    name = "blocked_users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["blocker_id", "blocked_id"])]
)
class BlockedUser(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    val blocker: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    val blocked: User
) : BaseTimeEntity()
