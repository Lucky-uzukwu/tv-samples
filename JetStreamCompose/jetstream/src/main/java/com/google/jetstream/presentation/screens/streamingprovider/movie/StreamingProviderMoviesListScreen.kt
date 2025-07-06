package com.google.jetstream.presentation.screens.streamingprovider.movie

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MovieCard
import com.google.jetstream.presentation.common.PosterImage
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.JetStreamBottomListPadding
import com.google.jetstream.presentation.utils.focusOnInitialVisibility
import kotlinx.coroutines.flow.StateFlow


object StreamingProviderMoviesListScreen {
    const val StreamingProviderIdBundleKey = "streamingProviderId"
}

@Composable
fun StreamingProviderMoviesListScreen(
    onBackPressed: () -> Unit,
    onMovieSelected: (MovieNew) -> Unit,
    streamingProviderMoviesListScreenViewModel: StreamingProviderMoviesListScreenViewModel = hiltViewModel()
) {
    val uiState by streamingProviderMoviesListScreenViewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        StreamingProviderMoviesListScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        StreamingProviderMoviesListScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize())
        }

        is StreamingProviderMoviesListScreenUiState.Done -> {
            val moviesPagingData = s.movies
            MoviesGrid(
                streamingProviderName = s.streamingProviderName,
                movies = moviesPagingData,
                onBackPressed = onBackPressed,
                onMovieSelected = onMovieSelected
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MoviesGrid(
    streamingProviderName: String,
    movies: StateFlow<PagingData<MovieNew>>,
    onBackPressed: () -> Unit,
    onMovieSelected: (MovieNew) -> Unit,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()
    val isFirstItemVisible = remember { mutableStateOf(false) }

    BackHandler(onBack = onBackPressed)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = streamingProviderName,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(
                vertical = childPadding.top.times(3.5f)
            )
        )
        AnimatedContent(
            targetState = movies,
            label = "",
        ) { state ->
            val movieList = state.collectAsLazyPagingItems().itemSnapshotList.items
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(bottom = JetStreamBottomListPadding)
            ) {
                itemsIndexed(movieList, key = { _, movie -> movie.id }) { index, movie ->
                    MovieCard(
                        onClick = { onMovieSelected(movie) },
                        modifier = Modifier
                            .aspectRatio(1 / 1.5f)
                            .padding(8.dp)
                            .then(
                                if (index == 0)
                                    Modifier.focusOnInitialVisibility(isFirstItemVisible)
                                else Modifier
                            ),
                    ) {
                        val imageUrl =
                            "https://stage.nortv.xyz/" + "storage/" + movie.posterImagePath
                        PosterImage(
                            title = movie.title,
                            posterUrl = imageUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

        }
    }
}
