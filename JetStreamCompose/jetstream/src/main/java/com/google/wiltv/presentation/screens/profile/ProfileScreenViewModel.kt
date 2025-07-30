package com.google.wiltv.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.wiltv.data.entities.User
import com.google.wiltv.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    init {
        getUser()
    }

    private val _uiState = MutableStateFlow(ProfileScreenUiState.Ready())
    val uiState: StateFlow<ProfileScreenUiState> = _uiState.asStateFlow()

    fun getUser() {

        viewModelScope.launch {
            ProfileScreenUiState.Loading
            try {
                val user = userRepository.getUser()
                _uiState.update { it.copy(user = user) }
            } catch (e: Exception) {
                ProfileScreenUiState.Error
            }
        }
    }
}

sealed interface ProfileScreenUiState {
    data object Loading : ProfileScreenUiState
    data object Error : ProfileScreenUiState
    data class Ready(
        val user: User? = null,
    ) : ProfileScreenUiState
}
