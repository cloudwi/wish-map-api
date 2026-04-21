package com.mindbridge.wishmap.context.social.api

import com.mindbridge.wishmap.context.social.api.dto.*

import com.mindbridge.wishmap.infrastructure.security.UserPrincipal
import com.mindbridge.wishmap.context.social.application.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/friends")
class FriendController(
    private val friendService: FriendService
) {

    @GetMapping("/search")
    fun searchUsers(
        @RequestParam q: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<UserSearchResult>> =
        ResponseEntity.ok(friendService.searchUsers(q, principal.id))

    @GetMapping
    fun getFriends(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<FriendResponse>> =
        ResponseEntity.ok(friendService.getFriends(principal.id))

    @GetMapping("/requests")
    fun getPendingRequests(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<FriendResponse>> =
        ResponseEntity.ok(friendService.getPendingRequests(principal.id))

    @PostMapping("/request/{userId}")
    fun sendRequest(
        @PathVariable userId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<FriendResponse> =
        ResponseEntity.ok(friendService.sendRequest(principal.id, userId))

    @PatchMapping("/request/{friendId}/accept")
    fun acceptRequest(
        @PathVariable friendId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<FriendResponse> =
        ResponseEntity.ok(friendService.respondToRequest(friendId, principal.id, accept = true))

    @PatchMapping("/request/{friendId}/reject")
    fun rejectRequest(
        @PathVariable friendId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<FriendResponse> =
        ResponseEntity.ok(friendService.respondToRequest(friendId, principal.id, accept = false))

    @DeleteMapping("/{friendId}")
    fun removeFriend(
        @PathVariable friendId: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Void> {
        friendService.removeFriend(friendId, principal.id)
        return ResponseEntity.noContent().build()
    }
}
