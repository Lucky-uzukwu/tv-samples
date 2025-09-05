// ABOUTME: ViewModel for Sports screen managing game data and sport types
// ABOUTME: Handles hero section games and sport type-specific game rows with paging

package com.google.wiltv.presentation.screens.sports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.data.entities.SportType
import com.google.wiltv.data.paging.pagingsources.sports.SportsHeroSectionPagingSource
import com.google.wiltv.data.paging.pagingsources.sports.SportTypePagingSource
import com.google.wiltv.data.repositories.SportsRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.asUiText
import co.touchlab.kermit.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SportsScreenViewModel @Inject constructor(
    private val sportsRepository: SportsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val heroSectionGames: StateFlow<PagingData<CompetitionGame>> = Pager(
        PagingConfig(pageSize = 20, initialLoadSize = 20)
    ) {
        SportsHeroSectionPagingSource(sportsRepository, userRepository)
    }.flow.cachedIn(viewModelScope).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
    )

    private val _uiState = MutableStateFlow<SportsScreenUiState>(SportsScreenUiState.Loading)
    val uiState: StateFlow<SportsScreenUiState> = _uiState.asStateFlow()

    init {
        fetchSportsData()
    }

    private fun fetchSportsData() {
        viewModelScope.launch {
            try {
                Logger.d { "Starting sports data fetch" }
                val token = userRepository.userToken.firstOrNull()
                if (token.isNullOrEmpty()) {
                    Logger.e { "No user token available" }
                    _uiState.value = SportsScreenUiState.Error(UiText.DynamicString("Unauthorized"))
                    return@launch
                }

                Logger.d { "Fetching sport types with token" }
                val sportTypesResult = sportsRepository.getSportTypes(token)
                val sportTypes = when (sportTypesResult) {
                    is ApiResult.Success -> {
                        Logger.d { "Successfully fetched ${sportTypesResult.data.member.size} sport types" }
                        sportTypesResult.data.member.filter { it.active }
                    }

                    is ApiResult.Error -> {
                        Logger.e { "Failed to fetch sport types: ${sportTypesResult.message}" }
                        _uiState.value = SportsScreenUiState.Error(
                            sportTypesResult.error.asUiText(sportTypesResult.message)
                        )
                        return@launch
                    }
                }

                Logger.d { "Creating paging sources for ${sportTypes.size} active sport types" }
                val sportTypeToGames =
                    sportTypes.sortedBy { it.priority }.associateWith { sportType ->
                        Pager(
                            PagingConfig(pageSize = 20, initialLoadSize = 20)
                        ) {
                            SportTypePagingSource(sportsRepository, userRepository, sportType.id)
                        }.flow.cachedIn(viewModelScope).stateIn(
                            viewModelScope,
                            SharingStarted.WhileSubscribed(5_000),
                            PagingData.empty()
                        )
                    }

                Logger.d { "Sports screen UI state set to Ready" }
                _uiState.value = SportsScreenUiState.Ready(
                    sportTypeToGames = sportTypeToGames,
                    sportTypes = sportTypes
                )

            } catch (e: Exception) {
                Logger.e(e) { "Error fetching sports data" }
                _uiState.value =
                    SportsScreenUiState.Error(UiText.DynamicString(e.message ?: "Unknown error"))
            }
        }
    }

    fun handlePagingError(errorText: UiText) {
        Logger.e { "Paging error in sports screen: $errorText" }
    }

    fun retryOperation() {
        _uiState.value = SportsScreenUiState.Loading
        fetchSportsData()
    }
}