// ABOUTME: Continue Watching row components with progress indicators based on ImmersiveListMoviesRow
// ABOUTME: Provides specialized movie row components for displaying watch progress and resume functionality

package com.google.wiltv.presentation.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.models.ContinueWatchingItem
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.presentation.theme.WilTvBorderWidth
import com.google.wiltv.presentation.theme.WilTvCardShape
import com.google.wiltv.presentation.utils.formatVotes
import com.google.wiltv.presentation.utils.getImdbRating

@Composable
fun ContinueWatchingRow(
    continueWatchingItems: List<ContinueWatchingItem>,
    sectionTitle: String = "Continue Watching",
    modifier: Modifier = Modifier,
    setSelectedMovie: (MovieNew) -> Unit,
    onMovieClick: (movie: MovieNew) -> Unit,
    lazyRowState: TvLazyListState? = null,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
    onItemFocused: (MovieNew, Int) -> Unit = { _, _ -> },
    clearDetailsSignal: Boolean = false,
    watchlistItemIds: Set<String> = emptySet()
) {
    var isListFocused by remember { mutableStateOf(false) }
    var shouldShowDetails by remember { mutableStateOf(false) }

    val selectedMovie by remember {
        derivedStateOf {
            continueWatchingItems.firstOrNull()?.movie
        }
    }

    // Clear details when clearDetailsSignal is triggered
    LaunchedEffect(clearDetailsSignal) {
        if (clearDetailsSignal) {
            shouldShowDetails = false
        }
    }

    selectedMovie?.let { movie ->
        ContinueWatchingImmersiveList(
            selectedMovie = movie,
            shouldShowDetails = shouldShowDetails,
            continueWatchingItems = continueWatchingItems,
            sectionTitle = sectionTitle,
            onMovieClick = onMovieClick,
            onMovieFocused = { movieItem, index ->
                setSelectedMovie(movieItem)
                onItemFocused(movieItem, index)
            },
            lazyRowState = lazyRowState,
            focusRequesters = focusRequesters,
            onFocusChanged = { focusState ->
                isListFocused = focusState.hasFocus
                if (focusState.hasFocus) {
                    shouldShowDetails = true
                }
            },
            watchlistItemIds = watchlistItemIds,
            modifier = modifier
        )
    }
}

@Composable
private fun ContinueWatchingImmersiveList(
    selectedMovie: MovieNew,
    shouldShowDetails: Boolean,
    continueWatchingItems: List<ContinueWatchingItem>,
    sectionTitle: String?,
    onFocusChanged: (androidx.compose.ui.focus.FocusState) -> Unit,
    onMovieFocused: (MovieNew, Int) -> Unit,
    onMovieClick: (MovieNew) -> Unit,
    modifier: Modifier = Modifier,
    lazyRowState: TvLazyListState? = null,
    watchlistItemIds: Set<String> = emptySet(),
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = modifier
    ) {
        Column {
            if (shouldShowDetails) {
                ContinueWatchingMovieDetails(
                    movie = selectedMovie
                )
            }

            ContinueWatchingMoviesRow(
                continueWatchingItems = continueWatchingItems,
                itemDirection = ItemDirection.Horizontal,
                title = sectionTitle,
                showIndexOverImage = false,
                onMovieSelected = onMovieClick,
                onMovieFocused = onMovieFocused,
                lazyRowState = lazyRowState,
                focusRequesters = focusRequesters,
                watchlistItemIds = watchlistItemIds,
                modifier = modifier.onFocusChanged(onFocusChanged)
            )
        }
    }
}

@Composable
private fun ContinueWatchingMovieDetails(
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
                getYear = getYear ?: "",
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ContinueWatchingMoviesRow(
    continueWatchingItems: List<ContinueWatchingItem>,
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
    watchlistItemIds: Set<String> = emptySet()
) {
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
            items(
                count = continueWatchingItems.size,
                key = { index -> 
                    continueWatchingItems[index].movie.id
                },
                contentType = { "ContinueWatchingMovieItem" }
            ) { index ->
                val continueWatchingItem = continueWatchingItems[index]
                val movie = continueWatchingItem.movie
                val focusRequester = focusRequesters[index]

                ContinueWatchingMovieCard(
                    movie = movie,
                    progressPercentage = continueWatchingItem.progressPercentage,
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
                    showIndexOverImage = showIndexOverImage,
                    isInWatchlist = watchlistItemIds.contains(movie.id.toString())
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ContinueWatchingMovieCard(
    index: Int,
    movie: MovieNew,
    progressPercentage: Float,
    onMovieSelected: (MovieNew) -> Unit,
    showIndexOverImage: Boolean,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    onMovieFocused: (MovieNew) -> Unit = {},
    isInWatchlist: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }
    val imageUrl = movie.posterImageUrl

    // Match the exact structure of MoviesRowItem
    MovieCard(
        onClick = { onMovieSelected(movie) },
        isInWatchlist = isInWatchlist,
        modifier = Modifier
            .border(
                width = WilTvBorderWidth,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = WilTvCardShape
            )
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
        // Use Box to overlay progress indicator
        Box {
            MoviesRowItemImage(
                modifier = Modifier.aspectRatio(itemDirection.aspectRatio),
                showIndexOverImage = showIndexOverImage,
                movieTitle = movie.title,
                movieUri = imageUrl,
                index = index
            )
            
            // Progress indicator at bottom of the card
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progressPercentage },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color.Red,
                trackColor = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}