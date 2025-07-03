package com.google.jetstream.presentation.common

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.jetstream.data.entities.MovieEntity
import com.google.jetstream.data.entities.toMovieNew
import com.google.jetstream.data.models.MovieNew


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieHeroSectionCarousel(
    movies: LazyPagingItems<MovieEntity>,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    goToMoreInfo: (movie: MovieNew) -> Unit,
    setSelectedMovie: (MovieNew) -> Unit,
    modifier: Modifier = Modifier,
    carouselState: CarouselState,
    carouselScrollEnabled: Boolean,
) {
    var isCarouselFocused by remember { mutableStateOf(false) }
    val itemsPerPage = 5
    val activeItemIndex = carouselState.activeItemIndex
    val totalItems = movies.itemCount
    val currentPage = activeItemIndex / itemsPerPage
    val startIndex = currentPage * itemsPerPage
    val endIndex = minOf(startIndex + itemsPerPage, totalItems)


    Carousel(
        itemCount = movies.itemCount,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val movie = movies[idx]?.toMovieNew() ?: return@Carousel

            LaunchedEffect(movie) {
                if (carouselScrollEnabled) {
                    setSelectedMovie(movie)
                }
            }
            // Gradient overlay
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        brush = Brush.horizontalGradient(
//                            colors = listOf(
//                                Color.Black.copy(alpha = 0.1f), // Adjust alpha for desired opacity
//                                Color.Transparent
//                            ),
//                            startX = 0f,
//                            endX = Float.POSITIVE_INFINITY // Or a specific width if needed
//                        )
//                    )
//            )

            CarouselItemForeground(
                movie = movie,
                onWatchNowClick = {
                    goToVideoPlayer(movies.itemSnapshotList.items[idx].toMovieNew())
                },
                isCarouselFocused = isCarouselFocused,
                modifier = Modifier
                    .align(Alignment.BottomStart),
            )
        }
    }
}


