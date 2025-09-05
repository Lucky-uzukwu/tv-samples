// ABOUTME: UI state sealed class for Sports screen
// ABOUTME: Handles Loading, Ready, and Error states for sports content display

package com.google.wiltv.presentation.screens.sports

import androidx.paging.PagingData
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.data.entities.SportType
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.presentation.UiText
import kotlinx.coroutines.flow.StateFlow

sealed class SportsScreenUiState {
    data object Loading : SportsScreenUiState()

    data class Ready(
        val sportTypeToGames: Map<SportType, StateFlow<PagingData<CompetitionGame>>>,
        val sportTypes: List<SportType>,
    ) : SportsScreenUiState()

    data class Error(val message: UiText) : SportsScreenUiState()
}