package com.google.jetstream.state

import androidx.lifecycle.ViewModel
import com.google.jetstream.data.entities.User
import com.google.jetstream.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class UserStateHolder @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    // Expose User state
    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState

    fun updateUser(user: User) {
        // Atomic read-modify-write
        _userState.update { current ->
            current.copy(isLoggedIn = true, user = user)
        }
    }

    fun clearUser() {
        // Atomically reset
        _userState.update { UserState() }
    }
}
