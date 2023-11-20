package com.forthreal.configuration

import com.forthreal.mapper.UserMapper
import com.forthreal.security.CustomUserDetailsManager
import org.mybatis.spring.annotation.MapperScan
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@Configuration
@MapperScan(basePackageClasses = [UserMapper::class])
open class AppConfiguration {
    @Bean("passwordEncoder")
    open fun getPasswordEncoder(): PasswordEncoder = object: PasswordEncoder {
        private val logger = LoggerFactory.getLogger(this.javaClass)
        private val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
        override fun encode(rawPassword: CharSequence): String = encoder.encode(rawPassword)

        override fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean {
            val result = encoder.matches(rawPassword, encodedPassword)
            logger.info("Matched $rawPassword to encoded $encodedPassword with result: $result")
            return result
        }
    }

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
            = httpSecurity
        .csrf().disable()
        .cors().disable()
        .userDetailsService(userDetailsService)
        .passwordManagement(Customizer.withDefaults())
        .authorizeHttpRequests {
            it.antMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
        }.formLogin { it.loginPage("/login").permitAll()
        }.rememberMe{ it.userDetailsService(userDetailsService)
        }.build()
}