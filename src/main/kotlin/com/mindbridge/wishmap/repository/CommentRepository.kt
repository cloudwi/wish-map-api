package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.comment.Comment
import com.mindbridge.wishmap.domain.place.Place
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByPlaceAndIsDeletedFalse(place: Place, pageable: Pageable): Page<Comment>
    fun countByPlaceAndIsDeletedFalse(place: Place): Long
    @EntityGraph(attributePaths = ["user", "tags"])
    fun findTop3ByPlaceAndIsDeletedFalseOrderByCreatedAtDesc(place: Place): List<Comment>

    @EntityGraph(attributePaths = ["user"])
    fun findWithUserAndTagsByPlaceAndIsDeletedFalse(place: Place, pageable: Pageable): Page<Comment>

    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.user = :user AND c.isDeleted = false")
    fun softDeleteAllByUser(user: User)
}
