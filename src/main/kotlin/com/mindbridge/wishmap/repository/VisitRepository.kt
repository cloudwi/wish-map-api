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
}
