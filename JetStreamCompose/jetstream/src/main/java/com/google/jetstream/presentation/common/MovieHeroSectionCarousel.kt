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

package com.google.jetstream.presentation.common

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.Carousel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import co.touchlab.kermit.Logger
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.utils.fadingEdge
import com.google.jetstream.presentation.utils.handleDPadKeyEvents


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieHeroSectionCarousel(
    movies: LazyPagingItems<MovieNew>,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    goToMoreInfo: (movie: MovieNew) -> Unit,
    setSelectedMovie: (MovieNew) -> Unit,
    modifier: Modifier = Modifier
) {
    var isCarouselFocused by remember { mutableStateOf(true) }
    var isWatchNowFocused by remember { mutableStateOf(false) }


    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    var currentMovieIndex by rememberSaveable { mutableIntStateOf(0) }

    val itemsPerPage = 5
    val startIndex by remember {
        derivedStateOf { currentPage * itemsPerPage }
    }
    val endIndex by remember {
        derivedStateOf { minOf(startIndex + itemsPerPage - 1, movies.itemCount - 1) }
    }
    val currentItemCount by remember {
        derivedStateOf { if (movies.itemCount > 0) (endIndex - startIndex + 1) else 0 }
    }

    val watchNowButtonFocusRequester = remember { FocusRequester() }
    val moreInfoButtonFocusRequester = remember { FocusRequester() }
    val topBottomFade =
        Brush.verticalGradient(
            // Create a gradient that starts from transparent, goes to red (or any visible color),
            // stays red for a while, and then fades back to transparent.
            // This creates a fading effect at the top and bottom of the Carousel.
            0f to Color.Transparent,
            0.1f to Color.Red,
            0.7f to Color.Red,
            1f to Color.Transparent,
        )

    Box(
        modifier = modifier
            .fadingEdge(topBottomFade)
    ) {
        Carousel(
            modifier = Modifier
                .onFocusChanged { isCarouselFocused = it.hasFocus }
                // Semantics for accessibility: describes the Carousel.
                .semantics {
                    contentDescription =
                        StringConstants.Composable.ContentDescription.MoviesCarousel
                }
                .handleDPadKeyEvents(
                    onRight = {
                        // Handle right D-pad key press: move to the next movie or page.
                        if (currentMovieIndex < currentItemCount - 1) {
                            currentMovieIndex += 1
                        } else if (startIndex + itemsPerPage < movies.itemCount) {
                            currentPage += 1
                            currentMovieIndex += 1
                            // Trigger pagination safely
                            movies.loadState.append is LoadState.NotLoading || movies.get(
                                startIndex + itemsPerPage
                            ) != null
                        }
                    },
                    onLeft = {
                        // Handle left D-pad key press: move to the previous movie or page.
                        if (currentMovieIndex > 0 && currentPage == 0) {
                            currentMovieIndex -= 1
                        } else {
                            // Check if currentPage is greater than 0 and currentMovieIndex is at the beginning of a page
                            if (currentPage > 0 && currentMovieIndex >= startIndex + itemsPerPage) { // Log entering the else if block
                                currentPage -= 1 // Log after decrementing currentPage
                                currentMovieIndex -= 1
                            } else {
                                if (currentPage > 0) {
                                    currentPage -= 1
                                    currentMovieIndex -= 1
                                } else {
                                    Logger.i("No conditions met in onLeft block.")
                                }
                            }
                        }
                    },
                    onDown = {
                        // Handle down D-pad key press: focus the "Watch Now" button.
                        isWatchNowFocused = true
                    }
                ),
            itemCount = currentItemCount,
            carouselIndicator = {
                CarouselIndicator(
                    itemCount = currentItemCount,
                    activeItemIndex = currentMovieIndex,
                )
            }, // Display carousel indicators (dots).
            autoScrollDurationMillis = 10000, // Auto-scroll every 5 seconds.
            // Define transitions for content changes.
            contentTransformStartToEnd = fadeIn(tween(durationMillis = 5000))
                .togetherWith(fadeOut(tween(durationMillis = 5000))),
            contentTransformEndToStart = fadeIn(tween(durationMillis = 5000))
                .togetherWith(fadeOut(tween(durationMillis = 5000))),
            content = { idx ->
                val movieIndex = startIndex + idx
                val movieNew = movies[movieIndex]
                currentMovieIndex = idx
                // Only render if the item is loaded
                if (movieNew != null) {
                    setSelectedMovie(movieNew)
                    CarouselItemImage(
                        movie = movieNew,
                        modifier = Modifier.fillMaxSize()
                    )
//                    CarouselItemForeground(
//                        movie = movieNew,
//                        isCarouselFocused = isCarouselFocused,
//                        modifier = Modifier.fillMaxSize(),
//                        onWatchNowClick = {
//                            movies.itemSnapshotList.items.get(movieIndex)
//                                .let { goToVideoPlayer(it) }
//                        },
//                        onMoreInfoClick = {
//                            movies.itemSnapshotList.items.get(movieIndex).let { goToMoreInfo(it) }
//                        },
//                        watchNowButtonFocusRequester = watchNowButtonFocusRequester,
//                        moreInfoButtonFocusRequester = moreInfoButtonFocusRequester,
//                    )
                }
            }
        )
    }
}