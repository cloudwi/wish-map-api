package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.comment.Comment
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.exception.ForbiddenException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.repository.CommentRepository
import com.mindbridge.wishmap.repository.RestaurantRepository
import com.mindbridge.wishmap.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getComments(restaurantId: Long, currentUserId: Long?, pageable: Pageable): Page<CommentResponse> {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }

        return commentRepository.findByRestaurantAndIsDeletedFalse(restaurant, pageable)
            .map { it.toResponse(currentUserId) }
    }

    @Transactional
    fun createComment(restaurantId: Long, userId: Long, request: CreateCommentRequest): CommentResponse {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val comment = Comment(
            restaurant = restaurant,
            user = user,
            content = request.content
        )

        val saved = commentRepository.save(comment)
        return saved.toResponse(userId)
    }

    @Transactional
    fun updateComment(commentId: Long, userId: Long, request: UpdateCommentRequest): CommentResponse {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { ResourceNotFoundException("Comment not found: $commentId") }

        if (comment.user.id != userId) {
            throw ForbiddenException("본인의 댓글만 수정할 수 있습니다")
        }

        if (comment.isDeleted) {
            throw IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다")
        }

        comment.content = request.content
        return comment.toResponse(userId)
    }

    @Transactional
    fun deleteComment(commentId: Long, userId: Long) {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { ResourceNotFoundException("Comment not found: $commentId") }

        if (comment.user.id != userId) {
            throw ForbiddenException("본인의 댓글만 삭제할 수 있습니다")
        }

        comment.softDelete()
    }
}
