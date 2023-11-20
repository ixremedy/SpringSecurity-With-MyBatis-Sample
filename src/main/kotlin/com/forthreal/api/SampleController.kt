package com.forthreal.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {
    @GetMapping("/login")
    fun getLogin(): String = "login"

    @GetMapping("/public")
    fun public(): String = "public"

    @GetMapping("/protected")
    fun protected(): String = "protected"
}