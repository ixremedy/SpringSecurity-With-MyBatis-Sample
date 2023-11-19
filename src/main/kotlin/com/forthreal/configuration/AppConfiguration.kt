package com.forthreal.configuration

import com.forthreal.mapper.UserMapper
import com.forthreal.security.CustomUserDetailsManager
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@Configuration
@MapperScan(basePackageClasses = [UserMapper::class])
open class AppConfiguration {
    @Bean("passwordEncoder")
    open fun getPasswordEncoder(): PasswordEncoder = Argon2PasswordEncoder()

    @Autowired
    lateinit var userMapper: UserMapper

    @Bean("userDetailsManager")
    @DependsOn(value = ["passwordEncoder","userMapper"])
    open fun getUserDetailsManager(passwordEncoder: PasswordEncoder): UserDetailsManager =
        CustomUserDetailsManager(userMapper, passwordEncoder)

    @Bean(name = ["userDetailsService"])
    open fun getUserDetailsService(userDetailsManager: UserDetailsManager) = UserDetailsService {
            username -> userDetailsManager.loadUserByUsername(username)
    }

    @Bean
    @DependsOn("userDetailsService")
    open fun getSecurityChain(httpSecurity: HttpSecurity, userDetailsService: UserDetailsService): SecurityFilterChain
            = httpSecurity.authorizeHttpRequests { authReq ->
        authReq.antMatchers("/public/**").permitAll()
            .anyRequest().authenticated()
        }.formLogin { formLogin ->
            formLogin.loginPage("/login").permitAll()
        }.rememberMe{ remember ->
            remember.userDetailsService(userDetailsService)
        }.build()
}