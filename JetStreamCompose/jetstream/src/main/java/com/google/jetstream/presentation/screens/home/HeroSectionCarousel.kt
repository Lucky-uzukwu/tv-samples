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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
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
import coil.compose.AsyncImage
import com.google.jetstream.R
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.network.MovieNew
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.theme.JetStreamBorderWidth
import com.google.jetstream.presentation.theme.JetStreamButtonShape
import com.google.jetstream.presentation.theme.onPrimaryContainerLightHighContrast
import com.google.jetstream.presentation.theme.onPrimaryLight
import com.google.jetstream.presentation.utils.Padding
import com.google.jetstream.presentation.utils.formatDuration
import com.google.jetstream.presentation.utils.formatVotes
import com.google.jetstream.presentation.utils.handleDPadKeyEvents
import co.touchlab.kermit.Logger


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroSectionCarousel(
    movies: List<Movie>,
    moviesNew: LazyPagingItems<MovieNew>,
    padding: Padding,
    goToVideoPlayer: (movie: Movie) -> Unit,
    goToMoreInfo: (movie: Movie) -> Unit,
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
        derivedStateOf { minOf(startIndex + itemsPerPage - 1, moviesNew.itemCount - 1) }
    }
    val currentItemCount by remember {
        derivedStateOf { if (moviesNew.itemCount > 0) (endIndex - startIndex + 1) else 0 }
    }

    val watchNowButtonFocusRequester = remember { FocusRequester() }
    val moreInfoButtonFocusRequester = remember { FocusRequester() }

    Box(modifier = modifier) {
        Carousel(
            modifier = Modifier
//                .padding(top = padding.top)
//                .border(
//                    width = JetStreamBorderWidth,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isCarouselFocused) 1f else 0f),
//                    shape = ShapeDefaults.Medium
//                )
//                .clip(ShapeDefaults.Medium)
                .onFocusChanged { isCarouselFocused = it.hasFocus }
                .semantics {
                    contentDescription =
                        StringConstants.Composable.ContentDescription.MoviesCarousel
                }
                .handleDPadKeyEvents(
                    onRight = {
                        if (currentMovieIndex < currentItemCount - 1) {
                            currentMovieIndex += 1
                        } else if (startIndex + itemsPerPage < moviesNew.itemCount) {
                            currentPage += 1
                            currentMovieIndex += 1
                            // Trigger pagination safely
                            moviesNew.loadState.append is LoadState.NotLoading || moviesNew.get(
                                startIndex + itemsPerPage
                            ) != null
                        }
                    },
                    onLeft = {
                        Logger.i("onLeft triggered")
                        if (currentMovieIndex > 0 && currentPage == 0) {
                            currentMovieIndex -= 1
                        } else {
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
                    onDown = { isWatchNowFocused = true }
                ),
            itemCount = currentItemCount,
            carouselIndicator = {
                CarouselIndicator(
                    itemCount = currentItemCount,
                    activeItemIndex = currentMovieIndex,
                )
            },
            autoScrollDurationMillis = 5000,
            contentTransformStartToEnd = fadeIn(tween(durationMillis = 1000))
                .togetherWith(fadeOut(tween(durationMillis = 1000))),
            contentTransformEndToStart = fadeIn(tween(durationMillis = 1000))
                .togetherWith(fadeOut(tween(durationMillis = 1000))),
            content = { idx ->
                val movieIndex = startIndex + idx
                val movieNew = moviesNew[movieIndex]

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
                            movies.getOrNull(movieIndex)?.let { goToVideoPlayer(it) }
                        },
                        onMoreInfoClick = {
                            movies.getOrNull(movieIndex)?.let { goToMoreInfo(it) }
                        },
                        watchNowButtonFocusRequester = watchNowButtonFocusRequester,
                        moreInfoButtonFocusRequester = moreInfoButtonFocusRequester,
                        onInnerElementRight = {
                            if (currentMovieIndex < currentItemCount - 1) {
                                currentMovieIndex += 1
                            } else if (startIndex + itemsPerPage < moviesNew.itemCount) {
                                currentPage += 1
                                currentMovieIndex = 0
                                moviesNew.loadState.append is LoadState.NotLoading || moviesNew.get(
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

private fun getNewMovieItemIndex(
    currentItemIndex: Int,
    moviesNew: LazyPagingItems<MovieNew>
): Int {
    var currentItemIndex1 = currentItemIndex
    if (currentItemIndex1 < moviesNew.itemCount - 1) {
        currentItemIndex1 += 1
        moviesNew[currentItemIndex1] // This helps to get new movies
    } else if (currentItemIndex1 == moviesNew.itemCount - 1) {
        moviesNew[currentItemIndex1]
    }
    return currentItemIndex1
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
            .padding(top = 64.dp)
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
                activeItemIndex = getListBPosition(activeItemIndex).position
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
            CarouselMovieTitle(movie)
            CarouselMovieDescription(movie)
            CarouselMovieExtraInfo(getYear, combinedGenre, movie)

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


            CarouselMovieGenericText(formattedPlot)
            Spacer(modifier = Modifier.height(10.dp))

            Row {
                IMDbLogo()
                Spacer(modifier = Modifier.width(8.dp))
                CarouselMovieGenericText(
                    "${
                        getMovieRating(movie)
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
private fun getMovieRating(movie: MovieNew): String? {
    return if (movie.imdbRating?.length!! > 3) {
        movie.imdbRating.substring(0, 3)
    } else {
        movie.imdbRating
    }
}

@Composable
private fun CarouselMovieGenericText(text: String) {

    Text(
        text = text,
        color = onPrimaryLight,
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
}

@Composable
private fun CarouselMovieExtraInfo(
    getYear: String?,
    combinedGenre: String,
    movie: MovieNew
) {
    Text(
        text = "$getYear - $combinedGenre - ${movie.duration?.formatDuration()}",
        color = onPrimaryLight,
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

@Composable
private fun CarouselMovieDescription(movie: MovieNew) {
    movie.tagLine?.let {
        Text(
            text = it,
            color = onPrimaryLight,
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
}

@Composable
private fun CarouselMovieTitle(movie: MovieNew) {
    Text(
        text = movie.title,
        color = onPrimaryLight,
        style = MaterialTheme.typography.displaySmall.copy(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.5f),
                offset = Offset(x = 2f, y = 4f),
                blurRadius = 2f
            )
        ),
        maxLines = 2
    )
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
            focusedContainerColor = onPrimaryContainerLightHighContrast,
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
fun IMDbLogo(
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFF111827), // Tailwind's text-gray-950
    backgroundColor: Color = Color(0xFFFBBF24) // Tailwind's bg-yellow-500
) {

    Text(
        text = "IMDb",
        modifier = modifier
            .background(backgroundColor)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        color = textColor,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold
        )
    )
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
            focusedContainerColor = onPrimaryContainerLightHighContrast,
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

data class ListBPosition(val page: Int, val position: Int)

fun getListBPosition(listAIndex: Int, pageSize: Int = 5): ListBPosition {
    require(listAIndex >= 0) { "listAIndex must be non-negative" }
    val page = listAIndex / pageSize
    val position = listAIndex % pageSize
    return ListBPosition(page, position)
}