package com.mindbridge.wishmap.domain.lunchvote

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.domain.user.User
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
