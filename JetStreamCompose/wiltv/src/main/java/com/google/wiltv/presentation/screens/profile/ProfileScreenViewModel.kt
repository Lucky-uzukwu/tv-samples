package com.google.wiltv.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.wiltv.data.entities.Profile
import com.google.wiltv.data.entities.User
import com.google.wiltv.data.repositories.ProfileRepository
import com.google.wiltv.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileScreenUiState>(ProfileScreenUiState.Ready())
    val uiState: StateFlow<ProfileScreenUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = ProfileScreenUiState.Loading
            try {
                val user = userRepository.getUser()
                combine(
                    profileRepository.getAllProfiles(),
                    profileRepository.getSelectedProfile()
                ) { profiles, selectedProfile ->
                    ProfileScreenUiState.Ready(
                        user = user,
                        profiles = profiles,
                        selectedProfile = selectedProfile
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = ProfileScreenUiState.Error
            }
        }
    }

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                profileRepository.selectProfile(profile.id)
            } catch (e: Exception) {
                _uiState.value = ProfileScreenUiState.Error
            }
        }
    }
}

sealed interface ProfileScreenUiState {
    data object Loading : ProfileScreenUiState
    data object Error : ProfileScreenUiState
    data class Ready(
        val user: User? = null,
        val profiles: List<Profile> = emptyList(),
        val selectedProfile: Profile? = null,
    ) : ProfileScreenUiState
}
