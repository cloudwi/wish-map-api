package com.mindbridge.wishmap.context.place.domain

import com.mindbridge.wishmap.context.review.domain.Visit
import com.mindbridge.wishmap.common.time.BaseEntity
import com.mindbridge.wishmap.context.identity.domain.User
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

    // 제보자가 탈퇴하면 SET NULL 로 익명화 (장소 자체는 동료들이 계속 사용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_by")
    var suggestedBy: User?,

    @OneToMany(mappedBy = "place", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    val visits: MutableList<Visit> = mutableListOf()
) : BaseEntity()
