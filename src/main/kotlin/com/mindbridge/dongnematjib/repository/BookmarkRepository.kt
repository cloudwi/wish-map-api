package com.mindbridge.dongnematjib.repository

import com.mindbridge.dongnematjib.domain.restaurant.Bookmark
import com.mindbridge.dongnematjib.domain.restaurant.Restaurant
import com.mindbridge.dongnematjib.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BookmarkRepository : JpaRepository<Bookmark, Long> {
    fun findByRestaurantAndUser(restaurant: Restaurant, user: User): Optional<Bookmark>
    fun existsByRestaurantAndUser(restaurant: Restaurant, user: User): Boolean
    fun findByUser(user: User, pageable: Pageable): Page<Bookmark>
    fun deleteByRestaurantAndUser(restaurant: Restaurant, user: User)
}
