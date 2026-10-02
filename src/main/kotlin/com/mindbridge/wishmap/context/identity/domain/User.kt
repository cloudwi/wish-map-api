package com.mindbridge.wishmap.context.identity.domain

import com.mindbridge.wishmap.common.time.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    var nickname: String,

    @Column(length = 11, unique = true)
    var phone: String? = null,

    var profileImage: String? = null,

    @Column(name = "push_token")
    var pushToken: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.USER
) : BaseTimeEntity()

enum class UserRole {
    USER, ADMIN
}
