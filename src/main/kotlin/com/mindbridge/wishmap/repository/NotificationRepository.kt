package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.notification.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<Notification>
    fun countByUserIdAndIsReadFalse(userId: Long): Long
    fun deleteAllByUserId(userId: Long)
}
