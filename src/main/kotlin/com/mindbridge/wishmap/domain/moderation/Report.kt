package com.mindbridge.wishmap.domain.moderation

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "reports",
    uniqueConstraints = [UniqueConstraint(columnNames = ["reporter_id", "target_type", "target_id"])]
)
class Report(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    val reporter: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "target_type")
    val targetType: ReportTargetType,

    @Column(nullable = false, name = "target_id")
    val targetId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val reason: ReportReason,

    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReportStatus = ReportStatus.PENDING,

    var resolvedAt: LocalDateTime? = null
) : BaseTimeEntity()

enum class ReportTargetType {
    COMMENT, RESTAURANT
}

enum class ReportReason {
    SPAM, INAPPROPRIATE, FALSE_INFO, OTHER
}

enum class ReportStatus {
    PENDING, RESOLVED, DISMISSED
}
