package com.google.wiltv.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.R
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.presentation.utils.bringIntoViewIfChildrenAreFocused
import com.google.wiltv.presentation.utils.formatVotes
import com.google.wiltv.presentation.utils.getImdbRating

@Composable
fun ImmersiveListMoviesRow(
    movies: LazyPagingItems<MovieNew>,
    sectionTitle: String? = stringResource(R.string.top_10_movies_title),
    modifier: Modifier = Modifier,
    setSelectedMovie: (MovieNew) -> Unit,
    onMovieClick: (movie: MovieNew) -> Unit,
    lazyRowState: TvLazyListState? = null,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
    onItemFocused: (MovieNew, Int) -> Unit = { _, _ -> },
    clearDetailsSignal: Boolean = false
) {
    var isListFocused by remember { mutableStateOf(false) }
    var shouldShowDetails by remember { mutableStateOf(false) }

    var selectedMovie by remember(movies) {
        mutableStateOf(movies.itemSnapshotList.firstOrNull())
    }

    // Clear details when clearDetailsSignal is triggered
    LaunchedEffect(clearDetailsSignal) {
        if (clearDetailsSignal) {
            shouldShowDetails = false
        }
    }

    ImmersiveList(
        selectedMovie = selectedMovie ?: return,
        shouldShowDetails = shouldShowDetails,
        movies = movies,
        sectionTitle = sectionTitle,
        onMovieClick = onMovieClick,
        onMovieFocused = { movie, index ->
            selectedMovie = movie
            setSelectedMovie(movie)
            onItemFocused(movie, index)
        },
        lazyRowState = lazyRowState,
        focusRequesters = focusRequesters,
        onFocusChanged = { focusState ->
            isListFocused = focusState.hasFocus
            // Show details when list is focused, and keep them visible even when focus moves elsewhere
            // (like to the sidebar), unless the user navigates to a completely different context
            if (focusState.hasFocus) {
                shouldShowDetails = true
            }
            // Don't immediately hide details when focus leaves - let them persist for sidebar navigation
        },
        modifier = modifier.bringIntoViewIfChildrenAreFocused(
            PaddingValues(bottom = 120.dp)
        )
    )

}

@Composable
private fun ImmersiveList(
    selectedMovie: MovieNew,
    shouldShowDetails: Boolean,
    movies: LazyPagingItems<MovieNew>,
    sectionTitle: String?,
    onFocusChanged: (FocusState) -> Unit,
    onMovieFocused: (MovieNew, Int) -> Unit,
    onMovieClick: (MovieNew) -> Unit,
    modifier: Modifier = Modifier,
    lazyRowState: TvLazyListState? = null,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
) {

    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = modifier
    ) {
        Column {
            // TODO HERE you can add more details for each row
            if (shouldShowDetails) {
                DisplayMovieDetails(
                    movie = selectedMovie
                )
            }

            ImmersiveListMoviesRow(
                movies = movies,
                itemDirection = ItemDirection.Horizontal,
                title = sectionTitle,
                showIndexOverImage = false,
                onMovieSelected = onMovieClick,
                onMovieFocused = onMovieFocused,
                lazyRowState = lazyRowState,
                focusRequesters = focusRequesters,
                modifier = modifier.onFocusChanged(onFocusChanged)
            )
        }
    }
}

@Composable
private fun DisplayMovieDetails(
    movie: MovieNew,
    modifier: Modifier = Modifier,
) {
    val combinedGenre = movie.genres.take(2).joinToString(" · ") { genre -> genre.name }
    val getYear = movie.releaseDate?.substring(0, 4)

    Column(
        modifier = modifier
            .padding(horizontal = 34.dp)
            .width(360.dp),
    ) {
        Row(
            modifier = Modifier.padding(bottom = 5.dp),
        ) {
            DisplayFilmExtraInfo(
                getYear = getYear,
                combinedGenre = combinedGenre,
                duration = movie.duration
            )
        }
        DisplayFilmTitle(
            title = movie.title,
            style = MaterialTheme.typography.displaySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 1
        )
        movie.plot?.let {
            DisplayFilmGenericText(
                modifier = Modifier.padding(top = 4.dp),
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 3
            )
        }
        Row(
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp)
        ) {
            DisplayFilmGenericText(
                text = "${
                    movie.imdbRating.getImdbRating()
                }/10 - ${movie.imdbVotes.toString().formatVotes()} Votes",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.width(8.dp))
            IMDbLogo()
        }
    }
}

private fun Modifier.gradientOverlay(gradientColor: Color): Modifier =
    drawWithCache {
        val horizontalGradient = Brush.horizontalGradient(
            colors = listOf(
                gradientColor,
                Color.Transparent
            ),
            startX = size.width.times(0.2f),
            endX = size.width.times(0.7f)
        )
        val verticalGradient = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                gradientColor
            ),
            endY = size.width.times(0.3f)
        )
        val linearGradient = Brush.linearGradient(
            colors = listOf(
                gradientColor,
                Color.Transparent
            ),
            start = Offset(
                size.width.times(0.2f),
                size.height.times(0.5f)
            ),
            end = Offset(
                size.width.times(0.9f),
                0f
            )
        )

        onDrawWithContent {
            drawContent()
            drawRect(horizontalGradient)
            drawRect(verticalGradient)
            drawRect(linearGradient)
        }
    }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImmersiveListMoviesRow(
    movies: LazyPagingItems<MovieNew>,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    showIndexOverImage: Boolean = false,
    onMovieSelected: (MovieNew) -> Unit = {},
    onMovieFocused: (MovieNew, Int) -> Unit = { _, _ -> },
    lazyRowState: TvLazyListState? = null,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
) {
    // Create infinite list by repeating the movies
    val infiniteMovieCount = if (movies.itemCount > 0) Int.MAX_VALUE else 0

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                color = Color.White,
                style = titleStyle,
                modifier = Modifier
                    .padding(start = 32.dp)
                    .alpha(1f)
            )
        }

        TvLazyRow(
            modifier = modifier.fillMaxWidth(),
            state = lazyRowState ?: rememberTvLazyListState(),
            pivotOffsets = PivotOffsets(0.1f, 0f),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            // Use safe item count to prevent index out of bounds
            val safeItemCount = minOf(
                movies.itemSnapshotList.items.size,
                movies.itemCount.coerceAtLeast(0)
            )

            items(safeItemCount) { index ->
                val movie = movies.itemSnapshotList.items.getOrNull(index)

                // Skip if movie is null (can happen during paging updates)
                if (movie == null) return@items

                val focusRequester = focusRequesters[index]

                MoviesRowItem(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (focusRequester != null) Modifier.focusRequester(focusRequester)
                            else Modifier
                        ),
                    index = index,
                    itemDirection = itemDirection,
                    onMovieSelected = onMovieSelected,
                    onMovieFocused = { movie: MovieNew -> onMovieFocused(movie, index) },
                    movie = movie,
                    showIndexOverImage = showIndexOverImage,
                )
            }
        }

    }
}


