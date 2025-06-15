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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.Carousel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.rememberCarouselState
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.presentation.theme.JetStreamBorderWidth
import com.google.jetstream.presentation.theme.JetStreamCardShape
import com.google.jetstream.presentation.utils.handleDPadKeyEvents


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvShowHeroSectionCarousel(
    tvShows: LazyPagingItems<TvShow>,
    goToMoreInfo: (tvShow: TvShow) -> Unit,
    setSelectedTvShow: (TvShow) -> Unit,
    modifier: Modifier = Modifier,
    isCarouselFocused: Boolean = false,
) {

    val carouselState = rememberCarouselState()
    val moreInfoButtonFocusRequester = remember { FocusRequester() }
    val displayTitleFocusRequester = remember { FocusRequester() }

    Carousel(
        itemCount = tvShows.itemCount,
        modifier = modifier
            .handleDPadKeyEvents(
                onRight = {
                    moreInfoButtonFocusRequester.requestFocus()
                },
            ),
        carouselIndicator = {},
        carouselState = carouselState,
        autoScrollDurationMillis = 5000,
        contentTransformStartToEnd = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
        contentTransformEndToStart = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
    ) { idx ->
        val tvShow = tvShows[idx] ?: return@Carousel
        setSelectedTvShow(tvShow)
        CarouselItemImage(
            tvShow = tvShow,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = JetStreamBorderWidth,
                    color = if (isCarouselFocused) Color.White else Color.Transparent,
                    shape = JetStreamCardShape
                )
        )

        CarouselItemForeground(
            tvShow = tvShow,
            modifier = Modifier
                .fillMaxSize(),
            onMoreInfoClick = {
                goToMoreInfo(tvShows.itemSnapshotList.items[idx])
            },
            displayTitleFocusRequester = displayTitleFocusRequester,
            moreInfoButtonFocusRequester = moreInfoButtonFocusRequester,
        )
    }
}
