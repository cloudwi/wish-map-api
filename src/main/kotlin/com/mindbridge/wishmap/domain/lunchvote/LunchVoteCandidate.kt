package com.mindbridge.wishmap.domain.lunchvote

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.domain.restaurant.Restaurant
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(
    name = "lunch_vote_candidates",
    uniqueConstraints = [UniqueConstraint(columnNames = ["vote_id", "restaurant_id"])]
)
class LunchVoteCandidate(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    val vote: LunchVote,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: Restaurant,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by", nullable = false)
    val addedBy: User,

    @OneToMany(mappedBy = "candidate", cascade = [CascadeType.ALL], orphanRemoval = true)
    val selections: MutableList<LunchVoteSelection> = mutableListOf()
) : BaseTimeEntity()
