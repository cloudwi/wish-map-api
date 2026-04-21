package com.mindbridge.wishmap.context.lunchvote.domain

import com.mindbridge.wishmap.common.time.BaseEntity
import com.mindbridge.wishmap.context.social.domain.Group
import com.mindbridge.wishmap.context.identity.domain.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "lunch_votes")
class LunchVote(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: User,

    @Column(nullable = false, length = 100)
    var title: String = "점심 투표",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: LunchVoteStatus = LunchVoteStatus.ACTIVE,

    @Column(nullable = false)
    val deadline: LocalDateTime,

    @OneToMany(mappedBy = "vote", cascade = [CascadeType.ALL], orphanRemoval = true)
    val candidates: MutableList<LunchVoteCandidate> = mutableListOf()
) : BaseEntity()

enum class LunchVoteStatus { ACTIVE, CLOSED }
