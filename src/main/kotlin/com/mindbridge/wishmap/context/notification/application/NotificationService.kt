package com.mindbridge.wishmap.context.notification.application

import com.mindbridge.wishmap.context.notification.api.dto.NotificationResponse
import com.mindbridge.wishmap.context.notification.domain.Notification
import com.mindbridge.wishmap.context.notification.domain.NotificationRepository
import com.mindbridge.wishmap.context.notification.domain.NotificationType
import com.mindbridge.wishmap.context.notification.infrastructure.PushNotificationService
import com.mindbridge.wishmap.exception.ForbiddenException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.context.social.domain.GroupMemberRepository
import com.mindbridge.wishmap.context.identity.domain.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val pushNotificationService: PushNotificationService
) {

    @Transactional(readOnly = true)
    fun getNotifications(userId: Long, pageable: Pageable): Slice<NotificationResponse> {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { it.toResponse() }
    }

    // Keyset(cursor) 기반 알림 조회.
    @Transactional(readOnly = true)
    fun getNotificationsByCursor(
        userId: Long,
        cursorCreatedAt: LocalDateTime?,
        cursorId: Long?,
        size: Int
    ): Slice<NotificationResponse> {
        return notificationRepository.findByUserIdAndCursor(userId, cursorCreatedAt, cursorId, PageRequest.of(0, size))
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getUnreadCount(userId: Long): Long {
        return notificationRepository.countByUserIdAndIsReadFalse(userId)
    }

    @Transactional
    fun markAsRead(userId: Long, notificationId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { ResourceNotFoundException("알림을 찾을 수 없습니다") }
        if (notification.user.id != userId) {
            throw ForbiddenException("본인의 알림만 읽음 처리할 수 있습니다")
        }
        notification.isRead = true
    }

    @Transactional
    fun markAllAsRead(userId: Long) {
        val pageable = Pageable.unpaged()
        val notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        notifications.forEach { it.isRead = true }
    }

    @Transactional
    fun createNotification(
        userId: Long,
        type: NotificationType,
        title: String,
        message: String,
        referenceId: Long? = null
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        notificationRepository.save(
            Notification(
                user = user,
                type = type,
                title = title,
                message = message,
                referenceId = referenceId
            )
        )
        pushNotificationService.sendPush(userId, title, message, data = referenceId?.let { mapOf("referenceId" to it.toString(), "type" to type.name) })
    }

    @Transactional
    fun notifyGroupMembers(
        groupId: Long,
        excludeUserId: Long,
        type: NotificationType,
        title: String,
        message: String
    ) {
        val memberUserIds = groupMemberRepository.findAcceptedUserIdsByGroupId(groupId)
        val targetUserIds = memberUserIds.filter { it != excludeUserId }
        targetUserIds.forEach { userId ->
            createNotification(userId, type, title, message, referenceId = groupId)
        }
    }

    private fun Notification.toResponse() = NotificationResponse(
        id = id,
        type = type,
        title = title,
        message = message,
        isRead = isRead,
        referenceId = referenceId,
        createdAt = createdAt
    )
}
