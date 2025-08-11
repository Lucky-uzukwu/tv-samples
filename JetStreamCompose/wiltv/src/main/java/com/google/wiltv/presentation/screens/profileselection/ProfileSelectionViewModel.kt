// ABOUTME: ViewModel for profile selection screen managing profile state and navigation
// ABOUTME: Handles profile loading, selection, and integration with UserStateHolder
package com.google.wiltv.presentation.screens.profileselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.wiltv.data.entities.Profile
import com.google.wiltv.data.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileSelectionUiState {
    object Loading : ProfileSelectionUiState()
    object Error : ProfileSelectionUiState()
    data class Ready(
        val profiles: List<Profile>,
        val selectedProfile: Profile? = null
    ) : ProfileSelectionUiState()
}

@HiltViewModel
class ProfileSelectionViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileSelectionUiState>(ProfileSelectionUiState.Loading)
    val uiState: StateFlow<ProfileSelectionUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            try {
                combine(
                    profileRepository.getAllProfiles(),
                    profileRepository.getSelectedProfile()
                ) { profiles, selectedProfile ->
                    ProfileSelectionUiState.Ready(
                        profiles = profiles,
                        selectedProfile = selectedProfile
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = ProfileSelectionUiState.Error
            }
        }
    }

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                profileRepository.selectProfile(profile.id)
            } catch (e: Exception) {
                _uiState.value = ProfileSelectionUiState.Error
            }
        }
    }
}