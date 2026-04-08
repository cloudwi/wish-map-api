package com.mindbridge.wishmap.domain.notification

import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
class Notification(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: NotificationType,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val message: String,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "reference_id")
    val referenceId: Long? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    GROUP_LOCATION_CHANGED,
    GROUP_INVITE,
    FRIEND_REQUEST,
    LUNCH_VOTE_CREATED,
    LUNCH_VOTE_CLOSED
}
