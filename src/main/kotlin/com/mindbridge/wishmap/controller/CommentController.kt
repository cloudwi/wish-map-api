package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.dto.CommentResponse
import com.mindbridge.wishmap.dto.CreateCommentRequest
import com.mindbridge.wishmap.dto.UpdateCommentRequest
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.service.CommentService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class CommentController(
    private val commentService: CommentService
) {

    @GetMapping("/restaurants/{restaurantId}/comments")
    fun getComments(
        @PathVariable restaurantId: Long,
        @AuthenticationPrincipal user: UserPrincipal?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = org.springframework.data.domain.Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<CommentResponse>> {
        val comments = commentService.getComments(restaurantId, user?.id, pageable)
        return ResponseEntity.ok(comments)
    }

    @PostMapping("/restaurants/{restaurantId}/comments")
    fun createComment(
        @PathVariable restaurantId: Long,
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: CreateCommentRequest
    ): ResponseEntity<CommentResponse> {
        val comment = commentService.createComment(restaurantId, user.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(comment)
    }

    @PatchMapping("/comments/{commentId}")
    fun updateComment(
        @PathVariable commentId: Long,
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: UpdateCommentRequest
    ): ResponseEntity<CommentResponse> {
        val comment = commentService.updateComment(commentId, user.id, request)
        return ResponseEntity.ok(comment)
    }

    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<Void> {
        commentService.deleteComment(commentId, user.id)
        return ResponseEntity.noContent().build()
    }
}
