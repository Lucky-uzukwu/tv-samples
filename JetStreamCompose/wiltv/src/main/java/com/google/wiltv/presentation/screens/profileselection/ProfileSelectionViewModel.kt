// ABOUTME: ViewModel for profile selection screen managing profile state and navigation
// ABOUTME: Handles profile loading, selection, and integration with UserStateHolder
package com.google.wiltv.presentation.screens.profileselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.wiltv.data.entities.Profile
import com.google.wiltv.data.repositories.CatalogRepository
import com.google.wiltv.data.repositories.ProfileRepository
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileSelectionUiState {
    object Loading : ProfileSelectionUiState()
    data class Error(val uiText: UiText) : ProfileSelectionUiState()
    data class Ready(
        val profiles: List<Profile>,
        val selectedProfile: Profile? = null,
        val catalogValidationPassed: Boolean = false
    ) : ProfileSelectionUiState()
}

@HiltViewModel
class ProfileSelectionViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ProfileSelectionUiState>(ProfileSelectionUiState.Loading)
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
                        selectedProfile = selectedProfile,
                        catalogValidationPassed = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = ProfileSelectionUiState.Error(
                    UiText.DynamicString("Failed to load profiles")
                )
            }
        }
    }

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                profileRepository.selectProfile(profile.id)
                validateCatalogAccess()
            } catch (e: Exception) {
                _uiState.value = ProfileSelectionUiState.Error(
                    UiText.DynamicString("Failed to select profile")
                )
            }
        }
    }

    fun validateCatalogAccess() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ProfileSelectionUiState.Ready) return@launch

            try {

                val catalogResult = catalogRepository.getMovieCatalog()

                when (catalogResult) {
                    is ApiResult.Success -> {
                        _uiState.value = currentState.copy(catalogValidationPassed = true)
                    }

                    // TODO : continue from here
                    is ApiResult.Error -> {
                        _uiState.value =
                            ProfileSelectionUiState.Error(catalogResult.error.asUiText(catalogResult.message))
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProfileSelectionUiState.Error(
                    UiText.DynamicString("Failed to validate catalog access")
                )
            }
        }
    }

    fun retryOperation() {
        loadProfiles()
    }
}