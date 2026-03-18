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
        SELECT DISTINCT r FROM Restaurant r
        LEFT JOIN Visit v ON v.restaurant = r
        LEFT JOIN Comment c ON c.restaurant = r AND c.isDeleted = false
        WHERE r.lat BETWEEN :minLat AND :maxLat
        AND r.lng BETWEEN :minLng AND :maxLng
        AND (r.status = 'APPROVED' OR v.id IS NOT NULL OR c.id IS NOT NULL)
    """)
    fun findVisibleByLocationBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        pageable: Pageable
    ): Page<Restaurant>

    @Query("""
        SELECT r.id, COUNT(DISTINCT l.likeGroup.user.id) FROM Restaurant r
        LEFT JOIN Like l ON l.restaurant = r
        WHERE r IN :restaurants
        GROUP BY r.id
    """)
    fun countLikesByRestaurants(restaurants: List<Restaurant>): List<Array<Any>>

    fun findByStatus(status: RestaurantStatus, pageable: Pageable): Page<Restaurant>

    fun findBySuggestedBy(user: User, pageable: Pageable): Page<Restaurant>

    fun existsByNaverPlaceId(naverPlaceId: String): Boolean
    fun findByNaverPlaceId(naverPlaceId: String): Restaurant?

    @Query("""
        SELECT r.id, COUNT(v) FROM Restaurant r
        LEFT JOIN Visit v ON v.restaurant = r
        WHERE r IN :restaurants
        GROUP BY r.id
    """)
    fun countVisitsByRestaurants(restaurants: List<Restaurant>): List<Array<Any>>

    @Query("""
        SELECT DISTINCT r FROM Restaurant r
        LEFT JOIN Visit v ON v.restaurant = r
        WHERE r.lat BETWEEN :minLat AND :maxLat
        AND r.lng BETWEEN :minLng AND :maxLng
        AND (r.suggestedBy.id IN :memberIds OR v.user.id IN :memberIds)
    """)
    fun findByLocationBoundsAndMembers(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double,
        memberIds: List<Long>, pageable: Pageable
    ): Page<Restaurant>
}
