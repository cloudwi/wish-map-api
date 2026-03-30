package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.comment.Comment
import com.mindbridge.wishmap.domain.comment.CommentImage
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.exception.ForbiddenException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.repository.BlockedUserRepository
import com.mindbridge.wishmap.repository.CommentRepository
import com.mindbridge.wishmap.repository.RestaurantRepository
import com.mindbridge.wishmap.repository.UserRepository
import com.mindbridge.wishmap.repository.VisitRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
    private val visitRepository: VisitRepository,
    private val blockedUserRepository: BlockedUserRepository
) {

    @Transactional(readOnly = true)
    fun getComments(restaurantId: Long, currentUserId: Long?, pageable: Pageable): Page<CommentResponse> {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }

        val page = commentRepository.findByRestaurantAndIsDeletedFalse(restaurant, pageable)

        // 차단한 유저의 댓글 필터링
        val blockedIds = if (currentUserId != null) {
            blockedUserRepository.findBlockedUserIds(currentUserId).toSet()
        } else emptySet()

        val filtered = page.content.filter { it.user.id !in blockedIds }

        // 유저별 방문 횟수 배치 조회
        val userIds = filtered.map { it.user }.distinct()
        val visitCountMap = userIds.associate { user ->
            user.id to visitRepository.countByRestaurantAndUser(restaurant, user)
        }

        val responses = filtered.map { it.toResponse(currentUserId, visitCountMap[it.user.id] ?: 0) }
        return org.springframework.data.domain.PageImpl(responses, pageable, page.totalElements)
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

        request.imageUrls.forEachIndexed { index, url ->
            comment.images.add(CommentImage(comment = comment, imageUrl = url, displayOrder = index))
        }

        val saved = commentRepository.save(comment)
        val visitCount = visitRepository.countByRestaurantAndUser(restaurant, user)
        return saved.toResponse(userId, visitCount)
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
