package com.google.wiltv.presentation.screens.moviedetails

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.Person
import com.google.wiltv.data.util.StringConstants
import com.google.wiltv.presentation.common.AuthenticatedAsyncImage
import com.google.wiltv.presentation.common.Error
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.screens.movies.MovieDetails
import com.google.wiltv.presentation.screens.movies.MovieLargeTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

    // Ensure title and play button are visible when screen loads
    LaunchedEffect(selectedMovie.id) {
        lazyListState.scrollToItem(0)
    }

    // Focus management state
    val playButtonFocusRequester = remember { FocusRequester() }
    val episodesTabFocusRequester = remember { FocusRequester() }
    val suggestedTabFocusRequester = remember { FocusRequester() }
    val detailsTabFocusRequester = remember { FocusRequester() }

    BackHandler(onBack = onBackPressed)

    Box(modifier = Modifier.fillMaxSize()) {
        // Normal layout - always exists in composition but may be hidden
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(1f)
        ) {
            // Background image
            MovieImageWithGradients(
                movie = selectedMovie,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Content overlay
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp)
            ) {

                item {
                    MovieDetails(
                        openVideoPlayer = openVideoPlayer,
                        id = selectedMovie.id,
                        title = selectedMovie.title,
                        releaseDate = selectedMovie.releaseDate,
                        genres = selectedMovie.genres,
                        duration = selectedMovie.duration,
                        plot = selectedMovie.plot,
                        streamingProviders = selectedMovie.streamingProviders,
                        video = selectedMovie.video,
                        playButtonFocusRequester = playButtonFocusRequester,
                        episodesTabFocusRequester = episodesTabFocusRequester,
                        onPlayButtonFocused = { }
                    )
                }

                item {
                    MovieDetailTabs(
                        modifier = Modifier.fillMaxSize(),
                        isFullScreen = true,
                        episodesTabFocusRequester = episodesTabFocusRequester,
                        suggestedTabFocusRequester = suggestedTabFocusRequester,
                        detailsTabFocusRequester = detailsTabFocusRequester,
                        playButtonFocusRequester = playButtonFocusRequester,
                        onTabsFocusChanged = { focused -> },
                        selectedMovie = selectedMovie,
                        similarMovies = similarMovies,
                        refreshScreenWithNewMovie = refreshScreenWithNewMovie
                    )
                }
            }
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