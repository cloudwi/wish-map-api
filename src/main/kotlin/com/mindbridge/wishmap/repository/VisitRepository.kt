package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.restaurant.Visit
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface VisitRepository : JpaRepository<Visit, Long> {
    fun existsByRestaurantAndUserAndCreatedAtBetween(
        restaurant: Restaurant,
        user: User,
        start: LocalDateTime,
        end: LocalDateTime
    ): Boolean
    fun countByRestaurant(restaurant: Restaurant): Long
    fun countByRestaurantAndUser(restaurant: Restaurant, user: User): Long

    @Query("SELECT AVG(v.rating) FROM Visit v WHERE v.restaurant = :restaurant AND v.rating IS NOT NULL")
    fun findAvgRatingByRestaurant(restaurant: Restaurant): Double?

    // 지정된 식당들의 주간 방문왕 (배치 조회용)
    @Query("""
        SELECT v.restaurant.id, v.user.nickname, COUNT(v) as cnt
        FROM Visit v
        WHERE v.restaurant.id IN :restaurantIds
          AND v.createdAt >= :weekStart AND v.createdAt < :weekEnd
        GROUP BY v.restaurant.id, v.user
        ORDER BY v.restaurant.id, cnt DESC
    """)
    fun findWeeklyChampionsByRestaurantIds(
        restaurantIds: List<Long>,
        weekStart: LocalDateTime,
        weekEnd: LocalDateTime
    ): List<Array<Any>>

    // 특정 식당에서 유저별 방문 횟수 (배치 조회)
    @Query("""
        SELECT v.user.id, COUNT(v)
        FROM Visit v
        WHERE v.restaurant = :restaurant AND v.user IN :users
        GROUP BY v.user.id
    """)
    fun countByRestaurantAndUsers(restaurant: Restaurant, users: List<User>): List<Array<Any>>

    @Query("""
        SELECT v.restaurant.id, MAX(v.createdAt)
        FROM Visit v
        WHERE v.user = :user AND v.restaurant.id IN :restaurantIds
        GROUP BY v.restaurant.id
    """)
    fun findLastVisitDatesByUserAndRestaurantIds(user: User, restaurantIds: List<Long>): List<Array<Any>>

    fun findFirstByRestaurantOrderByCreatedAtDesc(restaurant: Restaurant): Visit?

    fun deleteAllByUser(user: User)

    // 여러 식당의 최다 보고 가격대 (배치 조회)
    @Query("""
        SELECT v.restaurant.id, v.priceRange, COUNT(v) as cnt
        FROM Visit v
        WHERE v.restaurant IN :restaurants AND v.priceRange IS NOT NULL
        GROUP BY v.restaurant.id, v.priceRange
        ORDER BY v.restaurant.id, cnt DESC
    """)
    fun findPriceRangesByRestaurants(restaurants: List<Restaurant>): List<Array<Any>>
}
