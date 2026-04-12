package com.mindbridge.wishmap.domain.trendtag

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "trend_tags")
class TrendTag(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val label: String,

    @Column(name = "category_name")
    val categoryName: String? = null,

    val tags: String? = null,

    @Column(name = "price_range")
    val priceRange: String? = null,

    val priority: Int = 0,

    val active: Boolean = true,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
