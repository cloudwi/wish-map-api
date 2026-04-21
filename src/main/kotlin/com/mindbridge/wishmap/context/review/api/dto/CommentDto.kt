package com.mindbridge.wishmap.context.review.api.dto

import com.mindbridge.wishmap.context.review.domain.Comment
import com.mindbridge.wishmap.dto.UserSummary
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateCommentRequest(
    @field:Size(max = 1000, message = "댓글은 1000자 이하여야 합니다")
    val content: String = "",

    val tags: List<String> = emptyList(),

    val imageUrls: List<String> = emptyList()
)

data class UpdateCommentRequest(
    @field:Size(max = 1000, message = "댓글은 1000자 이하여야 합니다")
    val content: String = "",

    val tags: List<String> = emptyList()
)

data class CommentResponse(
    val id: Long,
    val content: String,
    val tags: List<String>,
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
    tags = tags.map { it.tag },
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
