package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.restaurant.RestaurantStatus
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RestaurantRepository : JpaRepository<Restaurant, Long> {

    @Query("""
        SELECT r FROM Restaurant r
        WHERE r.status = :status
        AND r.lat BETWEEN :minLat AND :maxLat
        AND r.lng BETWEEN :minLng AND :maxLng
    """)
    fun findByStatusAndLocationBounds(
        status: RestaurantStatus,
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        pageable: Pageable
    ): Page<Restaurant>

    @Query("""
        SELECT r.id, COUNT(l) FROM Restaurant r
        LEFT JOIN Like l ON l.restaurant = r
        WHERE r IN :restaurants
        GROUP BY r.id
    """)
    fun countLikesByRestaurants(restaurants: List<Restaurant>): List<Array<Any>>

    fun findByStatus(status: RestaurantStatus, pageable: Pageable): Page<Restaurant>

    fun findBySuggestedBy(user: User, pageable: Pageable): Page<Restaurant>

    fun existsByNaverPlaceId(naverPlaceId: String): Boolean
}
