package com.mindbridge.wishmap.context.notification.api

import com.mindbridge.wishmap.context.notification.api.dto.NotificationCountResponse
import com.mindbridge.wishmap.context.notification.api.dto.NotificationResponse
import com.mindbridge.wishmap.infrastructure.security.UserPrincipal
import com.mindbridge.wishmap.context.notification.application.NotificationService
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    // cursor* 파라미터가 오면 keyset pagination, 아니면 기존 offset 방식(구버전 앱 호환).
    @GetMapping
    fun getNotifications(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestParam(required = false) cursorCreatedAt: java.time.LocalDateTime?,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(defaultValue = "20") size: Int,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Slice<NotificationResponse>> {
        val result = if (cursorCreatedAt != null && cursorId != null) {
            notificationService.getNotificationsByCursor(user.id, cursorCreatedAt, cursorId, size)
        } else {
            notificationService.getNotifications(user.id, pageable)
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<NotificationCountResponse> =
        ResponseEntity.ok(NotificationCountResponse(notificationService.getUnreadCount(user.id)))

    @PatchMapping("/{id}/read")
    fun markAsRead(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        notificationService.markAsRead(user.id, id)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/read-all")
    fun markAllAsRead(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<Void> {
        notificationService.markAllAsRead(user.id)
        return ResponseEntity.ok().build()
    }
}
