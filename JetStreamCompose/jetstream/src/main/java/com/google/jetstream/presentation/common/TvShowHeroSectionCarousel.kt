package com.google.jetstream.presentation.common

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.util.conditional
import com.google.jetstream.util.shadowBox


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvShowHeroSectionCarousel(
    tvShows: LazyPagingItems<TvShow>,
    goToMoreInfo: (tvShow: TvShow) -> Unit,
    setSelectedTvShow: (TvShow) -> Unit,
    modifier: Modifier = Modifier,
    carouselState: CarouselState,
    carouselScrollEnabled: Boolean,
) {

    var isCarouselFocused by remember { mutableStateOf(false) }
    val itemsPerPage = 5
    val activeItemIndex = carouselState.activeItemIndex
    val totalItems = tvShows.itemCount
    val currentPage = activeItemIndex / itemsPerPage
    val startIndex = currentPage * itemsPerPage
    val endIndex = minOf(startIndex + itemsPerPage, totalItems)

    Carousel(
        itemCount = tvShows.itemCount,
        modifier = modifier
            .onFocusChanged {
                isCarouselFocused = it.hasFocus
            },
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
                if (carouselScrollEnabled) {
                    setSelectedTvShow(tvShow)
                }
            }
            CarouselItemForeground(
                tvShow = tvShow,
                onMoreInfoClick = {
                    goToMoreInfo(tvShows.itemSnapshotList.items[idx])
                },
                isCarouselFocused = isCarouselFocused,
                modifier = Modifier
                    .align(Alignment.BottomStart),
            )

        }
    }
}
