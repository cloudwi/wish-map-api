package com.mindbridge.wishmap.context.place.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "naver_category_mapping")
class NaverCategoryMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "naver_top", unique = true, nullable = false, length = 100)
    val naverTop: String,

    @Column(name = "place_category_id", nullable = false)
    val placeCategoryId: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
