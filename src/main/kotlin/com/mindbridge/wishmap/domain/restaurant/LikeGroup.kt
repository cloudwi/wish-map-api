package com.mindbridge.wishmap.domain.restaurant

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(
    name = "like_groups",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "name"])]
)
class LikeGroup(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, length = 100)
    var name: String,

    @OneToMany(mappedBy = "likeGroup", cascade = [CascadeType.ALL], orphanRemoval = true)
    val likes: MutableList<Like> = mutableListOf()
) : BaseTimeEntity()
