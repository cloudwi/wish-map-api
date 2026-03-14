package com.mindbridge.wishmap.domain.user

import com.mindbridge.wishmap.domain.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["email", "provider"])]
)
class User(
    @Column(nullable = false)
    val email: String,

    @Column(nullable = false)
    var nickname: String,

    var profileImage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthProvider,

    @Column(nullable = false)
    val providerId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.USER
) : BaseTimeEntity()

enum class AuthProvider {
    KAKAO, GOOGLE, NAVER, APPLE
}

enum class UserRole {
    USER, ADMIN
}
