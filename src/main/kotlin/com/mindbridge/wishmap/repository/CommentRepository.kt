package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.comment.Comment
import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByRestaurantAndIsDeletedFalse(restaurant: Restaurant, pageable: Pageable): Page<Comment>
    fun countByRestaurantAndIsDeletedFalse(restaurant: Restaurant): Long
    @EntityGraph(attributePaths = ["user", "tags"])
    fun findTop3ByRestaurantAndIsDeletedFalseOrderByCreatedAtDesc(restaurant: Restaurant): List<Comment>

    @EntityGraph(attributePaths = ["user", "tags", "images"])
    fun findWithUserAndTagsByRestaurantAndIsDeletedFalse(restaurant: Restaurant, pageable: Pageable): Page<Comment>

    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.user = :user AND c.isDeleted = false")
    fun softDeleteAllByUser(user: User)
}
