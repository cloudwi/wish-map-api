package com.mindbridge.wishmap.dto

import com.mindbridge.wishmap.domain.group.GroupRole
import com.mindbridge.wishmap.domain.group.MemberStatus
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
    val pendingCount: Int,
    val isLeader: Boolean,
    val baseLat: Double? = null,
    val baseLng: Double? = null,
    val baseAddress: String? = null,
    val baseRadius: Int? = null
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
    val status: MemberStatus,
    val joinedAt: String
)

data class InviteMemberRequest(
    val nickname: String
)

data class TransferLeaderRequest(
    val newLeaderId: Long
)

data class UpdateGroupLocationRequest(
    val baseLat: Double,
    val baseLng: Double,
    val baseAddress: String,
    val baseRadius: Int
)

data class GroupInviteResponse(
    val groupId: Long,
    val groupName: String,
    val leaderNickname: String,
    val memberCount: Int,
    val invitedAt: String
)
