package com.mindbridge.wishmap.repository

import com.mindbridge.wishmap.domain.group.Group
import com.mindbridge.wishmap.domain.group.GroupMember
import com.mindbridge.wishmap.domain.group.MemberStatus
import com.mindbridge.wishmap.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GroupRepository : JpaRepository<Group, Long> {
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user = :user AND m.status = :status ORDER BY g.name")
    fun findAllByMemberAndStatus(user: User, status: MemberStatus): List<Group>
}

interface GroupMemberRepository : JpaRepository<GroupMember, Long> {
    fun findByGroupAndUser(group: Group, user: User): GroupMember?
    fun existsByGroupAndUser(group: Group, user: User): Boolean

    @Query("SELECT m.user.id FROM GroupMember m WHERE m.group.id = :groupId AND m.status = 'ACCEPTED'")
    fun findAcceptedUserIdsByGroupId(groupId: Long): List<Long>

    fun findAllByUserAndStatus(user: User, status: MemberStatus): List<GroupMember>

    @Query("SELECT COUNT(m) FROM GroupMember m WHERE m.group = :group AND m.status = 'ACCEPTED'")
    fun countAcceptedMembers(group: Group): Int

    @Query("SELECT COUNT(m) FROM GroupMember m WHERE m.group = :group AND m.status = 'PENDING'")
    fun countPendingMembers(group: Group): Int

    fun deleteAllByUser(user: User)
}
