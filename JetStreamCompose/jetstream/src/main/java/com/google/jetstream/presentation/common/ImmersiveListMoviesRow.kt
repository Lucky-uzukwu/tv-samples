package com.google.jetstream.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusState
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
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.presentation.utils.bringIntoViewIfChildrenAreFocused
import com.google.jetstream.presentation.utils.formatVotes
import com.google.jetstream.presentation.utils.getImdbRating

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
            PaddingValues(bottom = 110.dp)
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
//        Background(
//            movie = selectedMovie,
//            visible = isListFocused,
//            modifier = modifier
//                .height(500.dp)
//                .fillMaxWidth()
//                .gradientOverlay(gradientColor)
//        )
        Column {
            // TODO HERE you can add more details for each row
            if (isListFocused) {
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
    val imageUrl = movie.backdropImageUrl
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 200),
            initialOffsetY = { -it }
        ) + fadeIn(animationSpec = tween(durationMillis = 200)) + expandVertically(
            animationSpec = tween(durationMillis = 200)
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 10)) + shrinkVertically(
            animationSpec = tween(durationMillis = 10)
        ),
        modifier = modifier
    ) {
        Crossfade(
            targetState = movie,
            label = "posterUriCrossfade",

            ) {
            imageUrl?.let { posterUrl ->
                PosterImage(
                    title = it.title,
                    posterUrl = posterUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
    onMovieFocused: (MovieNew) -> Unit = {}
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
            horizontalArrangement = Arrangement.spacedBy(5.dp),
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
//            items(
//                count = infiniteMovieCount,
//                key = { index ->
//                    val actualIndex = index % movies.itemCount
//                    movies[actualIndex]?.id ?: index
//                }
//            ) { index ->
//                val actualIndex = index % movies.itemCount
//                val movie = movies[actualIndex]
//                if (movie == null) {
//                    Spacer(modifier = Modifier.width(12.dp))
//                    return@items
//                }
//                MoviesRowItem(
//                    modifier = Modifier.weight(1f),
//                    index = index,
//                    itemDirection = itemDirection,
//                    onMovieSelected = {
//                        onMovieSelected(it)
//                    },
//                    onMovieFocused = onMovieFocused,
//                    movie = movie,
//                    showIndexOverImage = showIndexOverImage
//                )
//            }
        }

    }
}

