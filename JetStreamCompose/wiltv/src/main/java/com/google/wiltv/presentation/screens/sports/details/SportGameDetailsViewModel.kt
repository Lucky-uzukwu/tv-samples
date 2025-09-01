// ABOUTME: ViewModel for sport game details screen
// ABOUTME: Manages game data retrieval and UI state for streaming link selection

package com.google.wiltv.presentation.screens.sports.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.data.repositories.SportsRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.net.URLDecoder

sealed interface SportGameDetailsUiState {
    object Loading : SportGameDetailsUiState
    data class Error(val message: UiText) : SportGameDetailsUiState
    data class Done(val game: CompetitionGame) : SportGameDetailsUiState
}

@HiltViewModel
class SportGameDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sportsRepository: SportsRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val gameId: String? = savedStateHandle.get<String?>(SportGameDetailsScreen.GameIdBundleKey)
    
    private val _uiState = MutableStateFlow<SportGameDetailsUiState>(SportGameDetailsUiState.Loading)
    val uiState: StateFlow<SportGameDetailsUiState> = _uiState.asStateFlow()

    init {
        loadGameDetails()
    }

    private fun loadGameDetails() {
        viewModelScope.launch {
            _uiState.value = SportGameDetailsUiState.Loading
            
            // For now, since we're passing the game through navigation,
            // we'll decode it from the gameId parameter which will contain the serialized game
            try {
                val encodedGameData = gameId ?: throw IllegalArgumentException("Game data is required")
                val gameJson = URLDecoder.decode(encodedGameData, "UTF-8")
                val game = Json.decodeFromString<CompetitionGame>(gameJson)
                _uiState.value = SportGameDetailsUiState.Done(game)
            } catch (e: Exception) {
                _uiState.value = SportGameDetailsUiState.Error(
                    UiText.DynamicString("Failed to load game details: ${e.message}")
                )
            }
        }
    }

    fun retryOperation() {
        loadGameDetails()
    }
}