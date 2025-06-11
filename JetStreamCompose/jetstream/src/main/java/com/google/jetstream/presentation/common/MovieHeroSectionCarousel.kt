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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.Text
import co.touchlab.kermit.Logger
import coil.compose.AsyncImage
import com.google.jetstream.R
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.theme.JetStreamButtonShape
import com.google.jetstream.presentation.utils.fadingEdge
import com.google.jetstream.presentation.utils.formatPLot
import com.google.jetstream.presentation.utils.formatVotes
import com.google.jetstream.presentation.utils.getImdbRating
import com.google.jetstream.presentation.utils.getListBPosition
import com.google.jetstream.presentation.utils.handleDPadKeyEvents
import md_theme_light_onPrimaryContainer


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
                    CarouselItemBackground(
                        movie = movieNew,
                        modifier = Modifier.fillMaxSize()
                    )
                    CarouselItemForeground(
                        movie = movieNew,
                        isCarouselFocused = isCarouselFocused,
                        modifier = Modifier.fillMaxSize(),
                        onWatchNowClick = {
                            movies.itemSnapshotList.items.get(movieIndex)
                                .let { goToVideoPlayer(it) }
                        },
                        onMoreInfoClick = {
                            movies.itemSnapshotList.items.get(movieIndex).let { goToMoreInfo(it) }
                        },
                        watchNowButtonFocusRequester = watchNowButtonFocusRequester,
                        moreInfoButtonFocusRequester = moreInfoButtonFocusRequester,
                        onInnerElementRight = {
                            if (currentMovieIndex < currentItemCount - 1) {
                                currentMovieIndex += 1
                            } else if (startIndex + itemsPerPage < movies.itemCount) {
                                currentPage += 1
                                // Reset currentMovieIndex to 0 when moving to a new page
                                currentMovieIndex = 0
                                movies.loadState.append is LoadState.NotLoading || movies.get(
                                    startIndex + itemsPerPage
                                ) != null
                            }
                        },
                        onButtonFocus = { /* Handle button focus if needed */ }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun BoxScope.CarouselIndicator(
    itemCount: Int,
    activeItemIndex: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(bottom = 64.dp)
            .graphicsLayer {
                clip = true
                shape = ShapeDefaults.ExtraSmall
            }
            .align(Alignment.BottomCenter)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Indicator Row
            CarouselDefaults.IndicatorRow(
                itemCount = itemCount,
                activeItemIndex = getListBPosition(activeItemIndex).position,
                indicator =
                    { isActive ->
                        val activeColor = Color.White.copy(alpha = 1f) // Increased whiteness
                        val inactiveColor = activeColor.copy(alpha = 0.3f)
                        Box(
                            modifier =
                                Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (isActive) activeColor else inactiveColor,
                                        shape = CircleShape,
                                    ),
                        )
                    }
            )
        }
    }
}

@Composable
private fun CarouselItemForeground(
    movie: MovieNew,
    modifier: Modifier = Modifier,
    isCarouselFocused: Boolean = false,
    onWatchNowClick: () -> Unit,
    onMoreInfoClick: () -> Unit,
    watchNowButtonFocusRequester: FocusRequester,
    moreInfoButtonFocusRequester: FocusRequester,
    onInnerElementRight: () -> Unit,
    onButtonFocus: (Int) -> Unit
) {

    val combinedGenre = movie.genres.joinToString(" ") { genre -> genre.name }
    val getYear = movie.releaseDate?.substring(0, 4)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Top
        ) {
            DisplayFilmTitle(movie.title)
            DisplayFilmDescription(
                tagLine = movie.tagLine,
            )
            DisplayFilmExtraInfo(getYear, combinedGenre, movie.duration)

            val formattedPlot = movie.plot.formatPLot()


            DisplayFilmGenericText(formattedPlot)
            Spacer(modifier = Modifier.height(10.dp))

            Row {
                IMDbLogo()
                Spacer(modifier = Modifier.width(8.dp))
                DisplayFilmGenericText(
                    "${
                        movie.imdbRating.getImdbRating()
                    }/10 - ${movie.imdbVotes.toString().formatVotes()} IMDB Votes"
                )
            }

            AnimatedVisibility(
                visible = isCarouselFocused,
                content = {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        WatchNowButton(
                            onClick = onWatchNowClick,
                            focusRequester = watchNowButtonFocusRequester,
                            moreInfoButtonFocusRequester = moreInfoButtonFocusRequester,
                            onFocus = { onButtonFocus(0) },
                            onRight = onInnerElementRight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MoreInfoButton(
                            onClick = onMoreInfoClick,
                            focusRequester = moreInfoButtonFocusRequester,
                            onFocus = { onButtonFocus(1) })
                    }
                }
            )
        }
    }
}

@Composable
private fun CarouselItemBackground(
    movie: MovieNew,
    modifier: Modifier = Modifier
) {
    val imageUrl = "https://stage.nortv.xyz/" + "storage/" + movie.backdropImagePath

    AsyncImage(
        model = imageUrl,
        contentDescription = StringConstants
            .Composable
            .ContentDescription
            .moviePoster(movie.title),
        modifier = modifier
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = size.width * 0.8f // Stretch the gradient to 80% of the width
                    )
                )
            },
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun WatchNowButton(
    onClick: () -> Unit,
    focusRequester: FocusRequester,
    onFocus: () -> Unit,
    moreInfoButtonFocusRequester: FocusRequester,
    onRight: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onFocus() }
            .handleDPadKeyEvents(
                onRight = {
                    onRight
                },
                onDown = {
                    moreInfoButtonFocusRequester.requestFocus()
                }
            ),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = JetStreamButtonShape),
        colors = ButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            focusedContainerColor = md_theme_light_onPrimaryContainer,
            contentColor = MaterialTheme.colorScheme.surface,
            focusedContentColor = MaterialTheme.colorScheme.surface,
        ),
        scale = ButtonDefaults.scale(scale = 1f)
    ) {
        Icon(
            imageVector = Icons.Outlined.PlayArrow,
            contentDescription = null,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.watch_now),
            style = MaterialTheme.typography.titleSmall
        )
    }
}


@Composable
private fun MoreInfoButton(
    onClick: () -> Unit,
    focusRequester: FocusRequester,
    onFocus: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onFocus() }
            .handleDPadKeyEvents(
                onRight = {}
            ),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = JetStreamButtonShape),
        colors = ButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = md_theme_light_onPrimaryContainer,
            focusedContentColor = MaterialTheme.colorScheme.surface,
        ),
        scale = ButtonDefaults.scale(scale = 1f)
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "More info",
            style = MaterialTheme.typography.titleSmall
        )
    }
}