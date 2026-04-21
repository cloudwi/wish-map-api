package com.mindbridge.wishmap.context.lunchvote.domain

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.context.place.domain.Place
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(
    name = "lunch_vote_candidates",
    uniqueConstraints = [UniqueConstraint(columnNames = ["vote_id", "place_id"])]
)
class LunchVoteCandidate(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    val vote: LunchVote,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    val place: Place,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by", nullable = false)
    val addedBy: User,

    @OneToMany(mappedBy = "candidate", cascade = [CascadeType.ALL], orphanRemoval = true)
    val selections: MutableList<LunchVoteSelection> = mutableListOf()
) : BaseTimeEntity()
