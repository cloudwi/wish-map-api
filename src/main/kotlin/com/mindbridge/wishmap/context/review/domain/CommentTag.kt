package com.mindbridge.wishmap.context.review.domain

import jakarta.persistence.*

@Entity
@Table(name = "comment_tags")
class CommentTag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    val comment: Comment,

    @Column(nullable = false, length = 50)
    val tag: String,

    @Column(length = 30)
    val category: String? = null
)
