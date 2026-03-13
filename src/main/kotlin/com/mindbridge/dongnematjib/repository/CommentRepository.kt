package com.mindbridge.dongnematjib.repository

import com.mindbridge.dongnematjib.domain.comment.Comment
import com.mindbridge.dongnematjib.domain.restaurant.Restaurant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByRestaurantAndIsDeletedFalse(restaurant: Restaurant, pageable: Pageable): Page<Comment>
    fun countByRestaurantAndIsDeletedFalse(restaurant: Restaurant): Long
}
