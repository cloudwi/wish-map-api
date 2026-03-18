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
    val lat: Double,

    @Column(nullable = false)
    val lng: Double,

    @Column(unique = true)
    val naverPlaceId: String? = null,

    var category: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var thumbnailImage: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_by", nullable = false)
    val suggestedBy: User,

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableList<RestaurantImage> = mutableListOf(),

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val likes: MutableList<Like> = mutableListOf(),

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val visits: MutableList<Visit> = mutableListOf()
) : BaseEntity()
