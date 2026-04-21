package com.mindbridge.wishmap.context.place.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "place_categories")
class PlaceCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    val icon: String? = null,

    @Column(nullable = false)
    val priority: Int = 0,

    @Column(name = "has_price_range", nullable = false)
    val hasPriceRange: Boolean = false,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(name = "custom_only", nullable = false)
    val customOnly: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @OrderBy("priority ASC")
    val tags: MutableList<PlaceCategoryTag> = mutableListOf()
)
