package com.mindbridge.wishmap.context.social.application

import com.mindbridge.wishmap.context.notification.application.NotificationService
import com.mindbridge.wishmap.context.notification.domain.NotificationType
import com.mindbridge.wishmap.context.social.api.dto.*
import com.mindbridge.wishmap.context.social.domain.Group
import com.mindbridge.wishmap.context.social.domain.GroupMember
import com.mindbridge.wishmap.context.social.domain.GroupMemberRepository
import com.mindbridge.wishmap.context.social.domain.GroupRepository
import com.mindbridge.wishmap.context.social.domain.GroupRole
import com.mindbridge.wishmap.context.social.domain.MemberStatus
import com.mindbridge.wishmap.exception.DuplicateResourceException
import com.mindbridge.wishmap.exception.ForbiddenException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {

    private val log = org.slf4j.LoggerFactory.getLogger(GroupService::class.java)

    @Transactional(readOnly = true)
    fun getMyGroups(userId: Long): List<GroupResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        return groupRepository.findAllByMemberAndStatus(user, MemberStatus.ACCEPTED)
            .map { it.toResponse(userId) }
    }

    @Transactional
    fun createGroup(userId: Long, request: CreateGroupRequest): GroupResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val group = groupRepository.save(Group(name = request.name, leader = user))
        val member = GroupMember(group = group, user = user, role = GroupRole.LEADER, status = MemberStatus.ACCEPTED)
        groupMemberRepository.save(member)
        group.members.add(member)
        log.info("그룹 생성: userId={}, groupId={}, name={}", userId, group.id, group.name)
        return group.toResponse(userId)
    }

    @Transactional(readOnly = true)
    fun getGroupDetail(userId: Long, groupId: Long): GroupDetailResponse {
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("Group not found") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw ForbiddenException("그룹 구성원만 조회할 수 있습니다")
        }
        return GroupDetailResponse(
            id = group.id,
            name = group.name,
            leaderId = group.leader.id,
            leaderNickname = group.leader.nickname,
            members = group.members.map { m ->
                GroupMemberResponse(
                    id = m.id,
                    userId = m.user.id,
                    nickname = m.user.nickname,
                    profileImage = m.user.profileImage,
                    role = m.role,
                    status = m.status,
                    joinedAt = m.createdAt.toString()
                )
            }
        )
    }

    @Transactional
    fun inviteMember(userId: Long, groupId: Long, nickname: String): GroupMemberResponse {
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("Group not found") }
        val inviter = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        if (!groupMemberRepository.existsByGroupAndUser(group, inviter)) {
            throw ForbiddenException("그룹 구성원만 초대할 수 있습니다")
        }
        val target = userRepository.findByNickname(nickname)
            ?: throw ResourceNotFoundException("'$nickname' 사용자를 찾을 수 없습니다")
        if (groupMemberRepository.existsByGroupAndUser(group, target)) {
            throw DuplicateResourceException("이미 그룹에 속하거나 초대된 사용자입니다")
        }
        val member = groupMemberRepository.save(
            GroupMember(group = group, user = target, status = MemberStatus.PENDING)
        )
        notificationService.createNotification(
            userId = target.id,
            type = NotificationType.GROUP_INVITE,
            title = group.name,
            message = "${group.name} 그룹에 초대되었습니다",
            referenceId = groupId
        )
        return GroupMemberResponse(
            id = member.id,
            userId = target.id,
            nickname = target.nickname,
            profileImage = target.profileImage,
            role = member.role,
            status = member.status,
            joinedAt = member.createdAt.toString()
        )
    }

    @Transactional(readOnly = true)
    fun getPendingInvites(userId: Long): List<GroupInviteResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val invites = groupMemberRepository.findAllByUserAndStatus(user, MemberStatus.PENDING)
        if (invites.isEmpty()) return emptyList()

        val groupIds = invites.map { it.group.id }
        val memberCounts = groupMemberRepository.countAcceptedMembersByGroupIds(groupIds)
            .associate { it.groupId to it.cnt.toInt() }

        return invites.map { m ->
            GroupInviteResponse(
                groupId = m.group.id,
                groupName = m.group.name,
                leaderNickname = m.group.leader.nickname,
                memberCount = memberCounts[m.group.id] ?: 0,
                invitedAt = m.createdAt.toString()
            )
        }
    }

    @Transactional
    fun acceptInvite(userId: Long, groupId: Long) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("Group not found") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val member = groupMemberRepository.findByGroupAndUser(group, user)
            ?: throw ResourceNotFoundException("초대를 찾을 수 없습니다")
        if (member.status != MemberStatus.PENDING) {
            throw IllegalArgumentException("대기 중인 초대가 아닙니다")
        }
        member.status = MemberStatus.ACCEPTED
        log.info("그룹 초대 수락: userId={}, groupId={}", userId, groupId)
    }

    @Transactional
    fun rejectInvite(userId: Long, groupId: Long) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("Group not found") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val member = groupMemberRepository.findByGroupAndUser(group, user)
            ?: throw ResourceNotFoundException("초대를 찾을 수 없습니다")
        if (member.status != MemberStatus.PENDING) {
            throw IllegalArgumentException("대기 중인 초대가 아닙니다")
        }
        groupMemberRepository.delete(member)
    }

    @Transactional
    fun kickMember(userId: Long, groupId: Long, targetUserId: Long) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("Group not found") }
        if (group.leader.id != userId) {
            throw ForbiddenException("그룹장만 추방할 수 있습니다")
        }
        if (targetUserId == userId) {
            throw IllegalArgumentException("자기 자신을 추방할 수 없습니다")
        }
        val target = userRepository.findById(targetUserId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val member = groupMemberRepository.findByGroupAndUser(group, target)
            ?: throw ResourceNotFoundException("그룹 구성원이 아닙니다")
        groupMemberRepository.delete(member)
    }

    @Transactional
    fun transferLeader(userId: Long, groupId: Long, newLeaderId: Long) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("Group not found") }
        if (group.leader.id != userId) {
            throw ForbiddenException("그룹장만 양도할 수 있습니다")
        }
        val newLeader = userRepository.findById(newLeaderId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val newLeaderMember = groupMemberRepository.findByGroupAndUser(group, newLeader)
            ?: throw ResourceNotFoundException("그룹 구성원이 아닙니다")
        if (newLeaderMember.status != MemberStatus.ACCEPTED) {
            throw IllegalArgumentException("수락된 구성원만 그룹장이 될 수 있습니다")
        }
        val oldLeaderMember = groupMemberRepository.findByGroupAndUser(group, group.leader)
            ?: throw ResourceNotFoundException("기존 그룹장의 멤버 정보를 찾을 수 없습니다")
        oldLeaderMember.role = GroupRole.MEMBER
        newLeaderMember.role = GroupRole.LEADER
        group.leader = newLeader
        log.info("그룹장 양도: groupId={}, from={}, to={}", groupId, userId, newLeaderId)
    }

    @Transactional
    fun leaveGroup(userId: Long, groupId: Long) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("Group not found") }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        if (group.leader.id == userId) {
            throw IllegalArgumentException("그룹장은 탈퇴할 수 없습니다. 먼저 양도해주세요.")
        }
        val member = groupMemberRepository.findByGroupAndUser(group, user)
            ?: throw ResourceNotFoundException("그룹 구성원이 아닙니다")
        groupMemberRepository.delete(member)
    }

    @Transactional
    fun updateGroupLocation(userId: Long, groupId: Long, request: UpdateGroupLocationRequest) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("Group not found") }
        if (group.leader.id != userId) {
            throw ForbiddenException("그룹장만 위치를 설정할 수 있습니다")
        }
        group.baseLat = request.baseLat
        group.baseLng = request.baseLng
        group.baseAddress = request.baseAddress
        group.baseRadius = request.baseRadius

        notificationService.notifyGroupMembers(
            groupId = groupId,
            excludeUserId = userId,
            type = NotificationType.GROUP_LOCATION_CHANGED,
            title = group.name,
            message = "그룹 위치가 ${request.baseAddress}(으)로 변경되었습니다"
        )
    }

    fun getGroupMemberIds(groupId: Long): List<Long> =
        groupMemberRepository.findAcceptedUserIdsByGroupId(groupId)

    private fun Group.toResponse(userId: Long) = GroupResponse(
        id = id,
        name = name,
        leaderId = leader.id,
        leaderNickname = leader.nickname,
        memberCount = groupMemberRepository.countAcceptedMembers(this),
        pendingCount = groupMemberRepository.countPendingMembers(this),
        isLeader = leader.id == userId,
        baseLat = baseLat,
        baseLng = baseLng,
        baseAddress = baseAddress,
        baseRadius = baseRadius
    )
}
