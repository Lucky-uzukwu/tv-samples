package com.google.jetstream.presentation.common

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
import androidx.compose.foundation.layout.height
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
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.presentation.theme.JetStreamBorderWidth
import com.google.jetstream.presentation.theme.JetStreamCardShape
import com.google.jetstream.presentation.utils.bringIntoViewIfChildrenAreFocused
import com.google.jetstream.presentation.utils.formatPLot
import com.google.jetstream.presentation.utils.formatVotes
import com.google.jetstream.presentation.utils.getImdbRating

@Composable
fun ImmersiveShowsList(
    tvShows: LazyPagingItems<TvShow>,
    sectionTitle: String? = stringResource(R.string.top_10_movies_title),
    modifier: Modifier = Modifier,
    setSelectedTvShow: (TvShow) -> Unit,
    gradientColor: Color = Color.Black.copy(alpha = 0.7f),
    onTvShowClick: (tvShow: TvShow) -> Unit
) {
    var isListFocused by remember { mutableStateOf(false) }

    var selectedTvShow by remember(tvShows) {
        mutableStateOf(tvShows.itemSnapshotList.firstOrNull())
    }

    LaunchedEffect(Unit) {
        if (tvShows.itemSnapshotList.items.isNotEmpty()) {
            selectedTvShow = tvShows.itemSnapshotList.items.first()
            setSelectedTvShow(selectedTvShow!!)
        }
    }


    ImmersiveList(
        selectedTvShow = selectedTvShow ?: return,
        isListFocused = isListFocused,
        gradientColor = gradientColor,
        tvShows = tvShows,
        sectionTitle = sectionTitle,
        onMovieClick = onTvShowClick,
        onMovieFocused = {
            selectedTvShow = it
            setSelectedTvShow(it)
        },
        onFocusChanged = {
            isListFocused = it.hasFocus
        },
        modifier = modifier.bringIntoViewIfChildrenAreFocused(
            PaddingValues(bottom = 50.dp)
        )
    )

}

@Composable
private fun ImmersiveList(
    selectedTvShow: TvShow,
    isListFocused: Boolean,
    gradientColor: Color,
    tvShows: LazyPagingItems<TvShow>,
    sectionTitle: String?,
    onFocusChanged: (FocusState) -> Unit,
    onMovieFocused: (TvShow) -> Unit,
    onMovieClick: (TvShow) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = modifier
    ) {
        Background(
            movie = selectedTvShow,
            visible = isListFocused,
            modifier = modifier
                .height(500.dp)
                .padding(horizontal = 10.dp)
                .gradientOverlay(gradientColor)
        )
        Column {
            // TODO HERE you can add more deails for each row
            if (isListFocused) {
                TvShowDescription(
                    tvShow = selectedTvShow,
                )
            }

            ImmersiveListShowsRow(
                tvShows = tvShows,
                itemDirection = ItemDirection.Horizontal,
                title = sectionTitle,
                showIndexOverImage = false,
                onMovieSelected = onMovieClick,
                onMovieFocused = onMovieFocused,
                modifier = Modifier.onFocusChanged(onFocusChanged)
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
    val imageUrl = "https://stage.nortv.xyz/" + "storage/" + movie.backdropImagePath
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
                PosterImage(
                    movieTitle = movieTitle,
                    movieUri = imageUrl,
                    modifier = Modifier.fillMaxSize()
                )
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
                getYear = getYear,
                combinedGenre = combinedGenre,
                duration = tvShow.duration
            )
        }
        tvShow.title?.let {
            DisplayFilmTitle(
                title = it,
                style = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 2
            )
        }
        val formattedPlot = tvShow.plot.formatPLot()
        DisplayFilmGenericText(
            modifier = Modifier.padding(top = 4.dp),
            text = formattedPlot,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 3
        )
        Row(
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp)
        ) {
            DisplayFilmGenericText(
                text = "${
                    tvShow.imdbRating.getImdbRating()
                }/10 - ${tvShow.imdbVotes.toString().formatVotes()} IMDB Votes",
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
    titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp
    ),
    showIndexOverImage: Boolean = false,
    onMovieSelected: (TvShow) -> Unit = {},
    onMovieFocused: (TvShow) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
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
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            items(tvShows.itemSnapshotList.items.size) { index ->
                val tvShow = tvShows.itemSnapshotList.items[index]
                ShowsRowItem(
                    modifier = Modifier.weight(1f),
                    index = index,
                    itemDirection = itemDirection,
                    onTvShowSelected = {
                        onMovieSelected(it)
                    },
                    onTvShowFocused = onMovieFocused,
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
    val imageUrl = "https://stage.nortv.xyz/" + "storage/" + tvShow.posterImagePath

    MovieCard(
        onClick = { onTvShowSelected(tvShow) },
        modifier = Modifier
            .border(
                width = JetStreamBorderWidth,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = JetStreamCardShape
            )
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onTvShowFocused(tvShow)
                }
            }
            .focusProperties {
                left = if (index == 0) {
                    FocusRequester.Cancel
                } else {
                    FocusRequester.Default
                }
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
