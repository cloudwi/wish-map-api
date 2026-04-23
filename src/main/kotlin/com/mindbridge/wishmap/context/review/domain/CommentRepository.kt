package com.mindbridge.wishmap.context.review.domain

import com.mindbridge.wishmap.context.review.domain.Comment
import com.mindbridge.wishmap.context.place.domain.Place
import com.mindbridge.wishmap.context.identity.domain.User
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

// 무한 스크롤 기반 댓글 목록이므로 Page 대신 Slice 사용 (COUNT 쿼리 생략).
interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByPlaceAndIsDeletedFalse(place: Place, pageable: Pageable): Slice<Comment>
    fun countByPlaceAndIsDeletedFalse(place: Place): Long
    @EntityGraph(attributePaths = ["user"])
    fun findTop3ByPlaceAndIsDeletedFalseOrderByCreatedAtDesc(place: Place): List<Comment>

    @EntityGraph(attributePaths = ["tags"])
    @Query("SELECT c FROM Comment c WHERE c.id IN :ids")
    fun findAllWithTagsByIdIn(ids: Collection<Long>): List<Comment>

    @EntityGraph(attributePaths = ["user"])
    fun findWithUserAndTagsByPlaceAndIsDeletedFalse(place: Place, pageable: Pageable): Slice<Comment>

    // Keyset pagination: (createdAt, id) 복합 커서로 offset 제거.
    // 커서가 null이면 가장 최근 댓글부터, 아니면 커서보다 오래된 댓글만 반환.
    // (createdAt, id) DESC 정렬 → createdAt 동률은 id로 tiebreak.
    @EntityGraph(attributePaths = ["user"])
    @Query("""
        SELECT c FROM Comment c
        WHERE c.place = :place AND c.isDeleted = false
        AND (:cursorCreatedAt IS NULL
             OR c.createdAt < :cursorCreatedAt
             OR (c.createdAt = :cursorCreatedAt AND c.id < :cursorId))
        ORDER BY c.createdAt DESC, c.id DESC
    """)
    fun findCommentsByCursor(
        place: Place,
        cursorCreatedAt: LocalDateTime?,
        cursorId: Long?,
        pageable: Pageable
    ): Slice<Comment>

    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.user = :user AND c.isDeleted = false")
    fun softDeleteAllByUser(user: User)
}
