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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.paging.compose.LazyPagingItems
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.wiltv.data.models.TvShow


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvShowHeroSectionCarousel(
    tvShows: LazyPagingItems<TvShow>,
    goToMoreInfo: (tvShow: TvShow) -> Unit,
    setSelectedTvShow: (TvShow) -> Unit,
    modifier: Modifier = Modifier,
    carouselState: CarouselState,
    carouselScrollEnabled: Boolean,
    carouselFocusRequester: FocusRequester,
    firstLazyRowItemUnderCarouselRequester: FocusRequester,
) {

    var isCarouselFocused by remember { mutableStateOf(false) }
    val itemsPerPage = remember(tvShows.itemSnapshotList) {
        when {
            tvShows.itemCount == 0 -> 5 // Default fallback
            tvShows.itemSnapshotList.size <= 5 -> tvShows.itemSnapshotList.size
            else -> minOf(5, tvShows.itemSnapshotList.size) // Cap at 5 for UI consistency
        }
    }
    val activeItemIndex = carouselState.activeItemIndex
    val totalItems = tvShows.itemCount
    val currentPage = activeItemIndex / itemsPerPage
    val startIndex = currentPage * itemsPerPage

    Carousel(
        itemCount = tvShows.itemCount,
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
            val tvShow = tvShows[idx] ?: return@Carousel


            LaunchedEffect(tvShow) {
                setSelectedTvShow(tvShow)
            }

            val isActiveItem = idx == activeItemIndex

            TvShowCarouselItemForeground(
                tvShow = tvShow,
                onMoreInfoClick = {
                    goToMoreInfo(tvShow)
                },
                isCarouselFocused = isCarouselFocused && isActiveItem,
                modifier = Modifier
                    .align(Alignment.BottomStart),
            )

        }
    }
}
