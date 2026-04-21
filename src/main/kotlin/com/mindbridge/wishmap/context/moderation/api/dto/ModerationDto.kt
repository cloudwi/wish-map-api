package com.mindbridge.wishmap.context.moderation.api.dto

import com.mindbridge.wishmap.context.moderation.domain.ReportReason
import com.mindbridge.wishmap.context.moderation.domain.ReportTargetType
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CreateReportRequest(
    @field:NotNull val targetType: ReportTargetType,
    @field:NotNull val targetId: Long,
    @field:NotNull val reason: ReportReason,
    val description: String? = null
)

data class ReportResponse(
    val id: Long,
    val targetType: ReportTargetType,
    val targetId: Long,
    val reason: ReportReason,
    val description: String?,
    val createdAt: LocalDateTime
)

data class BlockedUserResponse(
    val id: Long,
    val userId: Long,
    val nickname: String,
    val profileImage: String?,
    val blockedAt: LocalDateTime
)

data class AgreementRequest(
    @field:NotNull val agreementType: String
)

data class AgreementResponse(
    val agreed: Boolean
)
