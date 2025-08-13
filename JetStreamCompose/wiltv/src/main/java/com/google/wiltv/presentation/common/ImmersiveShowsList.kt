package com.google.wiltv.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.focus.focusProperties
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
import co.touchlab.kermit.Logger
import com.google.wiltv.R
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.presentation.theme.WilTvBorderWidth
import com.google.wiltv.presentation.theme.WilTvCardShape
import com.google.wiltv.presentation.utils.bringIntoViewIfChildrenAreFocused
import com.google.wiltv.presentation.utils.formatVotes
import com.google.wiltv.presentation.utils.getImdbRating

@Composable
fun ImmersiveShowsList(
    tvShows: LazyPagingItems<TvShow>,
    sectionTitle: String? = stringResource(R.string.top_10_movies_title),
    modifier: Modifier = Modifier,
    setSelectedTvShow: (TvShow) -> Unit,
    onTvShowClick: (tvShow: TvShow) -> Unit,
    lazyRowState: TvLazyListState? = null,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
    onItemFocused: (TvShow, Int) -> Unit = { _, _ -> },
    clearDetailsSignal: Boolean = false
) {
    var isListFocused by remember { mutableStateOf(false) }
    var shouldShowDetails by remember { mutableStateOf(false) }

    val selectedTvShow by remember {
        androidx.compose.runtime.derivedStateOf {
            tvShows.itemSnapshotList.firstOrNull()
        }
    }

    // Clear details when clearDetailsSignal is triggered
    LaunchedEffect(clearDetailsSignal) {
        if (clearDetailsSignal) {
            shouldShowDetails = false
        }
    }

    ImmersiveList(
        selectedTvShow = selectedTvShow ?: return,
        shouldShowDetails = shouldShowDetails,
        tvShows = tvShows,
        sectionTitle = sectionTitle,
        onTvShowClick = onTvShowClick,
        onTvShowFocused = { tvShow, index ->
            setSelectedTvShow(tvShow)
            onItemFocused(tvShow, index)
        },
        onFocusChanged = { focusState ->
            isListFocused = focusState.hasFocus
            // Show details when list is focused, and keep them visible even when focus moves elsewhere
            // (like to the sidebar), unless the user navigates to a completely different context
            if (focusState.hasFocus) {
                shouldShowDetails = true
            }
            // Don't immediately hide details when focus leaves - let them persist for sidebar navigation
        },
        lazyRowState = lazyRowState,
        focusRequesters = focusRequesters,
        modifier = modifier.bringIntoViewIfChildrenAreFocused(
            PaddingValues(bottom = 120.dp)
        )
    )

}

@Composable
private fun ImmersiveList(
    selectedTvShow: TvShow,
    shouldShowDetails: Boolean,
    tvShows: LazyPagingItems<TvShow>,
    sectionTitle: String?,
    onFocusChanged: (FocusState) -> Unit,
    onTvShowFocused: (TvShow, Int) -> Unit,
    onTvShowClick: (TvShow) -> Unit,
    modifier: Modifier = Modifier,
    lazyRowState: TvLazyListState? = null,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = modifier
    ) {
        Column {
            // TODO HERE you can add more deails for each row
            if (shouldShowDetails) {
                TvShowDescription(
                    tvShow = selectedTvShow,
                )
            }

            ImmersiveListShowsRow(
                tvShows = tvShows,
                itemDirection = ItemDirection.Horizontal,
                title = sectionTitle,
                showIndexOverImage = false,
                onShowSelected = onTvShowClick,
                onShowFocused = onTvShowFocused,
                modifier = Modifier.onFocusChanged(onFocusChanged),
                lazyRowState = lazyRowState,
                focusRequesters = focusRequesters,
            )
        }
    }
}

