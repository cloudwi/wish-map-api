package com.mindbridge.wishmap.dto

import com.mindbridge.wishmap.domain.notification.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val referenceId: Long?,
    val createdAt: LocalDateTime
)

data class NotificationCountResponse(
    val count: Long
)
