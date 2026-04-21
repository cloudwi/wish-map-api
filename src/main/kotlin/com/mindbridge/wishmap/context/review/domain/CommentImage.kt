package com.mindbridge.wishmap.context.review.domain

import jakarta.persistence.*

@Entity
@Table(name = "comment_images")
class CommentImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    val comment: Comment,

    @Column(nullable = false)
    val imageUrl: String,

    @Column(nullable = false)
    var displayOrder: Int = 0
)
