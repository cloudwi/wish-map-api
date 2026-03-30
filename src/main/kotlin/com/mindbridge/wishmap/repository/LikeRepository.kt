package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.restaurant.Like
import com.mindbridge.wishmap.domain.restaurant.LikeGroup
import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LikeRepository : JpaRepository<Like, Long> {
    fun findByLikeGroup(likeGroup: LikeGroup): List<Like>
    fun findByLikeGroup(likeGroup: LikeGroup, pageable: Pageable): Page<Like>
    fun findByLikeGroup_UserAndRestaurant(user: User, restaurant: Restaurant): List<Like>

    @Query("SELECT COUNT(DISTINCT l.likeGroup.user) FROM Like l WHERE l.restaurant = :restaurant")
    fun countDistinctUsersByRestaurant(restaurant: Restaurant): Long

    fun countByRestaurant(restaurant: Restaurant): Long
    fun deleteAllByLikeGroupIn(likeGroups: List<LikeGroup>)
}
