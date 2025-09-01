package com.google.wiltv.state

import com.google.wiltv.data.entities.User
import com.google.wiltv.data.entities.Profile

data class UserState(
    val user: User? = null,
    val selectedProfile: Profile? = null,
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = true
)