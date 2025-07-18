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

package com.google.jetstream.presentation.screens.videoPlayer

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.UserRepository
import com.google.jetstream.data.paging.pagingsources.movie.MoviesPagingSources
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class VideoPlayerScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    movieRepository: MovieRepository,
    userRepository: UserRepository,
) : ViewModel() {
    val uiState: StateFlow<VideoPlayerScreenUiState> = combine(
        savedStateHandle
            .getStateFlow<String?>(VideoPlayerScreen.MovieIdBundleKey, null),
        userRepository.userToken,
    ) { movieId, userToken ->
        if (movieId == null || userToken == null) {
            VideoPlayerScreenUiState.Error
        } else {
            val details = movieRepository.getMovieDetailsNew(
                movieId = movieId,
                token = userToken
            ).firstOrNull() ?: return@combine VideoPlayerScreenUiState.Error

            val similarMovies = fetchMoviesByGenre(
                genreId = details.genres.first().id,
                movieRepository = movieRepository,
                userRepository = userRepository
            )
            VideoPlayerScreenUiState.Done(
                similarMovies = similarMovies,
                movie = details
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VideoPlayerScreenUiState.Loading
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


@Immutable
sealed class VideoPlayerScreenUiState {
    data object Loading : VideoPlayerScreenUiState()
    data object Error : VideoPlayerScreenUiState()
    data class Done(
        val movie: MovieNew,
        val similarMovies: StateFlow<PagingData<MovieNew>>
    ) : VideoPlayerScreenUiState()
}
