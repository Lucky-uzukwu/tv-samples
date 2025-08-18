package com.google.wiltv.presentation.screens.moviedetails

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.Person
import com.google.wiltv.presentation.common.AuthenticatedAsyncImage
import com.google.wiltv.presentation.common.Error
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.screens.movies.MovieDetails
import com.google.wiltv.data.util.StringConstants
import kotlinx.coroutines.flow.StateFlow

object MovieDetailsScreen {
    const val MovieIdBundleKey = "movieId"
}

data class PersonToCharacter(
    val person: Person,
    val character: String?,
)

@Composable
fun MovieDetailsScreen(
    openVideoPlayer: (movieId: String) -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (MovieNew) -> Unit,
    movieDetailsScreenViewModel: MovieDetailsScreenViewModel = hiltViewModel(),
) {
    val uiState by movieDetailsScreenViewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is MovieDetailsScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is MovieDetailsScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize())
        }

        is MovieDetailsScreenUiState.Done -> {
            Details(
                selectedMovie = s.movie,
                similarMovies = s.similarMovies,
                openVideoPlayer = openVideoPlayer,
                onBackPressed = onBackPressed,
                refreshScreenWithNewMovie = refreshScreenWithNewMovie,
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            )
        }
    }
}

@Composable
private fun Details(
    selectedMovie: MovieNew,
    similarMovies: StateFlow<PagingData<MovieNew>>,
    openVideoPlayer: (movieId: String) -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (MovieNew) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        MovieImageWithGradients(
            movie = selectedMovie,
            modifier = Modifier.fillMaxSize()
        )
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
    }

    BackHandler(onBack = onBackPressed)
    
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = modifier,
        state = lazyListState,
        userScrollEnabled = true
    ) {
        // Sticky header for movie title - always visible
        stickyHeader {
            val childPadding = rememberChildPadding()
            Text(
                text = selectedMovie.title,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(start = childPadding.start, top = 20.dp, bottom = 10.dp)
                    .focusable(),
                maxLines = 1
            )
        }
        
        item {
            MovieDetails(
                openVideoPlayer = openVideoPlayer,
                id = selectedMovie.id,
                title = null, // Title now in sticky header
                tagLine = selectedMovie.tagLine,
                releaseDate = selectedMovie.releaseDate,
                countries = selectedMovie.countries,
                genres = selectedMovie.genres,
                duration = selectedMovie.duration,
                plot = selectedMovie.plot,
                imdbRating = selectedMovie.imdbRating,
                imdbVotes = selectedMovie.imdbVotes,
                streamingProviders = selectedMovie.streamingProviders,
                video = selectedMovie.video
            )
        }
        
        item {
            MovieDetailTabs()
        }
    }
}

@Composable
private fun MovieImageWithGradients(
    movie: MovieNew,
    modifier: Modifier = Modifier,
) {
    val imageUrl = movie.backdropImageUrl
    AuthenticatedAsyncImage(
        model = imageUrl,
        contentDescription = StringConstants
            .Composable
            .ContentDescription
            .moviePoster(movie.title),
        modifier = modifier
    )
}