package com.mindbridge.wishmap.controller

import com.mindbridge.wishmap.dto.CommentResponse
import com.mindbridge.wishmap.dto.CreateCommentRequest
import com.mindbridge.wishmap.dto.UpdateCommentRequest
import com.mindbridge.wishmap.security.UserPrincipal
import com.mindbridge.wishmap.service.CommentService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
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

    // cursor* 파라미터가 오면 keyset pagination, 아니면 기존 offset 방식(구버전 앱 호환).
    // 첫 페이지는 두 모드 모두 cursor 없이 호출되므로 결과가 동일함.
    @GetMapping("/places/{placeId}/comments")
    fun getComments(
        @PathVariable placeId: Long,
        @AuthenticationPrincipal user: UserPrincipal?,
        @RequestParam(required = false) cursorCreatedAt: java.time.LocalDateTime?,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(defaultValue = "20") size: Int,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = org.springframework.data.domain.Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Slice<CommentResponse>> {
        val comments = if (cursorCreatedAt != null && cursorId != null) {
            commentService.getCommentsByCursor(placeId, user?.id, cursorCreatedAt, cursorId, size)
        } else {
            commentService.getComments(placeId, user?.id, pageable)
        }
        return ResponseEntity.ok(comments)
    }

    @PostMapping("/places/{placeId}/comments")
    fun createComment(
        @PathVariable placeId: Long,
        @AuthenticationPrincipal user: UserPrincipal,
        @Valid @RequestBody request: CreateCommentRequest
    ): ResponseEntity<CommentResponse> {
        val comment = commentService.createComment(placeId, user.id, request)
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
