package com.mindbridge.dongnematjib.domain.restaurant

import jakarta.persistence.*

@Entity
@Table(name = "restaurant_images")
class RestaurantImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: Restaurant,

    @Column(nullable = false)
    val imageUrl: String,

    @Column(nullable = false)
    var displayOrder: Int = 0
)
