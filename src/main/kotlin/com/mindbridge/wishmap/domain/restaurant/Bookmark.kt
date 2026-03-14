package com.mindbridge.wishmap.domain.restaurant

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = [UniqueConstraint(columnNames = ["restaurant_id", "user_id"])]
)
class Bookmark(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: Restaurant,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) : BaseTimeEntity()
