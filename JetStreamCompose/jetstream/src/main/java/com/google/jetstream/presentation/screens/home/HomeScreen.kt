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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.network.Genre
import com.google.jetstream.data.network.MovieNew
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    onMovieClick: (movie: Movie) -> Unit,
    goToVideoPlayer: (movie: Movie) -> Unit,
    selectedMovie: MovieNew? = null,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    isTopBarVisible: Boolean,
    homeScreeViewModel: HomeScreeViewModel = hiltViewModel(),
) {
    val uiState by homeScreeViewModel.uiState.collectAsStateWithLifecycle()
    val featuredMovies = homeScreeViewModel.heroSectionMovies.collectAsLazyPagingItems()

    when (val s = uiState) {
        is HomeScreenUiState.Ready -> {
            Catalog(
                featuredMovies = s.featuredMovieList,
                featuredMoviesNew = featuredMovies,
                catalogToMovies = s.catalogToMovies,
                genreToMovies = s.genreToMovies,
                onMovieClick = { },
                onScroll = onScroll,
                selectedMovie = selectedMovie,
                setSelectedMovie = setSelectedMovie,
                goToVideoPlayer = goToVideoPlayer,
                isTopBarVisible = isTopBarVisible,
                modifier = Modifier.fillMaxSize(),
            )
        }

        is HomeScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is HomeScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun Catalog(
    featuredMovies: MovieList,
    featuredMoviesNew: LazyPagingItems<MovieNew>,
    catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
    genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
    onMovieClick: (movie: MovieNew) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    goToVideoPlayer: (movie: Movie) -> Unit,
    modifier: Modifier = Modifier,
    selectedMovie: MovieNew? = null,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    isTopBarVisible: Boolean = true,
) {
    val lazyListState = rememberLazyListState()
    var immersiveListHasFocus by remember { mutableStateOf(false) }

    val shouldShowTopBar by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset < 300
        }
    }

    LaunchedEffect(shouldShowTopBar) {
        onScroll(shouldShowTopBar)
    }
    LaunchedEffect(isTopBarVisible) {
        if (isTopBarVisible) lazyListState.animateScrollToItem(0)
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(bottom = 108.dp),
            modifier = modifier,
        ) {
            item(contentType = "HeroSectionCarousel") {
                HeroSectionCarousel(
                    movies = featuredMovies,
                    moviesNew = featuredMoviesNew,
                    goToVideoPlayer = goToVideoPlayer,
                    goToMoreInfo = {},
                    setSelectedMovie = setSelectedMovie,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                )
            }

            // Loop through catalogList to display each catalog and its movies
            items(
                items = catalogToMovies.keys.toList(),
                key = { catalog -> catalog.id }, // Use catalog ID as unique key
                contentType = { "MoviesRow" }
            ) { catalog ->
                val movies = catalogToMovies[catalog]?.collectAsLazyPagingItems()
                val movieList = movies?.itemSnapshotList?.items ?: emptyList()


                Top10MoviesList(
                    movieList = movieList,
                    sectionTitle = catalog.name,
                    onMovieClick = onMovieClick,
                    setSelectedMovie =setSelectedMovie ,
                    modifier = Modifier.onFocusChanged {
                        immersiveListHasFocus = it.hasFocus
                    },
                )

//            MoviesRow(
//                movieList = movieList,
//                title = catalog.name,
//                onMovieSelected = onMovieClick,
//                modifier = Modifier.padding(top = 16.dp),
//            )
            }

            // Loop through genreList to display each catalog and its movies
            items(
                items = genreToMovies.keys.toList(),
                key = { genre -> genre.id }, // Use catalog ID as unique key
                contentType = { "MoviesRow" }
            ) { genre ->
                val movies = genreToMovies[genre]?.collectAsLazyPagingItems()
                val movieList = movies?.itemSnapshotList?.items ?: emptyList()

                Top10MoviesList(
                    movieList = movieList,
                    sectionTitle = genre.name,
                    onMovieClick = onMovieClick,
                    setSelectedMovie =setSelectedMovie ,
                    modifier = Modifier.onFocusChanged {
                        immersiveListHasFocus = it.hasFocus
                    },
                )
            }


            // Uncomment other sections as needed
            /*

            item(contentType = "Top10MoviesList") {
                Top10MoviesList(
                    movieList = top10Movies,
                    sectionTitle = "Trending Movies",
                    onMovieClick = onMovieClick,
                    modifier = Modifier.onFocusChanged {
                        immersiveListHasFocus = it.hasFocus
                    },
                )
            }
            item(contentType = "Top10MoviesList") {
                Top10MoviesList(
                    movieList = top10Movies,
                    sectionTitle = "Action Movies",
                    onMovieClick = onMovieClick,
                    modifier = Modifier.onFocusChanged {
                        immersiveListHasFocus = it.hasFocus
                    },
                )
            }
            item(contentType = "MoviesRow") {
                MoviesRow(
                    modifier = Modifier.padding(top = 16.dp),
                    movieList = nowPlayingMovies,
                    title = StringConstants.Composable.HomeScreenNowPlayingMoviesTitle,
                    onMovieSelected = onMovieClick
                )
            }
            */
        }
    }
}