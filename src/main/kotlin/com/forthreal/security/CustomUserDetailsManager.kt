package com.forthreal.security

import com.forthreal.domain.User
import com.forthreal.mapper.UserMapper
import com.forthreal.security.classes.SecurityUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager

class CustomUserDetailsManager(private val userMapper: UserMapper,
                               private val passwordEncoder: PasswordEncoder): UserDetailsManager {
    override fun loadUserByUsername(username: String): UserDetails?
        = userMapper.findByUsername(username)?.let { SecurityUser(it) }

    override fun createUser(user: UserDetails) {
        val newUser = User(id = null,
            username = user.username,
            password = passwordEncoder.encode(user.password))
        userMapper.addUser(newUser)
    }

    override fun updateUser(user: UserDetails) {
        val newUser = User(id = null,
            username = user.username,
            password = passwordEncoder.encode(user.password))
        userMapper.updatePassword(newUser)
    }

    override fun deleteUser(username: String) {
        userMapper.deleteUser(username)
    }

    override fun changePassword(oldPassword: String, newPassword: String) {
        TODO("Not yet implemented")
    }

    override fun userExists(username: String): Boolean
        = userMapper.findByUsername(username) != null
}