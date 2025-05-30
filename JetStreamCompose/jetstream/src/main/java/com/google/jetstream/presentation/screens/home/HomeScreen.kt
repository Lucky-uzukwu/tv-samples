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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MovieHeroSectionCarousel
import com.google.jetstream.presentation.common.Top10MoviesList
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
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
                featuredMoviesNew = featuredMovies,
                catalogToMovies = s.catalogToMovies,
                genreToMovies = s.genreToMovies,
                onMovieClick = onMovieClick,
                onScroll = onScroll,
                setSelectedMovie = setSelectedMovie,
                goToVideoPlayer = goToVideoPlayer,
                isTopBarVisible = isTopBarVisible,
                streamingProviders = s.streamingProviders,
                modifier = Modifier.fillMaxSize(),
            )
        }

        is HomeScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is HomeScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun Catalog(
    featuredMoviesNew: LazyPagingItems<MovieNew>,
    catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
    genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
    onMovieClick: (movie: MovieNew) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    modifier: Modifier = Modifier,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    streamingProviders: List<StreamingProvider>,
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
    val carouselFocusRequester = remember { FocusRequester() }

    LaunchedEffect(shouldShowTopBar) {
        onScroll(shouldShowTopBar)
    }
    LaunchedEffect(isTopBarVisible) {
        if (isTopBarVisible) lazyListState.animateScrollToItem(0)
    }


    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 108.dp),
        modifier = modifier
//            .focusable()
//            .handleDPadKeyEvents(
//                onDown = {
//                    if (lazyListState.firstVisibleItemIndex == 0) {
//                        carouselFocusRequester.requestFocus()
//                    } else {
//                        carouselFocusRequester.freeFocus()
//                        focusManager.moveFocus(FocusDirection.Down)
//                    }
//                },
//                onUp = {
//                    focusManager.moveFocus(FocusDirection.Up)
//                }
//            ),
    ) {
        item(contentType = "HeroSectionCarousel") {
            MovieHeroSectionCarousel(
                movies = featuredMoviesNew,
                goToVideoPlayer = goToVideoPlayer,
                goToMoreInfo = {},
                setSelectedMovie = setSelectedMovie,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .focusRequester(carouselFocusRequester),
            )
        }

//        item {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 48.dp, vertical = 16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                streamingProviders.forEach { streamingProvider ->
//                    if (streamingProvider.logoPath != null) {
//                        StreamingProviderIcon(
//                            modifier = Modifier
//                                .padding(top = 16.dp),
//                            logoPath = streamingProvider.logoPath,
//                            contentDescription = streamingProvider.name,
//                        )
//                        Spacer(Modifier.width(16.dp))
//                    }
//
//                }
//            }
//        }

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
                setSelectedMovie = setSelectedMovie,
                modifier = Modifier.onFocusChanged {
                    immersiveListHasFocus = it.hasFocus
                },
            )

        }
    }
}