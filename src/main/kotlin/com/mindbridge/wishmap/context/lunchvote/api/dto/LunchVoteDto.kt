package com.mindbridge.wishmap.context.lunchvote.api.dto

import com.mindbridge.wishmap.context.place.api.dto.UserSummary
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

// --- Request ---

data class CreateLunchVoteRequest(
    val title: String? = null,
    @field:NotNull(message = "마감 시간은 필수입니다")
    val deadline: LocalDateTime,
    val candidatePlaceIds: List<Long> = emptyList()
)

data class AddCandidateRequest(
    @field:NotNull(message = "맛집 ID는 필수입니다")
    val placeId: Long
)

data class CastVoteRequest(
    @field:NotNull(message = "후보 ID는 필수입니다")
    val candidateId: Long
)

// --- Response ---

data class LunchVoteResponse(
    val id: Long,
    val groupId: Long,
    val title: String,
    val status: String,
    val deadline: LocalDateTime,
    val createdBy: UserSummary,
    val candidates: List<LunchVoteCandidateResponse>,
    val myVoteCandidateId: Long?,
    val totalVotes: Int,
    val createdAt: LocalDateTime
)

data class LunchVoteCandidateResponse(
    val id: Long,
    val place: LunchVotePlaceSummary,
    val addedBy: String,
    val voteCount: Int,
    val voters: List<String>
)

data class LunchVotePlaceSummary(
    val id: Long,
    val name: String,
    val category: String?,
    val thumbnailImage: String?,
    val priceRange: String?
)
