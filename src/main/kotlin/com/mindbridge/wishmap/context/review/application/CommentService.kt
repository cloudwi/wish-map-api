package com.mindbridge.wishmap.context.review.application

import com.mindbridge.wishmap.context.moderation.domain.BlockedUserRepository
import com.mindbridge.wishmap.context.review.api.dto.*
import com.mindbridge.wishmap.context.review.domain.Comment
import com.mindbridge.wishmap.context.review.domain.CommentImage
import com.mindbridge.wishmap.context.review.domain.CommentRepository
import com.mindbridge.wishmap.context.review.domain.CommentTag
import com.mindbridge.wishmap.context.review.domain.VisitRepository
import com.mindbridge.wishmap.exception.ForbiddenException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.context.place.domain.PlaceRepository
import com.mindbridge.wishmap.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val placeRepository: PlaceRepository,
    private val userRepository: UserRepository,
    private val visitRepository: VisitRepository,
    private val blockedUserRepository: BlockedUserRepository
) {

    @Transactional(readOnly = true)
    fun getComments(placeId: Long, currentUserId: Long?, pageable: Pageable): Slice<CommentResponse> {
        val place = placeRepository.findById(placeId)
            .orElseThrow { ResourceNotFoundException("Place not found: $placeId") }

        val slice = commentRepository.findWithUserAndTagsByPlaceAndIsDeletedFalse(place, pageable)
        return buildCommentResponse(place, slice, currentUserId, pageable)
    }

    // Keyset(cursor) 기반 댓글 조회. cursor 파라미터가 null이면 최신 댓글부터 조회.
    @Transactional(readOnly = true)
    fun getCommentsByCursor(
        placeId: Long,
        currentUserId: Long?,
        cursorCreatedAt: LocalDateTime?,
        cursorId: Long?,
        size: Int
    ): Slice<CommentResponse> {
        val place = placeRepository.findById(placeId)
            .orElseThrow { ResourceNotFoundException("Place not found: $placeId") }

        val pageable = PageRequest.of(0, size)
        val slice = commentRepository.findCommentsByCursor(place, cursorCreatedAt, cursorId, pageable)
        return buildCommentResponse(place, slice, currentUserId, pageable)
    }

    private fun buildCommentResponse(
        place: com.mindbridge.wishmap.context.place.domain.Place,
        slice: Slice<com.mindbridge.wishmap.context.review.domain.Comment>,
        currentUserId: Long?,
        pageable: Pageable
    ): Slice<CommentResponse> {
        // 차단한 유저의 댓글 필터링
        val blockedIds = if (currentUserId != null) {
            blockedUserRepository.findBlockedUserIds(currentUserId).toSet()
        } else emptySet()

        val filtered = slice.content.filter { it.user.id !in blockedIds }

        // 유저별 방문 횟수 배치 조회 (단일 쿼리)
        val distinctUsers = filtered.map { it.user }.distinct()
        val visitCountMap = if (distinctUsers.isNotEmpty()) {
            visitRepository.countByPlaceAndUsers(place, distinctUsers)
                .associate { row -> (row[0] as Long) to (row[1] as Long) }
        } else emptyMap()

        val responses = filtered.map { it.toResponse(currentUserId, visitCountMap[it.user.id] ?: 0) }
        // hasNext는 원본 slice 기준 (차단 필터링 후 개수가 줄어도 다음 페이지 존재 여부는 동일).
        return SliceImpl(responses, pageable, slice.hasNext())
    }

    @Transactional
    fun createComment(placeId: Long, userId: Long, request: CreateCommentRequest): CommentResponse {
        val place = placeRepository.findById(placeId)
            .orElseThrow { ResourceNotFoundException("Place not found: $placeId") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val comment = Comment(
            place = place,
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
        val visitCount = visitRepository.countByPlaceAndUser(place, user)
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
            // 음식
            "혼밥 성지" to "분위기", "회식 추천" to "분위기", "데이트" to "분위기", "조용한" to "분위기", "활기찬" to "분위기",
            "매운맛" to "맛 특징", "달콤한" to "맛 특징", "담백한" to "맛 특징", "짜릿한" to "맛 특징", "고소한" to "맛 특징",
            "주차 편해" to "편의", "대기 없음" to "편의", "늦게까지" to "편의", "반려동물 OK" to "편의",
            "또 갈 집" to "한줄평", "숨은 맛집" to "한줄평", "점심 맛집" to "한줄평", "가성비 갑" to "한줄평",
            // 카페
            "넓은" to "분위기", "루프탑" to "분위기", "감성적인" to "분위기", "작업하기 좋은" to "분위기",
            "커피 맛집" to "메뉴", "디저트 맛집" to "메뉴", "브런치" to "메뉴", "음료 다양" to "메뉴",
            "또 갈 곳" to "한줄평", "숨은 카페" to "한줄평", "뷰 맛집" to "한줄평",
            // 디저트/간식
            "붕어빵" to "종류", "호떡" to "종류", "타코야끼" to "종류", "와플" to "종류", "마카롱" to "종류",
            "줄 서는 곳" to "특징", "가성비" to "특징", "수제" to "특징", "계절 한정" to "특징",
            // 자연/풍경
            "벚꽃" to "종류", "단풍" to "종류", "야경" to "종류", "일출" to "종류", "공원" to "종류",
            "사진 맛집" to "특징", "산책 코스" to "특징", "드라이브" to "특징", "피크닉" to "특징",
            // 생활편의
            "철물점" to "종류", "세탁소" to "종류", "수선집" to "종류", "열쇠" to "종류",
            "친절한" to "특징", "실력 좋은" to "특징", "빠른" to "특징"
        )

        fun resolveTagCategory(tag: String): String? = TAG_CATEGORY_MAP[tag]
    }
}
