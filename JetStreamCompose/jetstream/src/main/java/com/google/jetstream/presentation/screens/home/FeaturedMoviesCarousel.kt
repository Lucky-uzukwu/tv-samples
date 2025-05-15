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

package com.google.jetstream.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselDefaults
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.Text
import co.touchlab.kermit.Logger
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.google.jetstream.R
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.network.MovieNew
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.theme.JetStreamBorderWidth
import com.google.jetstream.presentation.theme.JetStreamButtonShape
import com.google.jetstream.presentation.utils.Padding
import com.google.jetstream.presentation.utils.formatDuration
import com.google.jetstream.presentation.utils.handleDPadKeyEvents
import kotlin.math.roundToLong

@OptIn(ExperimentalTvMaterial3Api::class)
val CarouselSaver = Saver<CarouselState, Int>(
    save = { it.activeItemIndex },
    restore = { CarouselState(it) }
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedMoviesCarousel(
    movies: List<Movie>,
    moviesNew: List<MovieNew>,
    padding: Padding,
    goToVideoPlayer: (movie: Movie) -> Unit,
    goToMoreInfo: (movie: Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val carouselState = rememberSaveable(saver = CarouselSaver) { CarouselState(0) }
    var isCarouselFocused by remember { mutableStateOf(false) }
    var currentCarouselFocusedItemIndex by remember { mutableStateOf(0) }
    val watchNowButtonFocusRequester = remember { FocusRequester() }
    val moreInfoButtonFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Safely request focus on Watch Now button when carousel gains focus
    LaunchedEffect(isCarouselFocused) {
        if (isCarouselFocused) {
            watchNowButtonFocusRequester.requestFocus()
        }
    }

    val alpha = if (isCarouselFocused) 1f else 0f

    Carousel(
        modifier = modifier
            .padding(start = padding.start, end = padding.start, top = padding.top)
            .border(
                width = JetStreamBorderWidth,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                shape = ShapeDefaults.Medium
            )
            .clip(ShapeDefaults.Medium)
            .onFocusChanged {
                isCarouselFocused = it.hasFocus
            }
            .semantics {
                contentDescription = StringConstants.Composable.ContentDescription.MoviesCarousel
            }
            .handleDPadKeyEvents(
                onEnter = {
//                    if (isCarouselFocused) {
//                        val currentMovie = movies[carouselState.activeItemIndex]
//                        if (currentCarouselFocusedItemIndex == 0) {
//                            goToVideoPlayer(currentMovie)
//                        } else {
//                            goToMoreInfo(currentMovie)
//                        }
//                    }
                },
                onLeft = { focusManager.moveFocus(FocusDirection.Left) },
                onRight = { focusManager.moveFocus(FocusDirection.Right) }
            ),
        itemCount = moviesNew.size,
        carouselState = carouselState,
        carouselIndicator = {
            CarouselIndicator(
                itemCount = moviesNew.size,
                activeItemIndex = carouselState.activeItemIndex
            )
        },
        contentTransformStartToEnd = fadeIn(tween(durationMillis = 1000))
            .togetherWith(fadeOut(tween(durationMillis = 1000))),
        contentTransformEndToStart = fadeIn(tween(durationMillis = 1000))
            .togetherWith(fadeOut(tween(durationMillis = 1000))),
        content = { index ->
            val movieNew = moviesNew[index]
            CarouselItemBackground(
                movie = movieNew,
                modifier = Modifier.fillMaxSize()
            )
            CarouselItemForeground(
                movie = movieNew,
                isCarouselFocused = isCarouselFocused,
                modifier = Modifier.fillMaxSize(),
                onWatchNowClick = { goToVideoPlayer(movies[index]) },
                onMoreInfoClick = { goToMoreInfo(movies[index]) },
                watchNowButtonFocusRequester = watchNowButtonFocusRequester,
                moreInfoButtonFocusRequester = moreInfoButtonFocusRequester,
                onButtonFocus = { buttonIndex ->
                    currentCarouselFocusedItemIndex = buttonIndex
                }
            )
        }
    )
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
            .padding(32.dp)
//            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            .graphicsLayer {
                clip = true
                shape = ShapeDefaults.ExtraSmall
            }
            .align(Alignment.BottomCenter)
    ) {
        CarouselDefaults.IndicatorRow(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            itemCount = itemCount,
            activeItemIndex = activeItemIndex,
        )
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
            Text(
                text = movie.title,
                style = MaterialTheme.typography.displayMedium.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(x = 2f, y = 4f),
                        blurRadius = 2f
                    )
                ),
                maxLines = 2
            )
            movie.tagLine?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.65f
                        ),
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(x = 2f, y = 4f),
                            blurRadius = 2f
                        )
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                text = "$getYear - $combinedGenre - ${movie.duration?.formatDuration()}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.65f
                    ),
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(x = 2f, y = 4f),
                        blurRadius = 2f
                    )
                ),
                maxLines = 1,
                modifier = Modifier.padding(top = 8.dp)
            )

            val plotWords = movie.plot?.split(" ") ?: emptyList()
            val formattedPlot = plotWords.chunked(9).joinToString("\n") { chunk ->
                // Ensure the second line (and subsequent lines) are not more than 9 words
                if (chunk.size > 9) chunk.take(9).joinToString(" ") + "..."
                else chunk.joinToString(" ")
            }.let {
                // Ensure the ellipsis is added correctly if the original plot was truncated.
                // and the original plot had more words than what's displayed (2 lines * 9 words = 18 words approx)
                // and the current formatted plot doesn't already end with an ellipsis
                if (plotWords.size > 18 && !it.endsWith("...")) {
                    // Trim potentially added newlines if ellipsis is added at the end of everything
                    it.trimEnd() + "..."
                } else it
            }

            Text(
                text = formattedPlot,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.65f
                    ),
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(x = 2f, y = 4f),
                        blurRadius = 2f
                    )
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "${
                    movie.imdbRating?.toDouble()?.roundToLong()
                }/10 - ${movie.imdbVotes} IMDB Votes",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.65f
                    ),
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(x = 2f, y = 4f),
                        blurRadius = 2f
                    )
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )

            AnimatedVisibility(
                visible = isCarouselFocused,
                content = {
                    Row(modifier = Modifier.padding(top = 16.dp)) {
                        WatchNowButton(
                            onClick = onWatchNowClick,
                            focusRequester = watchNowButtonFocusRequester,
                            moreInfoButtonFocusRequester = moreInfoButtonFocusRequester,
                            onFocus = { onButtonFocus(0) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
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
    moreInfoButtonFocusRequester: FocusRequester
) {
    val focusManager = LocalFocusManager.current

    Button(
        onClick = onClick,
        modifier = Modifier
            .focusRequester(focusRequester)
            .handleDPadKeyEvents(
                onRight = {
                    focusManager.moveFocus(FocusDirection.Right)
                    moreInfoButtonFocusRequester.requestFocus()
                }
            ),
//            .onFocusChanged { if (it.isFocused) onFocus() },
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = JetStreamButtonShape),
        colors = ButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
            .onFocusChanged { if (it.isFocused) onFocus() },
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = JetStreamButtonShape),
        colors = ButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.surface,
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
