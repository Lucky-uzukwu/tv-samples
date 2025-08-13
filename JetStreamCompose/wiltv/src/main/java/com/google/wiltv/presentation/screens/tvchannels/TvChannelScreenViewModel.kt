// ABOUTME: ViewModel for TV channels screen managing hero section and genre-based channel data
// ABOUTME: Handles loading states, pagination, and error handling for TV channels display

package com.google.wiltv.presentation.screens.tvchannels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.paging.pagingsources.tvchannel.TvChannelPagingSources
import com.google.wiltv.data.paging.pagingsources.tvchannel.TvChannelsHeroSectionPagingSource
import com.google.wiltv.data.repositories.GenreRepository
import com.google.wiltv.data.repositories.TvChannelsRepository
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvChannelScreenViewModel @Inject constructor(
    private val tvChannelsRepository: TvChannelsRepository,
    private val userRepository: UserRepository,
    private val genreRepository: GenreRepository,
) : ViewModel() {

    val heroSectionTvChannels: StateFlow<PagingData<TvChannel>> = Pager(
        PagingConfig(pageSize = 5, initialLoadSize = 5)
    ) {
        TvChannelsHeroSectionPagingSource(tvChannelsRepository, userRepository)
    }.flow.cachedIn(viewModelScope).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
    )

    private val _uiState = MutableStateFlow<TvChannelScreenUiState>(TvChannelScreenUiState.Loading)
    val uiState: StateFlow<TvChannelScreenUiState> = _uiState.asStateFlow()


    init {
        loadTvChannelData()
    }

    fun loadTvChannelData() {
        viewModelScope.launch {
            try {
                combine(
                    userRepository.userToken,
                ) { token ->
                    val token = token.firstOrNull() ?: ""
                    when {
                        token.isEmpty() -> TvChannelScreenUiState.Error(UiText.DynamicString("Unauthorized"))
                        else -> {
                            val (genreToTvChannels, genres) = fetchTvChannelsByGenre(
                                genreRepository = genreRepository,
                                userRepository
                            )

                            if (genreToTvChannels.isEmpty()) {
                                return@combine TvChannelScreenUiState.Error(UiText.DynamicString("No data found"))
                            } else {
                                return@combine TvChannelScreenUiState.Ready(
                                    genreToTvChannels = genreToTvChannels,
                                    genres = genres
                                )
                            }
                        }
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = TvChannelScreenUiState.Error(
                    message = UiText.DynamicString(
                        value = "Error fetching tv channels",
                    )
                )
            }
        }
    }

    private suspend fun fetchTvChannelsByGenre(
        genreRepository: GenreRepository,
        userRepository: UserRepository
    ): Pair<Map<Genre, StateFlow<PagingData<TvChannel>>>, List<Genre>> {
        val genresResponse = genreRepository.getTvChannelGenre()
        when (genresResponse) {
            is ApiResult.Success -> {
                val genres = genresResponse.data.member
                val genreToTvChannelPagingData = genres.associateWith { genre ->
                    TvChannelPagingSources().getTvChannelsGenrePagingSource(
                        genreId = genre.id,
                        tvChannelsRepository = tvChannelsRepository,
                        userRepository = userRepository
                    ).cachedIn(viewModelScope).stateIn(
                        viewModelScope,
                        SharingStarted.WhileSubscribed(5_000),
                        PagingData.empty()
                    )
                }
                return Pair(genreToTvChannelPagingData, genres)
            }

            is ApiResult.Error -> {
                _uiState.value = TvChannelScreenUiState.Error(genresResponse.error.asUiText(genresResponse.message))
            }
        }
        return Pair(emptyMap(), emptyList())
    }

    fun retryOperation() {
        loadTvChannelData()
    }
    
    fun handlePagingError(errorMessage: UiText) {
        _uiState.value = TvChannelScreenUiState.Error(errorMessage)
    }

}

sealed interface TvChannelScreenUiState {
    data object Loading : TvChannelScreenUiState
    data class Error(val message: UiText) : TvChannelScreenUiState
    data class Ready(
        val genreToTvChannels: Map<Genre, StateFlow<PagingData<TvChannel>>>,
        val genres: List<Genre>
    ) : TvChannelScreenUiState
}