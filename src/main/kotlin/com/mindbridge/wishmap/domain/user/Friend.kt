package com.mindbridge.wishmap.domain.user

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
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
