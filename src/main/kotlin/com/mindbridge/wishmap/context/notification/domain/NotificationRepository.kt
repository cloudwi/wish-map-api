package com.mindbridge.wishmap.context.notification.domain

import com.mindbridge.wishmap.context.notification.domain.Notification
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

// 무한 스크롤 기반 알림 목록이므로 Page 대신 Slice 사용 (COUNT 쿼리 생략).
interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Slice<Notification>
    fun countByUserIdAndIsReadFalse(userId: Long): Long
    fun deleteAllByUserId(userId: Long)

    // Keyset pagination. (createdAt, id) 커서로 깊은 스크롤에서도 O(log n).
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId
        AND (:cursorCreatedAt IS NULL
             OR n.createdAt < :cursorCreatedAt
             OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId))
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    fun findByUserIdAndCursor(
        userId: Long,
        cursorCreatedAt: LocalDateTime?,
        cursorId: Long?,
        pageable: Pageable
    ): Slice<Notification>
}
