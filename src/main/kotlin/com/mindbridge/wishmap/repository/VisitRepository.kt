package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.restaurant.Visit
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface VisitRepository : JpaRepository<Visit, Long> {
    fun existsByRestaurantAndUser(restaurant: Restaurant, user: User): Boolean
    fun existsByRestaurantAndUserAndCreatedAtBetween(
        restaurant: Restaurant,
        user: User,
        start: LocalDateTime,
        end: LocalDateTime
    ): Boolean
    fun countByRestaurant(restaurant: Restaurant): Long

    @Query("SELECT AVG(v.rating) FROM Visit v WHERE v.restaurant = :restaurant AND v.rating IS NOT NULL")
    fun findAvgRatingByRestaurant(restaurant: Restaurant): Double?

    // 주간 방문왕: 해당 주(월~일) 동안 가장 많이 방문한 유저의 닉네임
    @Query("""
        SELECT v.user.nickname 
        FROM Visit v 
        WHERE v.restaurant = :restaurant 
          AND v.createdAt >= :weekStart 
          AND v.createdAt < :weekEnd
        GROUP BY v.user
        ORDER BY COUNT(v) DESC
        LIMIT 1
    """)
    fun findWeeklyChampion(
        restaurant: Restaurant,
        weekStart: LocalDateTime,
        weekEnd: LocalDateTime
    ): String?

    // 모든 식당의 주간 방문왕 (배치 조회용)
    @Query("""
        SELECT v.restaurant.id, v.user.nickname, COUNT(v) as cnt
        FROM Visit v
        WHERE v.createdAt >= :weekStart AND v.createdAt < :weekEnd
        GROUP BY v.restaurant.id, v.user
        ORDER BY v.restaurant.id, cnt DESC
    """)
    fun findAllWeeklyChampions(
        weekStart: LocalDateTime,
        weekEnd: LocalDateTime
    ): List<Array<Any>>
}
