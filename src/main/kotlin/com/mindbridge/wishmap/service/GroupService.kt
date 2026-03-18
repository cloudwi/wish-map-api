package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.group.Group
import com.mindbridge.wishmap.domain.group.GroupMember
import com.mindbridge.wishmap.domain.group.GroupRole
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.exception.DuplicateResourceException
import com.mindbridge.wishmap.exception.ForbiddenException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.repository.GroupMemberRepository
import com.mindbridge.wishmap.repository.GroupRepository
import com.mindbridge.wishmap.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getMyGroups(userId: Long): List<GroupResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        return groupRepository.findAllByMember(user).map { it.toResponse(userId) }
    }

    @Transactional
    fun createGroup(userId: Long, request: CreateGroupRequest): GroupResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val group = groupRepository.save(Group(name = request.name, leader = user))
        val member = GroupMember(group = group, user = user, role = GroupRole.LEADER)
        groupMemberRepository.save(member)
        group.members.add(member)
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
            throw DuplicateResourceException("이미 그룹에 속한 사용자입니다")
        }
        val member = groupMemberRepository.save(GroupMember(group = group, user = target))
        return GroupMemberResponse(
            id = member.id,
            userId = target.id,
            nickname = target.nickname,
            profileImage = target.profileImage,
            role = member.role,
            joinedAt = member.createdAt.toString()
        )
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
        val oldLeaderMember = groupMemberRepository.findByGroupAndUser(group, group.leader)!!
        oldLeaderMember.role = GroupRole.MEMBER
        newLeaderMember.role = GroupRole.LEADER
        group.leader = newLeader
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

    fun getGroupMemberIds(groupId: Long): List<Long> =
        groupMemberRepository.findUserIdsByGroupId(groupId)

    private fun Group.toResponse(userId: Long) = GroupResponse(
        id = id,
        name = name,
        leaderId = leader.id,
        leaderNickname = leader.nickname,
        memberCount = members.size,
        isLeader = leader.id == userId
    )
}
