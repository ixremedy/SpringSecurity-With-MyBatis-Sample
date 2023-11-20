package com.forthreal.security.classes

import com.forthreal.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class SecurityUser(id: Long?, username: String, password: String?): User(id,username,password), UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        TODO("Not yet implemented")
    }

    constructor(user: User): this(id = user.id, username = user.username!!, password = user.password)

    override fun getPassword() = super.password
    override fun getUsername() = super.username

    /* without support for locking and expiration */
    override fun isAccountNonExpired(): Boolean = false
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}