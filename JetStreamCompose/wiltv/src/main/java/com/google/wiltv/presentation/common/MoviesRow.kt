package com.google.wiltv.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.theme.WilTvBorderWidth
import com.google.wiltv.presentation.theme.WilTvCardShape
import kotlinx.coroutines.flow.StateFlow

enum class ItemDirection(val aspectRatio: Float) {
    Vertical(10.5f / 16f),
    Horizontal(16f / 9f);
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MoviesRow(
    similarMovies: StateFlow<PagingData<MovieNew>>,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    startPadding: Dp = rememberChildPadding().start,
    endPadding: Dp = rememberChildPadding().end,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp
    ),
    showIndexOverImage: Boolean = false,
    onMovieSelected: (movie: MovieNew) -> Unit = {},
    watchlistItemIds: Set<String> = emptySet()
) {
    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }

    Column(
        modifier = modifier.focusGroup()
    ) {
        if (title != null) {
            Text(
                text = title,
                style = titleStyle,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .alpha(1f)
                    .padding(start = startPadding, top = 16.dp, bottom = 16.dp)
            )
        }
        AnimatedContent(
            targetState = similarMovies,
            label = "",
        ) { movieState ->
            val similarMoviesAsLazyItems = movieState.collectAsLazyPagingItems()
            val similarMovies = similarMoviesAsLazyItems.itemSnapshotList.items
            LazyRow(
                contentPadding = PaddingValues(
                    start = startPadding,
                    end = endPadding,
                ),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .focusRequester(lazyRow)
                    .focusRequester(firstItem)
            ) {
                itemsIndexed(similarMovies, key = { _, movie -> movie.id }) { index, movie ->
                    val itemModifier = if (index == 0) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }
                    MoviesRowItem(
                        modifier = itemModifier.weight(1f),
                        index = index,
                        itemDirection = itemDirection,
                        onMovieSelected = {
                            lazyRow.saveFocusedChild()
                            onMovieSelected(it)
                        },
                        movie = movie,
                        showIndexOverImage = showIndexOverImage,
                        isInWatchlist = watchlistItemIds.contains(movie.id.toString())
                    )
                }
            }
        }
    }
}




@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MoviesRowItem(
    index: Int,
    movie: MovieNew,
    onMovieSelected: (MovieNew) -> Unit,
    showIndexOverImage: Boolean,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    onMovieFocused: (MovieNew) -> Unit = {},
    isInWatchlist: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }
    val imageUrl = movie.posterImageUrl

    MovieCard(
        onClick = { onMovieSelected(movie) },
        isInWatchlist = isInWatchlist,
        modifier = Modifier
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onMovieFocused(movie)
                }
            }
            .focusProperties {
                left = if (index == 0) {
                    FocusRequester.Default
                } else {
                    FocusRequester.Default
                }
                down = FocusRequester.Default
            }
            .then(modifier)
    ) {
        MoviesRowItemImage(
            modifier = Modifier.aspectRatio(itemDirection.aspectRatio),
            showIndexOverImage = showIndexOverImage,
            movieTitle = movie.title,
            movieUri = imageUrl,
            index = index
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TvShowRowItem(
    index: Int,
    tvShow: TvShow,
    onTvShowSelected: (TvShow) -> Unit,
    showIndexOverImage: Boolean,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    onTvShowFocused: (TvShow) -> Unit = {},
    downFocusRequester: FocusRequester? = null,
    isInWatchlist: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }
    val imageUrl = tvShow.posterImageUrl

    MovieCard(
        onClick = { onTvShowSelected(tvShow) },
        isInWatchlist = isInWatchlist,
        modifier = Modifier
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onTvShowFocused(tvShow)
                }
            }
            .focusProperties {
                left = if (index == 0) {
                    FocusRequester.Default
                } else {
                    FocusRequester.Default
                }
                down = FocusRequester.Default
            }
            .then(modifier)
    ) {
        tvShow.title?.let {
            MoviesRowItemImage(
                modifier = Modifier.aspectRatio(itemDirection.aspectRatio),
                showIndexOverImage = showIndexOverImage,
                movieTitle = it,
                movieUri = imageUrl,
                index = index
            )
        }
    }
}


@Composable
fun MoviesRowItemImage(
    movieTitle: String,
    movieUri: String?,
    showIndexOverImage: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
) {
    Box(contentAlignment = Alignment.CenterStart) {
        movieUri?.let {
            PosterImage(
                title = movieTitle,
                posterUrl = it,
                modifier = modifier
                    .height(198.dp)
                    .width(150.dp)
                    .drawWithContent {
                        drawContent()
                        if (showIndexOverImage) {
                            drawRect(
                                color = Color.Black.copy(
                                    alpha = 0.1f
                                )
                            )
                        }
                    },
            )
        }
        if (showIndexOverImage) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "#${index.inc()}",
                style = MaterialTheme.typography.displayLarge
                    .copy(
                        shadow = Shadow(
                            offset = Offset(0.5f, 0.5f),
                            blurRadius = 5f
                        ),
                        color = Color.White
                    ),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MoviesRowItemText(
    showItemTitle: Boolean,
    isItemFocused: Boolean,
    movieTitle: String,
    modifier: Modifier = Modifier
) {
    if (showItemTitle) {
        val movieNameAlpha by animateFloatAsState(
            targetValue = if (isItemFocused) 1f else 0f,
            label = "",
        )
        Text(
            text = if (movieTitle.length > 14) movieTitle.take(14) + "..." else movieTitle,
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            modifier = modifier
                .alpha(movieNameAlpha)
                .fillMaxWidth()
                .padding(top = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
