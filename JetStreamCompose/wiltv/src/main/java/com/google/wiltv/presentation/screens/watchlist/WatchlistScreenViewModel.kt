// ABOUTME: ViewModel for watchlist screen managing state and user interactions
// ABOUTME: Combines watchlist data with full movie/TV details and handles remove operations
package com.google.wiltv.presentation.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.repositories.MovieRepository
import com.google.wiltv.data.repositories.TvShowsRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.data.repositories.WatchlistRepository
import com.google.wiltv.domain.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistScreenViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val userRepository: UserRepository,
    private val movieRepository: MovieRepository,
    private val tvShowsRepository: TvShowsRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)

    val uiState: StateFlow<WatchlistScreenUiState> = combine(
        userRepository.userId,
        userRepository.userToken,
        _loading
    ) { userId, userToken, loading ->
        if (loading) {
            WatchlistScreenUiState.Loading
        } else if (userId == null || userToken == null) {
            WatchlistScreenUiState.Error
        } else {
            getUserWatchlistItems(userId, userToken)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WatchlistScreenUiState.Loading
    )

    private suspend fun getUserWatchlistItems(userId: String, userToken: String): WatchlistScreenUiState {
        return try {
            val watchlistItems = watchlistRepository.getUserWatchlist(userId).stateIn(viewModelScope).value
            
            if (watchlistItems.isEmpty()) {
                return WatchlistScreenUiState.Empty
            }

            val contentItems = mutableListOf<WatchlistContentItem>()

            for (item in watchlistItems) {
                when (item.contentType) {
                    "movie" -> {
                        when (val result = movieRepository.getMovieDetailsNew(
                            movieId = item.contentId.toString(),
                            token = userToken
                        )) {
                            is ApiResult.Success -> {
                                contentItems.add(WatchlistContentItem.Movie(item.contentId, result.data))
                            }
                            is ApiResult.Error -> {
                                // Skip failed items but continue processing others
                                continue
                            }
                        }
                    }
                    "tvshow" -> {
                        when (val result = tvShowsRepository.getTvShowsDetails(
                            tvShowId = item.contentId.toString(),
                            token = userToken
                        )) {
                            is ApiResult.Success -> {
                                contentItems.add(WatchlistContentItem.TvShow(item.contentId, result.data))
                            }
                            is ApiResult.Error -> {
                                // Skip failed items but continue processing others
                                continue
                            }
                        }
                    }
                }
            }

            if (contentItems.isEmpty()) {
                WatchlistScreenUiState.Empty
            } else {
                WatchlistScreenUiState.Success(contentItems)
            }
        } catch (e: Exception) {
            WatchlistScreenUiState.Error
        }
    }

    fun removeFromWatchlist(contentId: Int) {
        viewModelScope.launch {
            try {
                val userId = userRepository.ensureUserIdExists()
                watchlistRepository.removeFromWatchlist(userId, contentId)
                // State will automatically update due to reactive Flow
            } catch (e: Exception) {
                // Handle error silently for now, could add error state later
            }
        }
    }
}

sealed class WatchlistScreenUiState {
    data object Loading : WatchlistScreenUiState()
    data object Empty : WatchlistScreenUiState()
    data object Error : WatchlistScreenUiState()
    data class Success(val watchlistItems: List<WatchlistContentItem>) : WatchlistScreenUiState()
}

sealed class WatchlistContentItem {
    abstract val contentId: Int
    
    data class Movie(override val contentId: Int, val movie: MovieNew) : WatchlistContentItem()
    data class TvShow(override val contentId: Int, val tvShow: com.google.wiltv.data.models.TvShow) : WatchlistContentItem()
}