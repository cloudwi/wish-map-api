package com.mindbridge.wishmap.dto

import com.mindbridge.wishmap.domain.comment.Comment
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 필수입니다")
    @field:Size(max = 1000, message = "댓글은 1000자 이하여야 합니다")
    val content: String,

    val imageUrls: List<String> = emptyList()
)

data class UpdateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 필수입니다")
    @field:Size(max = 1000, message = "댓글은 1000자 이하여야 합니다")
    val content: String
)

data class CommentResponse(
    val id: Long,
    val content: String,
    val images: List<String>,
    val user: UserSummary,
    val userVisitCount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val isMine: Boolean
)

fun Comment.toResponse(currentUserId: Long?, userVisitCount: Long = 0) = CommentResponse(
    id = id,
    content = content,
    images = images.sortedBy { it.displayOrder }.map { it.imageUrl },
    user = UserSummary(
        id = user.id,
        nickname = user.nickname,
        profileImage = user.profileImage
    ),
    userVisitCount = userVisitCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isEdited = updatedAt != createdAt,
    isDeleted = isDeleted,
    isMine = currentUserId == user.id
)
