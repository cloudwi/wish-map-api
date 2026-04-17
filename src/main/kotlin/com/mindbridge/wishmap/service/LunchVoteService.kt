package com.mindbridge.wishmap.service

import com.mindbridge.wishmap.domain.lunchvote.LunchVote
import com.mindbridge.wishmap.domain.lunchvote.LunchVoteCandidate
import com.mindbridge.wishmap.domain.lunchvote.LunchVoteSelection
import com.mindbridge.wishmap.domain.lunchvote.LunchVoteStatus
import com.mindbridge.wishmap.domain.notification.NotificationType
import com.mindbridge.wishmap.dto.*
import com.mindbridge.wishmap.exception.BusinessException
import com.mindbridge.wishmap.exception.ForbiddenException
import com.mindbridge.wishmap.exception.ResourceNotFoundException
import com.mindbridge.wishmap.repository.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LunchVoteService(
    private val lunchVoteRepository: LunchVoteRepository,
    private val candidateRepository: LunchVoteCandidateRepository,
    private val selectionRepository: LunchVoteSelectionRepository,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val placeRepository: PlaceRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {

    @Transactional
    fun createVote(userId: Long, groupId: Long, request: CreateLunchVoteRequest): LunchVoteResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다") }
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("그룹을 찾을 수 없습니다") }
        requireAcceptedMember(groupId, userId)

        // 기존 활성 투표가 있으면 마감 확인 후 자동 종료
        val existing = lunchVoteRepository.findByGroupIdAndStatus(groupId, LunchVoteStatus.ACTIVE)
        if (existing != null) {
            if (existing.deadline.isBefore(LocalDateTime.now())) {
                closeVoteInternal(existing)
            } else {
                throw BusinessException("이미 진행 중인 투표가 있습니다")
            }
        }

        val vote = lunchVoteRepository.save(
            LunchVote(
                group = group,
                createdBy = user,
                title = request.title ?: "점심 투표",
                deadline = request.deadline
            )
        )

        // 초기 후보 추가
        request.candidatePlaceIds.forEach { placeId ->
            val place = placeRepository.findById(placeId).orElse(null) ?: return@forEach
            candidateRepository.save(LunchVoteCandidate(vote = vote, place = place, addedBy = user))
        }

        // 그룹 알림
        notificationService.notifyGroupMembers(
            groupId = groupId,
            excludeUserId = userId,
            type = NotificationType.LUNCH_VOTE_CREATED,
            title = group.name,
            message = "${user.nickname}님이 점심 투표를 시작했어요"
        )

        return toResponse(vote, userId)
    }

    @Transactional
    fun getActiveVote(userId: Long, groupId: Long): LunchVoteResponse? {
        requireAcceptedMember(groupId, userId)

        val vote = lunchVoteRepository.findByGroupIdAndStatus(groupId, LunchVoteStatus.ACTIVE) ?: return null

        // 마감 시간 지났으면 자동 종료
        if (vote.deadline.isBefore(LocalDateTime.now())) {
            closeVoteInternal(vote)
        }

        return toResponse(vote, userId)
    }

    @Transactional
    fun addCandidate(userId: Long, groupId: Long, request: AddCandidateRequest): LunchVoteResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다") }
        requireAcceptedMember(groupId, userId)

        val vote = getActiveVoteOrThrow(groupId)
        requireNotExpired(vote)

        if (candidateRepository.existsByVoteIdAndPlaceId(vote.id, request.placeId)) {
            throw BusinessException("이미 추가된 장소입니다")
        }

        val place = placeRepository.findById(request.placeId)
            .orElseThrow { ResourceNotFoundException("장소를 찾을 수 없습니다") }

        candidateRepository.save(LunchVoteCandidate(vote = vote, place = place, addedBy = user))

        return toResponse(vote, userId)
    }

    @Transactional
    fun castVote(userId: Long, groupId: Long, request: CastVoteRequest): LunchVoteResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다") }
        requireAcceptedMember(groupId, userId)

        val vote = getActiveVoteOrThrow(groupId)
        requireNotExpired(vote)

        val candidate = candidateRepository.findById(request.candidateId)
            .orElseThrow { ResourceNotFoundException("후보를 찾을 수 없습니다") }
        if (candidate.vote.id != vote.id) throw BusinessException("해당 투표의 후보가 아닙니다")

        // 기존 투표 삭제 후 새로 투표
        val existing = selectionRepository.findByVoteIdAndUserId(vote.id, userId)
        if (existing != null) {
            selectionRepository.delete(existing)
            selectionRepository.flush()
        }

        selectionRepository.save(LunchVoteSelection(vote = vote, candidate = candidate, user = user))

        return toResponse(vote, userId)
    }

    @Transactional
    fun retractVote(userId: Long, groupId: Long) {
        requireAcceptedMember(groupId, userId)
        val vote = getActiveVoteOrThrow(groupId)
        selectionRepository.deleteByVoteIdAndUserId(vote.id, userId)
    }

    @Transactional
    fun closeVote(userId: Long, groupId: Long): LunchVoteResponse {
        requireAcceptedMember(groupId, userId)
        val vote = getActiveVoteOrThrow(groupId)

        // 생성자 또는 그룹장만 종료 가능
        val group = vote.group
        if (vote.createdBy.id != userId && group.leader.id != userId) {
            throw ForbiddenException("투표 생성자 또는 그룹장만 종료할 수 있습니다")
        }

        closeVoteInternal(vote)
        return toResponse(vote, userId)
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    fun closeExpiredVotes() {
        val expired = lunchVoteRepository.findExpiredActiveVotes(LunchVoteStatus.ACTIVE, LocalDateTime.now())
        expired.forEach { closeVoteInternal(it) }
    }

    // --- Internal helpers ---

    private fun closeVoteInternal(vote: LunchVote) {
        if (vote.status == LunchVoteStatus.CLOSED) return
        vote.status = LunchVoteStatus.CLOSED

        val voteCounts = selectionRepository.countByCandidate(vote.id)
        val winnerId = voteCounts.maxByOrNull { it[1] as Long }?.get(0) as? Long
        val winner = winnerId?.let { candidateRepository.findById(it).orElse(null) }

        val message = winner?.let { "투표 결과: ${it.place.name}" } ?: "투표가 마감되었습니다"

        notificationService.notifyGroupMembers(
            groupId = vote.group.id,
            excludeUserId = 0,
            type = NotificationType.LUNCH_VOTE_CLOSED,
            title = vote.group.name,
            message = message
        )
    }

    private fun getActiveVoteOrThrow(groupId: Long): LunchVote {
        return lunchVoteRepository.findByGroupIdAndStatus(groupId, LunchVoteStatus.ACTIVE)
            ?: throw ResourceNotFoundException("진행 중인 투표가 없습니다")
    }

    private fun requireNotExpired(vote: LunchVote) {
        if (vote.deadline.isBefore(LocalDateTime.now())) {
            throw BusinessException("투표가 마감되었습니다")
        }
    }

    private fun requireAcceptedMember(groupId: Long, userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다") }
        val group = groupRepository.findById(groupId)
            .orElseThrow { ResourceNotFoundException("그룹을 찾을 수 없습니다") }
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw ForbiddenException("그룹 멤버만 접근할 수 있습니다")
        }
    }

    private fun toResponse(vote: LunchVote, currentUserId: Long): LunchVoteResponse {
        val candidates = candidateRepository.findAllByVoteId(vote.id)
        val voteCounts = selectionRepository.countByCandidate(vote.id)
            .associate { (it[0] as Long) to (it[1] as Long).toInt() }
        val voters = selectionRepository.findVotersByVoteId(vote.id)
            .groupBy({ it[0] as Long }, { it[1] as String })
        val mySelection = selectionRepository.findByVoteIdAndUserId(vote.id, currentUserId)

        return LunchVoteResponse(
            id = vote.id,
            groupId = vote.group.id,
            title = vote.title,
            status = vote.status.name,
            deadline = vote.deadline,
            createdBy = UserSummary(
                id = vote.createdBy.id,
                nickname = vote.createdBy.nickname,
                profileImage = vote.createdBy.profileImage
            ),
            candidates = candidates.map { c ->
                LunchVoteCandidateResponse(
                    id = c.id,
                    place = LunchVotePlaceSummary(
                        id = c.place.id,
                        name = c.place.name,
                        category = c.place.category,
                        thumbnailImage = c.place.thumbnailImage,
                        priceRange = c.place.priceRange?.name
                    ),
                    addedBy = c.addedBy.nickname,
                    voteCount = voteCounts[c.id] ?: 0,
                    voters = voters[c.id] ?: emptyList()
                )
            }.sortedByDescending { it.voteCount },
            myVoteCandidateId = mySelection?.candidate?.id,
            totalVotes = voteCounts.values.sum(),
            createdAt = vote.createdAt
        )
    }
}
