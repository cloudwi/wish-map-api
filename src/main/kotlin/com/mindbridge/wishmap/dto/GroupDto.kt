package com.mindbridge.wishmap.dto

import com.mindbridge.wishmap.domain.group.GroupRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateGroupRequest(
    @field:NotBlank(message = "그룹 이름은 필수입니다")
    @field:Size(max = 100, message = "그룹 이름은 100자 이하여야 합니다")
    val name: String
)

data class GroupResponse(
    val id: Long,
    val name: String,
    val leaderId: Long,
    val leaderNickname: String,
    val memberCount: Int,
    val isLeader: Boolean
)

data class GroupDetailResponse(
    val id: Long,
    val name: String,
    val leaderId: Long,
    val leaderNickname: String,
    val members: List<GroupMemberResponse>
)

data class GroupMemberResponse(
    val id: Long,
    val userId: Long,
    val nickname: String,
    val profileImage: String?,
    val role: GroupRole,
    val joinedAt: String
)

data class InviteMemberRequest(
    val nickname: String
)

data class TransferLeaderRequest(
    val newLeaderId: Long
)
