package com.mindbridge.wishmap.context.identity.domain

import com.mindbridge.wishmap.common.time.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "social_accounts",
    uniqueConstraints = [UniqueConstraint(columnNames = ["provider", "provider_id"])]
)
class SocialAccount(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthProvider,

    @Column(nullable = false)
    val providerId: String,

    @Column(nullable = false)
    val email: String,

    @Column(name = "refresh_token")
    var refreshToken: String? = null
) : BaseTimeEntity()
