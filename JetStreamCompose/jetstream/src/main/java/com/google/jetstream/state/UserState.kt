package com.google.jetstream.state

import com.google.jetstream.data.entities.User

data class UserState(
    val isLoggedIn: Boolean = false,
    val user: User? = null
)