package com.mindbridge.wishmap.security

import com.mindbridge.wishmap.context.identity.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val id: Long,
    private val nickname: String,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String? = null

    override fun getUsername(): String = nickname

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    companion object {
        fun from(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
            return UserPrincipal(user.id, user.nickname, authorities)
        }
    }
}
