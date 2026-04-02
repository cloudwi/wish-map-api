package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.comment.Comment
import com.mindbridge.wishmap.domain.comment.CommentImage
import com.mindbridge.wishmap.domain.comment.CommentTag
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

        request.tags.forEach { tag ->
            comment.tags.add(CommentTag(comment = comment, tag = tag, category = resolveTagCategory(tag)))
        }

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
        comment.tags.clear()
        request.tags.forEach { tag ->
            comment.tags.add(CommentTag(comment = comment, tag = tag, category = resolveTagCategory(tag)))
        }
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

    companion object {
        private val TAG_CATEGORY_MAP = mapOf(
            "혼밥 성지" to "atmosphere", "회식 추천" to "atmosphere", "데이트" to "atmosphere", "조용한" to "atmosphere", "활기찬" to "atmosphere",
            "매운맛" to "taste", "달콤한" to "taste", "담백한" to "taste", "짜릿한" to "taste", "고소한" to "taste",
            "주차 편해" to "convenience", "대기 없음" to "convenience", "늦게까지" to "convenience", "반려동물 OK" to "convenience",
            "또 갈 집" to "oneLiner", "숨은 맛집" to "oneLiner", "점심 맛집" to "oneLiner", "줄 서는 집" to "oneLiner", "가성비 갑" to "oneLiner", "뷰 맛집" to "oneLiner"
        )

        fun resolveTagCategory(tag: String): String? = TAG_CATEGORY_MAP[tag]
    }
}
