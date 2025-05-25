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

package com.google.jetstream.presentation.screens.shows

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.StreamingProviderIcon
import com.google.jetstream.presentation.common.TvShowHeroSectionCarousel
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.forEach

@Composable
fun TVShowScreen(
    onTVShowClick: (tvShow: TvShow) -> Unit,
    goToVideoPlayer: (tvShow: TvShow) -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    isTopBarVisible: Boolean,
    tvShowScreenViewModel: TvShowScreenViewModel = hiltViewModel(),
) {
    val uiState by tvShowScreenViewModel.uiState.collectAsStateWithLifecycle()
    val heroSectionTvShows = tvShowScreenViewModel.heroSectionTvShows.collectAsLazyPagingItems()

    when (val currentState = uiState) {
        is TvShowScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is TvShowScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())

        is TvShowScreenUiState.Ready -> {
            Catalog(
                heroSectionTvShows = heroSectionTvShows,
                catalogToTvShows = currentState.catalogToTvShows,
                genreToTvShows = currentState.genreToTvShows,
                onTVShowClick = onTVShowClick,
                onScroll = onScroll,
                setSelectedTvShow = setSelectedTvShow,
                goToVideoPlayer = goToVideoPlayer,
                isTopBarVisible = isTopBarVisible,
                streamingProviders = currentState.streamingProviders,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun Catalog(
    heroSectionTvShows: LazyPagingItems<TvShow>,
    catalogToTvShows: Map<Catalog, StateFlow<PagingData<TvShow>>>,
    genreToTvShows: Map<Genre, StateFlow<PagingData<TvShow>>>,
    onTVShowClick: (tvShow: TvShow) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    goToVideoPlayer: (tvShow: TvShow) -> Unit,
    modifier: Modifier = Modifier,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
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
                TvShowHeroSectionCarousel(
                    tvShows = heroSectionTvShows,
                    goToVideoPlayer = goToVideoPlayer,
                    goToMoreInfo = {},
                    setSelectedTvShow = setSelectedTvShow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                )
            }

            item() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    streamingProviders.forEach { streamingProvider ->
                        if (streamingProvider.logoPath != null) {
                            StreamingProviderIcon(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .focusable(),
                                logoPath = streamingProvider.logoPath,
                                contentDescription = streamingProvider.name,
                            )
                            Spacer(Modifier.width(16.dp))
                        }

                    }
                }
            }

            // Loop through catalogList to display each catalog and its movies
//            items(
//                items = catalogToTvShows.keys.toList(),
//                key = { catalog -> catalog.id }, // Use catalog ID as unique key
//                contentType = { "MoviesRow" }
//            ) { catalog ->
//                val movies = catalogToTvShows[catalog]?.collectAsLazyPagingItems()
//                val movieList = movies?.itemSnapshotList?.items ?: emptyList()
//
//
//                Top10MoviesList(
//                    movieList = movieList,
//                    sectionTitle = catalog.name,
//                    onMovieClick = onTVShowClick,
//                    setSelectedMovie = setSelectedTvShow,
//                    modifier = Modifier.onFocusChanged {
//                        immersiveListHasFocus = it.hasFocus
//                    },
//                )
//            }
//
//            // Loop through genreList to display each catalog and its movies
//            items(
//                items = genreToTvShows.keys.toList(),
//                key = { genre -> genre.id }, // Use catalog ID as unique key
//                contentType = { "MoviesRow" }
//            ) { genre ->
//                val movies = genreToTvShows[genre]?.collectAsLazyPagingItems()
//                val movieList = movies?.itemSnapshotList?.items ?: emptyList()
//
//                Top10MoviesList(
//                    movieList = movieList,
//                    sectionTitle = genre.name,
//                    onMovieClick = onTVShowClick,
//                    setSelectedMovie = setSelectedTvShow,
//                    modifier = Modifier.onFocusChanged {
//                        immersiveListHasFocus = it.hasFocus
//                    },
//                )
//            }
        }
    }
}