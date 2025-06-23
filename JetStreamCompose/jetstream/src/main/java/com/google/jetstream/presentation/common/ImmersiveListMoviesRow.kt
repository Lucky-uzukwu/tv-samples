package com.google.jetstream.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.utils.bringIntoViewIfChildrenAreFocused

@Composable
fun ImmersiveListMoviesRow(
    movies: LazyPagingItems<MovieNew>,
    sectionTitle: String? = stringResource(R.string.top_10_movies_title),
    modifier: Modifier = Modifier,
    setSelectedMovie: (MovieNew) -> Unit,
    gradientColor: Color = Color.Black.copy(alpha = 0.7f),
    onMovieClick: (movie: MovieNew) -> Unit
) {
    var isListFocused by remember { mutableStateOf(false) }

    var selectedMovie by remember(movies) {
        mutableStateOf(movies.itemSnapshotList.firstOrNull())
    }

    LaunchedEffect(Unit) {
        if (movies.itemSnapshotList.items.isNotEmpty()) {
            selectedMovie = movies.itemSnapshotList.items.first()
            setSelectedMovie(selectedMovie!!)
        }
    }

    ImmersiveList(
        selectedMovie = selectedMovie ?: return,
        isListFocused = isListFocused,
        gradientColor = gradientColor,
        movies = movies,
        sectionTitle = sectionTitle,
        onMovieClick = onMovieClick,
        onMovieFocused = {
            selectedMovie = it
            setSelectedMovie(it)
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
    selectedMovie: MovieNew,
    isListFocused: Boolean,
    gradientColor: Color,
    movies: LazyPagingItems<MovieNew>,
    sectionTitle: String?,
    onFocusChanged: (FocusState) -> Unit,
    onMovieFocused: (MovieNew) -> Unit,
    onMovieClick: (MovieNew) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = modifier
    ) {
        Background(
            movie = selectedMovie,
            visible = isListFocused,
            modifier = modifier
                .height(500.dp)
                .gradientOverlay(gradientColor)
        )
        Column {
            // TODO HERE you can add more details for each row
            if (isListFocused) {
                DisplayMovieDetails(
                    movie = selectedMovie
                )
            }

            ImmersiveListMoviesRowNew(
                movies = movies,
                itemDirection = ItemDirection.Horizontal,
                title = sectionTitle,
                showIndexOverImage = false,
                onMovieSelected = onMovieClick,
                onMovieFocused = onMovieFocused,
                modifier = modifier.onFocusChanged(onFocusChanged)
            )
        }
    }
}

@Composable
private fun Background(
    movie: MovieNew,
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
            PosterImage(
                movieTitle = it.title,
                movieUri = imageUrl,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun DisplayMovieDetails(
    movie: MovieNew,
    modifier: Modifier = Modifier,
) {
    val combinedGenre = movie.genres.joinToString(" ") { genre -> genre.name }
    val getYear = movie.releaseDate?.substring(0, 4)

    Column(
        modifier = modifier.padding(
            start = 10.dp
        ),
    ) {
        Text(
            modifier = Modifier.padding(top = 64.dp),
            text = movie.title,
            maxLines = 2,
            color = Color.White,
            fontWeight = FontWeight.W900,
            style = MaterialTheme.typography.displaySmall.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(x = 2f, y = 4f),
                    blurRadius = 2f
                )
            ),
        )
//        val formattedPlot = movie.plot.formatPLot()
//        DisplayFilmGenericText(formattedPlot, maxLines = 2)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
//            DisplayFilmExtraInfo(getYear, combinedGenre, movie.duration)
            Spacer(modifier = Modifier.width(8.dp))
//            DisplayFilmGenericText(
//                "${
//                    movie.imdbRating.getImdbRating()
//                }/10 - ${movie.imdbVotes.toString().formatVotes()} IMDB Votes"
//            )
            Spacer(modifier = Modifier.width(8.dp))
            IMDbLogo()
        }
        Spacer(modifier = Modifier.height(28.dp))
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
    endPadding: Dp = rememberChildPadding().end,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    showIndexOverImage: Boolean = false,
    onMovieSelected: (MovieNew) -> Unit = {},
    onMovieFocused: (MovieNew) -> Unit = {}
) {
    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }

    Column(
        modifier = modifier
            .padding(
                start = 3.dp
            )
            .focusGroup()
    ) {
        if (title != null) {
            Text(
                text = title,
                color = Color.White,
                style = titleStyle,
                modifier = Modifier
                    .alpha(1f)
                    .padding(vertical = 16.dp, horizontal = 9.dp)
            )
        }
        AnimatedContent(
            targetState = movies,
            label = "",
        ) { movieState ->
            LazyRow(
                contentPadding = PaddingValues(end = endPadding),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .focusRequester(lazyRow)
                    .focusRequester(firstItem)
            ) {
                itemsIndexed(
                    movieState.itemSnapshotList.items,
                    key = { _, movie ->
                        movie.id
                    }
                ) { index, movie ->
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
                        onMovieFocused = onMovieFocused,
                        movie = movie,
                        showIndexOverImage = showIndexOverImage
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImmersiveListMoviesRowNew(
    movies: LazyPagingItems<MovieNew>,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    endPadding: Dp = rememberChildPadding().end,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    showIndexOverImage: Boolean = false,
    onMovieSelected: (MovieNew) -> Unit = {},
    onMovieFocused: (MovieNew) -> Unit = {}
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            items(movies.itemSnapshotList.items.size) { index ->
                val movie = movies.itemSnapshotList.items[index]
                MoviesRowItem(
                    modifier = Modifier.weight(1f),
                    index = index,
                    itemDirection = itemDirection,
                    onMovieSelected = {
                        onMovieSelected(it)
                    },
                    onMovieFocused = onMovieFocused,
                    movie = movie,
                    showIndexOverImage = showIndexOverImage
                )
            }
        }

    }
}

