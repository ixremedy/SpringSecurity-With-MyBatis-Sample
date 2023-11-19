package com.forthreal.domain

open class User(var id: Long? = null,
                @get:JvmName("getusername") var username: String? = null,
                @get:JvmName("getpassword") var password: String? = null)
