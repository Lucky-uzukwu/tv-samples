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
import javax.inject.Inject

@HiltViewModel
class MovieDetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    movieRepository: MovieRepository,
    userRepository: UserRepository,
) : ViewModel() {
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
