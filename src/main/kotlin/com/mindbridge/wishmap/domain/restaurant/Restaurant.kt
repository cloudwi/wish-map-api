package com.mindbridge.wishmap.domain.restaurant

import com.mindbridge.wishmap.domain.common.BaseEntity
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "restaurants")
class Restaurant(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var address: String,

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
    @Column(nullable = false)
    var status: RestaurantStatus = RestaurantStatus.PENDING,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_by", nullable = false)
    val suggestedBy: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    var approvedBy: User? = null,

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableList<RestaurantImage> = mutableListOf(),

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val likes: MutableList<Like> = mutableListOf(),

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bookmarks: MutableList<Bookmark> = mutableListOf()
) : BaseEntity()

enum class RestaurantStatus {
    PENDING, APPROVED, REJECTED
}
