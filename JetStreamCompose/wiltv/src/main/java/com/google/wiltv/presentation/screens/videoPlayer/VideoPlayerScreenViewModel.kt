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

package com.google.wiltv.presentation.screens.videoPlayer

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.repositories.MovieRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.data.paging.pagingsources.movie.MoviesPagingSources
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import java.net.URLDecoder

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
    ) { contentId, userToken ->
        if (contentId == null) {
            VideoPlayerScreenUiState.Error(UiText.DynamicString("Missing content ID"))
        } else {
            try {
                // Check if this is a TV channel URL (URL-encoded)
                val decodedContent = URLDecoder.decode(contentId, "UTF-8")
                
                if (decodedContent.startsWith("http")) {
                    // This is a TV channel URL - create a simple state for direct playback
                    VideoPlayerScreenUiState.TvChannelDirect(directUrl = decodedContent)
                } else {
                    // This is a movie/TV show ID - fetch movie details
                    if (userToken == null) {
                        VideoPlayerScreenUiState.Error(UiText.DynamicString("Missing user token"))
                    } else {
                        val detailsResult = movieRepository.getMovieDetailsNew(
                            movieId = decodedContent,
                            token = userToken
                        )
                        
                        val details = when (detailsResult) {
                            is ApiResult.Success -> detailsResult.data
                            is ApiResult.Error -> return@combine VideoPlayerScreenUiState.Error(
                                detailsResult.error.asUiText(detailsResult.message)
                            )
                        }

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
                }
            } catch (e: Exception) {
                VideoPlayerScreenUiState.Error(UiText.DynamicString("Failed to process content ID: ${e.message}"))
            }
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
    data class Error(val uiText: UiText) : VideoPlayerScreenUiState()
    data class Done(
        val movie: MovieNew,
        val similarMovies: StateFlow<PagingData<MovieNew>>
    ) : VideoPlayerScreenUiState()
    data class TvChannelDirect(
        val directUrl: String,
        val title: String? = "TV Channel"
    ) : VideoPlayerScreenUiState()
}
