// ABOUTME: Hero section carousel component specifically designed for TV channels
// ABOUTME: Displays featured TV channels in a carousel with focus management and selection handling

package com.google.wiltv.presentation.common

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.wiltv.data.network.TvChannel


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvChannelHeroSectionCarousel(
    tvChannels: LazyPagingItems<TvChannel>,
    onChannelClick: (channel: TvChannel) -> Unit,
    setSelectedTvChannel: (TvChannel) -> Unit,
    modifier: Modifier = Modifier,
    carouselState: CarouselState,
    carouselScrollEnabled: Boolean,
    carouselFocusRequester: FocusRequester,
    firstLazyRowItemUnderCarouselRequester: FocusRequester,
) {

    var isCarouselFocused by remember { mutableStateOf(false) }
    val itemsPerPage = 3
    val activeItemIndex = carouselState.activeItemIndex
    val totalItems = tvChannels.itemCount
    val currentPage = activeItemIndex / itemsPerPage
    val startIndex = currentPage * itemsPerPage

    Carousel(
        itemCount = tvChannels.itemCount,
        modifier = modifier
            .focusProperties {
                down = firstLazyRowItemUnderCarouselRequester
            }
            .onFocusChanged {
                isCarouselFocused = it.hasFocus
            }
            .focusRequester(carouselFocusRequester),
        carouselIndicator = {
            CarouselIndicator(
                itemCount = minOf(itemsPerPage, totalItems - startIndex), // Show up to 5 items
                activeItemIndex = activeItemIndex % itemsPerPage, // Relative index within the page
            )
        },
        carouselState = carouselState,
        contentTransformStartToEnd = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
        contentTransformEndToStart = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
    ) { idx ->
        Box(modifier = Modifier.fillMaxSize()) {
            val tvChannel = tvChannels[idx] ?: return@Carousel

            LaunchedEffect(tvChannel) {
                if (carouselScrollEnabled) {
                    setSelectedTvChannel(tvChannel)
                }
            }

            val isActiveItem = idx == activeItemIndex

            TvChannelCarouselItemForeground(
                tvChannel = tvChannel,
                onChannelClick = {
                    onChannelClick(tvChannel)
                },
                isCarouselFocused = isCarouselFocused && isActiveItem,
                modifier = Modifier
                    .align(Alignment.BottomStart),
            )

        }
    }
}