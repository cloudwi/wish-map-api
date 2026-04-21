package com.mindbridge.wishmap.context.lunchvote.domain

import com.mindbridge.wishmap.common.time.BaseTimeEntity
import com.mindbridge.wishmap.context.identity.domain.User
import jakarta.persistence.*

@Entity
@Table(
    name = "lunch_vote_selections",
    uniqueConstraints = [UniqueConstraint(columnNames = ["vote_id", "user_id"])]
)
class LunchVoteSelection(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    val vote: LunchVote,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    val candidate: LunchVoteCandidate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) : BaseTimeEntity()
