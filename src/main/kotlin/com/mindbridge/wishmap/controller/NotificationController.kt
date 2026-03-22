package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.dto.NotificationCountResponse
import com.mindbridge.wishmap.dto.NotificationResponse
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.service.NotificationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    fun getNotifications(
        @AuthenticationPrincipal user: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<NotificationResponse>> =
        ResponseEntity.ok(notificationService.getNotifications(user.id, pageable))

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
