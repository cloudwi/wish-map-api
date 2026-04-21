package com.mindbridge.wishmap.context.social.domain

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.context.identity.domain.User
import jakarta.persistence.*

@Entity
@Table(
    name = "friends",
    uniqueConstraints = [UniqueConstraint(columnNames = ["requester_id", "receiver_id"])]
)
class Friend(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    val requester: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FriendStatus = FriendStatus.PENDING
) : BaseTimeEntity()

enum class FriendStatus {
    PENDING, ACCEPTED, REJECTED
}
