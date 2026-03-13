package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.restaurant.Like
import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LikeRepository : JpaRepository<Like, Long> {
    fun findByRestaurantAndUser(restaurant: Restaurant, user: User): Optional<Like>
    fun existsByRestaurantAndUser(restaurant: Restaurant, user: User): Boolean
    fun countByRestaurant(restaurant: Restaurant): Long
    fun deleteByRestaurantAndUser(restaurant: Restaurant, user: User)
}
