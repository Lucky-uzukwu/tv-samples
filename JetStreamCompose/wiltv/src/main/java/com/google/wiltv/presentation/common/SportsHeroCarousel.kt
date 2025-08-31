// ABOUTME: Hero carousel component for featured sports games
// ABOUTME: Displays live games with large team versus layouts and navigation controls


package com.google.wiltv.presentation.common

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.entities.CompetitionGame

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SportsHeroCarousel(
    games: LazyPagingItems<CompetitionGame>,
    onGameClick: (game: CompetitionGame) -> Unit,
    setSelectedGame: (CompetitionGame) -> Unit,
    modifier: Modifier = Modifier,
    carouselState: CarouselState,
    carouselScrollEnabled: Boolean,
    carouselFocusRequester: FocusRequester,
    firstLazyRowItemUnderCarouselRequester: FocusRequester
) {
    var isCarouselFocused by remember { mutableStateOf(false) }
    val itemsPerPage = 5
    val activeItemIndex = carouselState.activeItemIndex
    val totalItems = games.itemCount
    val currentPage = activeItemIndex / itemsPerPage
    val startIndex = currentPage * itemsPerPage

    Carousel(
        itemCount = games.itemCount,
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
                itemCount = minOf(itemsPerPage, totalItems - startIndex),
                activeItemIndex = activeItemIndex % itemsPerPage,
            )
        },
        carouselState = carouselState,
        contentTransformStartToEnd = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
        contentTransformEndToStart = fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))),
    ) { idx ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val game = games[idx] ?: return@Carousel

            LaunchedEffect(game) {
                if (carouselScrollEnabled) {
                    setSelectedGame(game)
                }
            }

            val isActiveItem = idx == activeItemIndex

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                GameHeroContent(
                    game = game,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                GameCarouselItemForeground(
                    game = game,
                    onWatchNowClick = {
                        onGameClick(game)
                    },
                    isCarouselFocused = isCarouselFocused && isActiveItem
                )
            }
        }
    }
}

@Composable
fun GameHeroContent(
    game: CompetitionGame,
    modifier: Modifier = Modifier
) {

    val patternUrl = sequenceOf(
        game.competition.logoUrl,
        game.competition.featuredImageUrl,
        game.competition.coverImageUrl
    ).firstOrNull { !it.isNullOrBlank() }
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        TeamVersusImage(
            game = game,
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight(),
            showLiveBadge = false
        )

        if (patternUrl.isNullOrBlank().not()) {
            CompetitionPatternBackground(
                logoUrl = patternUrl,
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun CompetitionPatternBackground(
    logoUrl: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            repeat(8) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    repeat(4) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .alpha(0.35f)
                                .height(40.dp)
                                .weight(1f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}