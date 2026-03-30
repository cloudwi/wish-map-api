package com.mindbridge.wishmap.domain.moderation

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import com.mindbridge.wishmap.domain.user.User
import jakarta.persistence.*

@Entity
@Table(
    name = "user_agreements",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "agreement_type"])]
)
class UserAgreement(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "agreement_type")
    val agreementType: AgreementType
) : BaseTimeEntity()

enum class AgreementType {
    TERMS_OF_SERVICE
}
