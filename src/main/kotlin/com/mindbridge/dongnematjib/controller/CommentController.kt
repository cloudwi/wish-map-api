package com.mindbridge.dongnematjib.controller

import com.mindbridge.dongnematjib.dto.CommentResponse
import com.mindbridge.dongnematjib.dto.CreateCommentRequest
import com.mindbridge.dongnematjib.dto.UpdateCommentRequest
import com.mindbridge.dongnematjib.security.UserPrincipal
import com.mindbridge.dongnematjib.service.CommentService
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
        @PageableDefault(size = 20) pageable: Pageable
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
