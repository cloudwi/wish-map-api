package com.mindbridge.wishmap.domain.restaurant

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "visits")
class Visit(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: Restaurant,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column
    var rating: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "price_range", nullable = false)
    var priceRange: PriceRange = PriceRange.RANGE_10K
) : BaseTimeEntity()