@Composable
private fun Background(
    movie: TvShow,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val imageUrl = movie.backdropImageUrl
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Crossfade(
            targetState = movie,
            label = "posterUriCrossfade",

            ) {
            it.title?.let { movieTitle ->
                imageUrl?.let { posterUrl ->
                    PosterImage(
                        title = movieTitle,
                        posterUrl = posterUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun TvShowDescription(
    tvShow: TvShow,
    modifier: Modifier = Modifier,
) {
    val combinedGenre = tvShow.genres?.joinToString(" ") { genre -> genre.name }
    val getYear = tvShow.releaseDate?.substring(0, 4)

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
                combinedGenre = combinedGenre ?: "",
                duration = tvShow.duration
            )
        }
        tvShow.title?.let {
            DisplayFilmTitle(
                title = it,
                style = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }

        tvShow.plot?.let {
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
                    tvShow.imdbRating.getImdbRating()
                }/10 - ${tvShow.imdbVotes.toString().formatVotes()} Votes",
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
fun ImmersiveListShowsRow(
    tvShows: LazyPagingItems<TvShow>,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    showIndexOverImage: Boolean = false,
    onShowSelected: (TvShow) -> Unit = {},
    onShowFocused: (TvShow, Int) -> Unit = { _, _ -> },
    lazyRowState: TvLazyListState? = null,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
) {
    // Create infinite list by repeating the movies
    val infiniteShowsCount = if (tvShows.itemCount > 0) Int.MAX_VALUE else 0

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
                count = tvShows.itemCount,
                key = { index ->
                    tvShows.peek(index)?.id ?: "tvshow_$index"
                },
                contentType = { "TvShowItem" }
            ) { index ->
                val tvShow = tvShows[index]

                // Skip if tvShow is null (can happen during paging updates)
                if (tvShow == null) return@items

                val focusRequester = focusRequesters[index]

                ShowsRowItem(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (focusRequester != null) Modifier.focusRequester(focusRequester)
                            else Modifier
                        ),
                    index = index,
                    itemDirection = itemDirection,
                    onTvShowSelected = onShowSelected,
                    onTvShowFocused = { tvShow -> onShowFocused(tvShow, index) },
                    tvShow = tvShow,
                    showIndexOverImage = showIndexOverImage,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ShowsRowItem(
    index: Int,
    tvShow: TvShow,
    onTvShowSelected: (TvShow) -> Unit,
    showIndexOverImage: Boolean,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    onTvShowFocused: (TvShow) -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }
    val imageUrl = tvShow.posterImageUrl

    MovieCard(
        onClick = { onTvShowSelected(tvShow) },
        modifier = Modifier
            .border(
                width = WilTvBorderWidth,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = WilTvCardShape
            )
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
fun rememberTvShowRowFocusRequesters(
    tvShows: LazyPagingItems<TvShow>?,
    rowIndex: Int,
    focusRequesters: MutableMap<Pair<Int, Int>, FocusRequester>,
    focusManagementConfig: FocusManagementConfig?
): Map<Int, FocusRequester> {
    return remember(tvShows?.itemCount, rowIndex) {
        if (tvShows == null || tvShows.itemCount == 0) {
            emptyMap()
        } else {
            val startTime = System.currentTimeMillis()

            // Use cached values to avoid multiple data access
            val itemCount = tvShows.itemCount
            val snapshotSize = tvShows.itemSnapshotList.items.size
            val actualItemCount = minOf(itemCount, snapshotSize)
            val maxFocusItems = focusManagementConfig?.maxFocusRequestersPerRow ?: 50
            val limitedItemCount = minOf(actualItemCount, maxFocusItems)

            // Simple range creation without expensive null checks
            // Focus requesters will be created lazily when actually needed
            val result = (0 until limitedItemCount).associate { index ->
                index to focusRequesters.getOrPut(Pair(rowIndex, index)) { FocusRequester() }
            }

            val endTime = System.currentTimeMillis()
            Logger.d { "TV Show Row $rowIndex focus requesters created in ${endTime - startTime}ms (${result.size} items)" }

            result
        }
    }
}
