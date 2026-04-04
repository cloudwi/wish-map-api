package com.mindbridge.wishmap.domain.placecategory

import jakarta.persistence.*

@Entity
@Table(name = "place_category_tags")
class PlaceCategoryTag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: PlaceCategory,

    @Column(name = "tag_group", nullable = false)
    val tagGroup: String,

    @Column(nullable = false)
    val tag: String,

    @Column(nullable = false)
    val priority: Int = 0
)
