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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.network.Genre
import com.google.jetstream.data.network.MovieNew
import com.google.jetstream.data.repositories.CatalogRepository
import com.google.jetstream.data.repositories.GenreRepository
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.UserRepository
import com.google.jetstream.presentation.screens.home.pagingsources.HomeScreenPagingSources
import com.google.jetstream.presentation.screens.home.pagingsources.MoviesHeroSectionPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class HomeScreeViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    userRepository: UserRepository,
    catalogRepository: CatalogRepository,
    genreRepository: GenreRepository
) : ViewModel() {

    // Paginated flows for movie lists
    val heroSectionMovies: StateFlow<PagingData<MovieNew>> = Pager(
        PagingConfig(pageSize = 5, initialLoadSize = 5)
    ) {
        MoviesHeroSectionPagingSource(movieRepository, userRepository)
    }.flow.cachedIn(viewModelScope).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
    )

    // UI State combining all data
    val uiState: StateFlow<HomeScreenUiState> = combine(
        userRepository.userToken,
        movieRepository.getFeaturedMovies(),
        movieRepository.getTrendingMovies(),
        movieRepository.getTop10Movies(),
        movieRepository.getNowPlayingMovies(),
    ) { token, featuredMovieList, trendingMovieList, top10MovieList, nowPlayingMovieList ->
        when {
            token.isNullOrBlank() -> HomeScreenUiState.Error
            else -> {
                // Create paginated flows for each catalog
                val catalogToMovies = fetchCatalogsAndMovies(
                    catalogRepository,
                    token,
                    userRepository
                )
                val genreToMovies = fetchGenresAndMovies(
                    genreRepository = genreRepository,
                    token,
                    userRepository
                )

                HomeScreenUiState.Ready(
                    featuredMovieList = featuredMovieList,
                    trendingMovieList = trendingMovieList,
                    top10MovieList = top10MovieList,
                    nowPlayingMovieList = nowPlayingMovieList,
                    catalogToMovies = catalogToMovies,
                    genreToMovies = genreToMovies
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenUiState.Loading
    )

    private suspend fun fetchCatalogsAndMovies(
        catalogRepository: CatalogRepository,
        token: String,
        userRepository: UserRepository
    ): Map<Catalog, StateFlow<PagingData<MovieNew>>> {
        val catalogs = catalogRepository.getMovieCatalog(token).firstOrNull() ?: emptyList()
        val catalogToMovies = catalogs.associateWith { catalog ->
            HomeScreenPagingSources().getMoviesCatalogPagingSource(
                catalog = catalog,
                movieRepository = movieRepository,
                userRepository = userRepository
            ).cachedIn(viewModelScope).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PagingData.empty()
            )
        }
        return catalogToMovies
    }

    private suspend fun fetchGenresAndMovies(
        genreRepository: GenreRepository,
        token: String,
        userRepository: UserRepository
    ): Map<Genre, StateFlow<PagingData<MovieNew>>> {
        val genres = genreRepository.getMovieGenre(token).firstOrNull() ?: emptyList()
        val genreToMovies = genres.associateWith { catalog ->
            HomeScreenPagingSources().getMoviesGenrePagingSource(
                genre = catalog,
                movieRepository = movieRepository,
                userRepository = userRepository
            ).cachedIn(viewModelScope).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PagingData.empty()
            )
        }
        return genreToMovies
    }
}

sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState
    data object Error : HomeScreenUiState
    data class Ready(
        val featuredMovieList: MovieList,
        val trendingMovieList: MovieList,
        val top10MovieList: MovieList,
        val nowPlayingMovieList: MovieList,
        val catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
        val genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>
    ) : HomeScreenUiState
}
