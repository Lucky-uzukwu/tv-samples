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

package com.google.jetstream.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.entities.MovieListNew
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.repositories.CatalogRepository
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeScreeViewModel @Inject constructor(
    movieRepository: MovieRepository,
    userRepository: UserRepository,
    catalogRepository: CatalogRepository
) : ViewModel() {

    val uiState: StateFlow<HomeScreenUiState> = combine(
        userRepository.userToken,
        movieRepository.getFeaturedMovies(),
        movieRepository.getTrendingMovies(),
        movieRepository.getTop10Movies(),
        movieRepository.getNowPlayingMovies(),
    ) { token, featuredMovieList, trendingMovieList, top10MovieList, nowPlayingMovieList ->
        if (token == null || token.isBlank()) {
            HomeScreenUiState.Error
        } else {
            // TODO: Look into why first() is a good choice
            val featuredMovieListNew =
                movieRepository.getMoviesToShowInHeroSection(token).first()

            val catalogList = catalogRepository.getMovieCatalog(token).first()

            HomeScreenUiState.Ready(
                featuredMovieList,
                featuredMovieListNew,
                trendingMovieList,
                top10MovieList,
                nowPlayingMovieList,
                catalogList
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenUiState.Loading
    )
}

sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState
    data object Error : HomeScreenUiState
    data class Ready(
        val featuredMovieList: MovieList,
        val featuredMovieListNew: MovieListNew,
        val trendingMovieList: MovieList,
        val top10MovieList: MovieList,
        val nowPlayingMovieList: MovieList,
        val catalogList: List<Catalog>
    ) : HomeScreenUiState
}
