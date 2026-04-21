package com.mindbridge.wishmap.context.identity.domain

import com.mindbridge.wishmap.common.time.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    var nickname: String,

    var profileImage: String? = null,

    @Column(name = "push_token")
    var pushToken: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.USER,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val socialAccounts: MutableList<SocialAccount> = mutableListOf()
) : BaseTimeEntity() {

    fun addSocialAccount(socialAccount: SocialAccount) {
        socialAccounts.add(socialAccount)
    }

    val primaryEmail: String?
        get() = socialAccounts.firstOrNull()?.email
}

enum class AuthProvider {
    KAKAO, GOOGLE, NAVER, APPLE
}

enum class UserRole {
    USER, ADMIN
}
