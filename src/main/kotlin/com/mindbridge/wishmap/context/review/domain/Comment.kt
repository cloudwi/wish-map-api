package com.mindbridge.wishmap.context.review.domain

import com.mindbridge.wishmap.domain.common.BaseEntity
import com.mindbridge.wishmap.context.place.domain.Place
import com.mindbridge.wishmap.context.identity.domain.User
import jakarta.persistence.*

@Entity
@Table(name = "comments")
class Comment(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    val place: Place,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    var isDeleted: Boolean = false,

    @OneToMany(mappedBy = "comment", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableList<CommentImage> = mutableListOf(),

    @OneToMany(mappedBy = "comment", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tags: MutableList<CommentTag> = mutableListOf()
) : BaseEntity() {

    fun softDelete() {
        isDeleted = true
        content = "[삭제된 댓글입니다]"
        tags.clear()
    }
}
