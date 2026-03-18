package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.comment.Comment
import com.mindbridge.wishmap.domain.restaurant.Restaurant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByRestaurantAndIsDeletedFalse(restaurant: Restaurant, pageable: Pageable): Page<Comment>
    fun countByRestaurantAndIsDeletedFalse(restaurant: Restaurant): Long
    fun findTop3ByRestaurantAndIsDeletedFalseOrderByCreatedAtDesc(restaurant: Restaurant): List<Comment>
}
