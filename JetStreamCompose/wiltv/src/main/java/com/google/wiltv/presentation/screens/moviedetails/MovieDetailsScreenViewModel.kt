package com.google.wiltv.presentation.screens.moviedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.paging.pagingsources.movie.MoviesPagingSources
import com.google.wiltv.data.repositories.MovieRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.data.repositories.WatchlistRepository
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.asUiText
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
class MovieDetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
    private val userRepository: UserRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {
    
    private val movieId: String? = savedStateHandle.get<String?>(MovieDetailsScreen.MovieIdBundleKey)
    
    // Watchlist state management
    val isInWatchlist: StateFlow<Boolean> = combine(
        userRepository.userId,
        savedStateHandle.getStateFlow<String?>(MovieDetailsScreen.MovieIdBundleKey, null)
    ) { userId, movieId ->
        // Use default user ID if not authenticated
        val effectiveUserId = userId ?: "default_user"
        if (movieId != null) {
            try {
                watchlistRepository.isInWatchlist(effectiveUserId, movieId.toIntOrNull() ?: 0).firstOrNull() ?: false
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
            val currentMovieId = movieId?.toIntOrNull()
            
            // Use ensureUserIdExists to safely get/create user ID
            val effectiveUserId = try {
                userRepository.ensureUserIdExists()
            } catch (e: Exception) {
                "default_user" // Fallback if storage fails
            }
            
            if (currentMovieId != null) {
                try {
                    _watchlistLoading.value = true
                    val isCurrentlyInWatchlist = isInWatchlist.value
                    
                    if (isCurrentlyInWatchlist) {
                        watchlistRepository.removeFromWatchlist(effectiveUserId, currentMovieId)
                        // Add success feedback here if needed
                    } else {
                        watchlistRepository.addToWatchlist(effectiveUserId, currentMovieId, "movie")
                        // Add success feedback here if needed
                    }
                } catch (e: Exception) {
                    // Handle error - add error feedback if needed
                    android.util.Log.e("MovieDetailsViewModel", "Error toggling watchlist", e)
                } finally {
                    _watchlistLoading.value = false
                }
            }
        }
    }
    
    val uiState: StateFlow<MovieDetailsScreenUiState> = combine(
        savedStateHandle
            .getStateFlow<String?>(MovieDetailsScreen.MovieIdBundleKey, null),
        userRepository.userToken,
    ) { movieId, userToken ->
        if (movieId == null || userToken == null) {
            MovieDetailsScreenUiState.Error(UiText.DynamicString("Missing movie ID or token"))
        } else {
            val detailsResult = movieRepository.getMovieDetailsNew(
                movieId = movieId,
                token = userToken
            )
            
            val details = when (detailsResult) {
                is ApiResult.Success -> detailsResult.data
                is ApiResult.Error -> return@combine MovieDetailsScreenUiState.Error(
                    detailsResult.error.asUiText(detailsResult.message)
                )
            }


            val similarMovies = if (details.genres.isNotEmpty()) {
                fetchMoviesByGenre(
                    genreId = details.genres.first().id,
                    movieRepository = movieRepository,
                    userRepository = userRepository
                )
            } else {
                MutableStateFlow(PagingData.empty<MovieNew>())
            }
            MovieDetailsScreenUiState.Done(
                similarMovies = similarMovies,
                movie = details
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MovieDetailsScreenUiState.Loading
    )


    private fun fetchMoviesByGenre(
        movieRepository: MovieRepository,
        genreId: Int,
        userRepository: UserRepository
    ): StateFlow<PagingData<MovieNew>> {
        return MoviesPagingSources().getMoviesGenrePagingSource(
            genreId = genreId,
            movieRepository = movieRepository,
            userRepository = userRepository
        ).cachedIn(viewModelScope).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PagingData.empty()
        )
    }
}


sealed class MovieDetailsScreenUiState {
    data object Loading : MovieDetailsScreenUiState()
    data class Error(val uiText: UiText) : MovieDetailsScreenUiState()
    data class Done(
        val movie: MovieNew,
        val similarMovies: StateFlow<PagingData<MovieNew>>
    ) : MovieDetailsScreenUiState()
}
