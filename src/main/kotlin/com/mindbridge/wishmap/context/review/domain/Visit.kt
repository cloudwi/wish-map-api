package com.mindbridge.wishmap.context.review.domain

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.domain.place.Place
import com.mindbridge.wishmap.domain.place.PriceRange
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "visits")
class Visit(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    val place: Place,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column
    var rating: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "price_range")
    var priceRange: PriceRange? = null
) : BaseTimeEntity()
