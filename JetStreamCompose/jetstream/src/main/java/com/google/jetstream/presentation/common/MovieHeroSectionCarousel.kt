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
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.util.conditional
import com.google.jetstream.util.shadowBox


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieHeroSectionCarousel(
    movies: LazyPagingItems<MovieNew>,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    goToMoreInfo: (movie: MovieNew) -> Unit,
    setSelectedMovie: (MovieNew) -> Unit,
    modifier: Modifier = Modifier,
    carouselState: CarouselState,
) {
    var isCarouselFocused by remember { mutableStateOf(false) }

    Carousel(
        modifier = modifier
            .fillMaxSize()
            .padding(PaddingValues(horizontal = 32.dp))
            .conditional(
                isCarouselFocused,
                ifTrue = {
                    shadowBox(
                        color = Color(0x994B635B),
                        blurRadius = 40.dp,
                        offset = DpOffset(0.dp, 8.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                }
            )
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.border.copy(alpha = if (isCarouselFocused) 1f else 0f),
                shape = MaterialTheme.shapes.extraLarge,
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .onFocusChanged {
                isCarouselFocused = it.hasFocus
            },
        itemCount = movies.itemCount,
        carouselIndicator = {},
        carouselState = carouselState,
        contentTransformStartToEnd = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
        contentTransformEndToStart = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
    ) { idx ->
        Box(modifier = Modifier.fillMaxSize()) {
            val movie = movies[idx] ?: return@Carousel

            CarouselItemImage(
                movie = movie,
                modifier = Modifier
                    .fillMaxSize()
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 1f), // Adjust alpha for desired opacity
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = Float.POSITIVE_INFINITY // Or a specific width if needed
                        )
                    )
            )

            CarouselItemForeground(
                movie = movie,
                onWatchNowClick = {
                    goToVideoPlayer(movies.itemSnapshotList.items[idx])
                },
                isCarouselFocused = isCarouselFocused,
                modifier = Modifier
                    .align(Alignment.BottomStart),
            )
            setSelectedMovie(movie)
        }
    }
}