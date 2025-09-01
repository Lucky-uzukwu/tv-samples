// ABOUTME: ViewModel for Dashboard screen to manage profile state for navigation filtering
// ABOUTME: Provides selected profile information to determine which tabs should be visible
package com.google.wiltv.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.wiltv.data.entities.Profile
import com.google.wiltv.data.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    val selectedProfile: StateFlow<Profile?> = profileRepository.getSelectedProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}