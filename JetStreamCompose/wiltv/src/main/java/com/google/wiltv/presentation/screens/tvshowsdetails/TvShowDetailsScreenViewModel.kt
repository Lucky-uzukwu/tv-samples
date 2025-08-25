/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.wiltv.presentation.screens.tvshowsdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.models.Season
import com.google.wiltv.data.paging.pagingsources.tvshow.TvShowPagingSources
import com.google.wiltv.data.repositories.TvShowsRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.data.repositories.WatchlistRepository
import com.google.wiltv.domain.ApiResult
import co.touchlab.kermit.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowDetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tvShowRepository: TvShowsRepository,
    private val userRepository: UserRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {
    
    private val tvShowId: String? = savedStateHandle.get<String?>(TvShowDetailsScreen.TvShowIdBundleKey)
    
    // Watchlist state management
    val isInWatchlist: StateFlow<Boolean> = combine(
        userRepository.userId,
        savedStateHandle.getStateFlow<String?>(TvShowDetailsScreen.TvShowIdBundleKey, null)
    ) { userId, tvShowId ->
        // Use default user ID if not authenticated
        val effectiveUserId = userId ?: "default_user"
        if (tvShowId != null) {
            try {
                watchlistRepository.isInWatchlist(effectiveUserId, tvShowId.toIntOrNull() ?: 0).firstOrNull() ?: false
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )
    
    private val _watchlistLoading = MutableStateFlow(false)
    val watchlistLoading: StateFlow<Boolean> = _watchlistLoading
    
    fun toggleWatchlist() {
        viewModelScope.launch {
            val currentTvShowId = tvShowId?.toIntOrNull()
            
            // Use ensureUserIdExists to safely get/create user ID
            val effectiveUserId = try {
                userRepository.ensureUserIdExists()
            } catch (e: Exception) {
                "default_user" // Fallback if storage fails
            }
            
            if (currentTvShowId != null) {
                try {
                    _watchlistLoading.value = true
                    val isCurrentlyInWatchlist = isInWatchlist.value
                    
                    if (isCurrentlyInWatchlist) {
                        watchlistRepository.removeFromWatchlist(effectiveUserId, currentTvShowId)
                        Logger.d { "✅ TvShowDetailsScreenViewModel: Removed from watchlist - ID: $currentTvShowId" }
                    } else {
                        watchlistRepository.addToWatchlist(effectiveUserId, currentTvShowId, "tvshow")
                        Logger.d { "✅ TvShowDetailsScreenViewModel: Added to watchlist - ID: $currentTvShowId" }
                    }
                } catch (e: Exception) {
                    Logger.e { "❌ TvShowDetailsScreenViewModel: Failed to toggle watchlist - ${e.message}" }
                } finally {
                    _watchlistLoading.value = false
                }
            }
        }
    }
    
    val uiState: StateFlow<TvShowDetailsScreenUiState> = combine(
        savedStateHandle
            .getStateFlow<String?>(TvShowDetailsScreen.TvShowIdBundleKey, null),
        userRepository.userToken,
    ) { tvShowId, userToken ->
        Logger.d { "📺 TvShowDetailsScreenViewModel: Processing tvShowId=$tvShowId, userToken=${userToken != null}" }
        
        if (tvShowId == null || userToken == null) {
            Logger.e { "❌ TvShowDetailsScreenViewModel: Missing tvShowId or userToken" }
            TvShowDetailsScreenUiState.Error
        } else {
            Logger.d { "🔍 TvShowDetailsScreenViewModel: Fetching details for tvShowId=$tvShowId" }
            
            val detailsResult = tvShowRepository.getTvShowsDetails(
                tvShowId = tvShowId,
                token = userToken
            )
            
            when (detailsResult) {
                is ApiResult.Success -> {
                    val details = detailsResult.data
                    Logger.d { "✅ TvShowDetailsScreenViewModel: Successfully fetched details for tvShow: ${details.title}" }
                    
                    // Fetch seasons if seasonsCount > 0
                    val seasons = if ((details.seasonsCount ?: 0) > 0) {
                        Logger.d { "🎬 TvShowDetailsScreenViewModel: Fetching seasons for tvShow ${details.id}, seasonsCount: ${details.seasonsCount}" }
                        when (val seasonsResult = tvShowRepository.getTvShowSeasons(
                            token = userToken,
                            tvShowId = details.id
                        )) {
                            is ApiResult.Success -> {
                                val seasonsWithEpisodes = seasonsResult.data.member.map { season ->
                                    if ((season.episodesCount ?: 0) > 0) {
                                        Logger.d { "📺 TvShowDetailsScreenViewModel: Fetching episodes for season ${season.id}, episodesCount: ${season.episodesCount}" }
                                        when (val episodesResult = tvShowRepository.getTvShowSeasonEpisodes(
                                            token = userToken,
                                            tvShowId = details.id,
                                            seasonId = season.id
                                        )) {
                                            is ApiResult.Success -> season.copy(episodes = episodesResult.data.member)
                                            is ApiResult.Error -> {
                                                Logger.e { "❌ TvShowDetailsScreenViewModel: Failed to fetch episodes for season ${season.id}" }
                                                season
                                            }
                                        }
                                    } else {
                                        season
                                    }
                                }
                                Logger.d { "✅ TvShowDetailsScreenViewModel: Successfully fetched ${seasonsWithEpisodes.size} seasons" }
                                seasonsWithEpisodes
                            }
                            is ApiResult.Error -> {
                                Logger.e { "❌ TvShowDetailsScreenViewModel: Failed to fetch seasons - ${seasonsResult.message ?: seasonsResult.error}" }
                                emptyList()
                            }
                        }
                    } else {
                        emptyList()
                    }
                    
                    val genreId =
                        if (details.genres?.isNotEmpty() == true) details.genres.first().id else 0

                    val similarTvShows = getTvShowsByGenre(
                        genreId = genreId,
                        tvShowRepository = tvShowRepository,
                        userRepository = userRepository
                    )
                    TvShowDetailsScreenUiState.Done(
                        tvShow = details,
                        seasons = seasons,
                        similarTvShows = similarTvShows
                    )
                }
                is ApiResult.Error -> {
                    Logger.e { "❌ TvShowDetailsScreenViewModel: Failed to fetch details - ${detailsResult.message ?: detailsResult.error}" }
                    TvShowDetailsScreenUiState.Error
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TvShowDetailsScreenUiState.Loading
    )


    private fun getTvShowsByGenre(
        tvShowRepository: TvShowsRepository,
        genreId: Int,
        userRepository: UserRepository
    ): StateFlow<PagingData<TvShow>> {
        return TvShowPagingSources().getTvShowsGenrePagingSource(
            genreId = genreId,
            tvShowRepository = tvShowRepository,
            userRepository = userRepository
        ).cachedIn(viewModelScope).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PagingData.empty()
        )
    }
}


sealed class TvShowDetailsScreenUiState {
    data object Loading : TvShowDetailsScreenUiState()
    data object Error : TvShowDetailsScreenUiState()
    data class Done(
        val tvShow: TvShow,
        val seasons: List<Season> = emptyList(),
        val similarTvShows: StateFlow<PagingData<TvShow>>
    ) : TvShowDetailsScreenUiState()
}
