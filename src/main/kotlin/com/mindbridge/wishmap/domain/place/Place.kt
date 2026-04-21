package com.mindbridge.wishmap.domain.place

import com.mindbridge.wishmap.context.review.domain.Visit
import com.mindbridge.wishmap.domain.common.BaseEntity
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "places")
class Place(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    val lat: Double,

    @Column(nullable = false)
    val lng: Double,

    @Column(unique = true)
    val naverPlaceId: String? = null,

    var category: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var thumbnailImage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "price_range")
    var priceRange: PriceRange? = null,

    @Column(name = "place_category_id")
    var placeCategoryId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_by", nullable = false)
    val suggestedBy: User,

    @OneToMany(mappedBy = "place", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    val visits: MutableList<Visit> = mutableListOf()
) : BaseEntity()
