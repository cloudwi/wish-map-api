package com.mindbridge.wishmap.context.moderation.api

import com.mindbridge.wishmap.context.moderation.api.dto.BlockedUserResponse
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.context.moderation.application.ModerationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class BlockController(
    private val moderationService: ModerationService
) {

    @PostMapping("/users/{userId}/block")
    fun blockUser(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable userId: Long
    ): ResponseEntity<BlockedUserResponse> {
        val response = moderationService.blockUser(user.id, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/users/{userId}/block")
    fun unblockUser(
        @AuthenticationPrincipal user: UserPrincipal,
        @PathVariable userId: Long
    ): ResponseEntity<Void> {
        moderationService.unblockUser(user.id, userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/blocked-users")
    fun getBlockedUsers(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<List<BlockedUserResponse>> {
        return ResponseEntity.ok(moderationService.getBlockedUsers(user.id))
    }
}
