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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.Carousel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.rememberCarouselState
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.presentation.utils.handleDPadKeyEvents


@Composable
fun Modifier.onFirstGainingVisibility(onGainingVisibility: () -> Unit): Modifier {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) { if (isVisible) onGainingVisibility() }

    return onPlaced { isVisible = true }
}

@Composable
fun Modifier.requestFocusOnFirstGainingVisibility(): Modifier {
    val focusRequester = remember { FocusRequester() }
    return focusRequester(focusRequester).onFirstGainingVisibility {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieHeroSectionCarouselNew(
    movies: LazyPagingItems<MovieNew>,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    goToMoreInfo: (movie: MovieNew) -> Unit,
    setSelectedMovie: (MovieNew) -> Unit,
    modifier: Modifier = Modifier
) {

    val carouselState = rememberCarouselState()
    var isCarouselFocused by remember { mutableStateOf(true) }
    val carouselFocusRequester = remember { FocusRequester() }
    val watchNowButtonFocusRequester = remember { FocusRequester() }
    val moreInfoButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        watchNowButtonFocusRequester.requestFocus()
    }

    Carousel(
        itemCount = movies.itemCount,
        modifier = modifier.handleDPadKeyEvents(
            onRight = {},
//            onDown = {
//                carouselFocusRequester.requestFocus()
//            },
//            onUp = {
//                isCarouselFocused = false
//            }
        ),
        carouselIndicator = {},
        carouselState = carouselState,
        autoScrollDurationMillis = 3000,
        contentTransformStartToEnd = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
        contentTransformEndToStart = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
    ) { idx ->
        val movie = movies[idx] ?: return@Carousel

        setSelectedMovie(movie)

        CarouselItemImage(
            movie = movie,
            modifier = Modifier.fillMaxSize()
        )

        CarouselItemForeground(
            movie = movie,
            isCarouselFocused = isCarouselFocused,
            modifier = Modifier.fillMaxSize(),
            onWatchNowClick = {
                goToVideoPlayer(movies.itemSnapshotList.items[idx])
            },
            onMoreInfoClick = {
                goToMoreInfo(movies.itemSnapshotList.items[idx])
            },
            watchNowButtonFocusRequester = watchNowButtonFocusRequester,
            moreInfoButtonFocusRequester = moreInfoButtonFocusRequester,
        )
    }

}