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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MovieHeroSectionCarouselNew
import com.google.jetstream.presentation.screens.backgroundImageState
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
    val featuredMovies = homeScreeViewModel.heroMovies.collectAsLazyPagingItems()

    when (val s = uiState) {
        is HomeScreenUiState.Ready -> {
            Catalog(
                featuredMovies = featuredMovies,
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
    featuredMovies: LazyPagingItems<MovieNew>,
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
    val backgroundState = backgroundImageState()

    val shouldShowTopBar by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset < 300
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val targetBitmap by remember(backgroundState) { backgroundState.drawable }

        val overlayColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)

        Crossfade(targetState = targetBitmap) {
            it?.let {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                Brush.horizontalGradient(
                                    listOf(
                                        overlayColor,
                                        overlayColor.copy(alpha = 0.8f),
                                        Color.Transparent
                                    )
                                )
                            )
                            drawRect(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent, overlayColor.copy(alpha = 0.5f)
                                    )
                                )
                            )
                        },
                    bitmap = it,
                    contentDescription = "Hero item background",
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize()
    ) {

        item(contentType = "HeroSectionCarousel") {
            MovieHeroSectionCarouselNew(
                movies = featuredMovies,
                goToVideoPlayer = goToVideoPlayer,
                goToMoreInfo = {},
                setSelectedMovie = { movie ->
                    val imageUrl = "https://stage.nortv.xyz/" + "storage/" + movie.backdropImagePath
                    setSelectedMovie(movie)
                    backgroundState.load(
                        url = imageUrl
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
        }
    }


//    Box {
//        LazyColumn(
//            state = lazyListState,
//            contentPadding = PaddingValues(bottom = 108.dp),
//            modifier = modifier
////            .focusable()
////            .handleDPadKeyEvents(
////                onDown = {
////                    if (lazyListState.firstVisibleItemIndex == 0) {
////                        carouselFocusRequester.requestFocus()
////                    } else {
////                        carouselFocusRequester.freeFocus()
////                        focusManager.moveFocus(FocusDirection.Down)
////                    }
////                },
////                onUp = {
////                    focusManager.moveFocus(FocusDirection.Up)
////                }
////            ),
//        ) {

//
////        item {
////            Row(
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .padding(horizontal = 48.dp, vertical = 16.dp),
////                verticalAlignment = Alignment.CenterVertically
////            ) {
////                streamingProviders.forEach { streamingProvider ->
////                    if (streamingProvider.logoPath != null) {
////                        StreamingProviderIcon(
////                            modifier = Modifier
////                                .padding(top = 16.dp),
////                            logoPath = streamingProvider.logoPath,
////                            contentDescription = streamingProvider.name,
////                        )
////                        Spacer(Modifier.width(16.dp))
////                    }
////
////                }
////            }
////        }
//
//            // Loop through catalogList to display each catalog and its movies
//            items(
//                items = catalogToMovies.keys.toList(),
//                key = { catalog -> catalog.id }, // Use catalog ID as unique key
//                contentType = { "MoviesRow" }
//            ) { catalog ->
//                val movies = catalogToMovies[catalog]?.collectAsLazyPagingItems()
//                val movieList = movies?.itemSnapshotList?.items ?: emptyList()
//
//
//                Top10MoviesList(
//                    movieList = movieList,
//                    sectionTitle = catalog.name,
//                    onMovieClick = onMovieClick,
//                    setSelectedMovie = setSelectedMovie,
//                    modifier = Modifier.onFocusChanged {
//                        immersiveListHasFocus = it.hasFocus
//                    },
//                )
//
//            }
//        }
//    }
}