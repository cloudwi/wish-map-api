package com.mindbridge.wishmap.context.lunchvote.domain

import com.mindbridge.wishmap.context.lunchvote.domain.LunchVote
import com.mindbridge.wishmap.context.lunchvote.domain.LunchVoteCandidate
import com.mindbridge.wishmap.context.lunchvote.domain.LunchVoteSelection
import com.mindbridge.wishmap.context.lunchvote.domain.LunchVoteStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface LunchVoteRepository : JpaRepository<LunchVote, Long> {
    fun findByGroupIdAndStatus(groupId: Long, status: LunchVoteStatus): LunchVote?

    @Query("SELECT v FROM LunchVote v WHERE v.status = :status AND v.deadline < :now")
    fun findExpiredActiveVotes(status: LunchVoteStatus, now: LocalDateTime): List<LunchVote>
}

interface LunchVoteCandidateRepository : JpaRepository<LunchVoteCandidate, Long> {
    fun findAllByVoteId(voteId: Long): List<LunchVoteCandidate>
    fun existsByVoteIdAndPlaceId(voteId: Long, placeId: Long): Boolean
}

interface LunchVoteSelectionRepository : JpaRepository<LunchVoteSelection, Long> {
    fun findByVoteIdAndUserId(voteId: Long, userId: Long): LunchVoteSelection?
    fun deleteByVoteIdAndUserId(voteId: Long, userId: Long)

    @Query("SELECT s.candidate.id, COUNT(s) FROM LunchVoteSelection s WHERE s.vote.id = :voteId GROUP BY s.candidate.id")
    fun countByCandidate(voteId: Long): List<Array<Any>>

    @Query("SELECT s.candidate.id, s.user.nickname FROM LunchVoteSelection s WHERE s.vote.id = :voteId")
    fun findVotersByVoteId(voteId: Long): List<Array<Any>>
}
