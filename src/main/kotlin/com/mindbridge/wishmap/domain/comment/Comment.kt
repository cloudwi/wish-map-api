package com.mindbridge.wishmap.domain.comment

import com.mindbridge.wishmap.domain.common.BaseEntity
import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "comments")
class Comment(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: Restaurant,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    var isDeleted: Boolean = false
) : BaseEntity() {

    fun softDelete() {
        isDeleted = true
        content = "[삭제된 댓글입니다]"
    }
}
