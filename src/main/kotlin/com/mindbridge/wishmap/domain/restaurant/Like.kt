package com.mindbridge.wishmap.domain.restaurant

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(
    name = "likes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["like_group_id", "restaurant_id"])]
)
class Like(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: Restaurant,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "like_group_id", nullable = false)
    val likeGroup: LikeGroup
) : BaseTimeEntity()
