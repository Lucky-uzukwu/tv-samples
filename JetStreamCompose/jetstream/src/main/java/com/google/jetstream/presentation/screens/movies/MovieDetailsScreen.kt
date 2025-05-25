/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.presentation.screens.movies

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.entities.Movie
import com.google.jetstream.data.network.MovieNew
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MoviesRow
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import kotlinx.coroutines.flow.StateFlow

object MovieDetailsScreen {
    const val MovieIdBundleKey = "movieId"
}

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
    val childPadding = rememberChildPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        MovieImageWithGradients(
            movie = selectedMovie,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
    }


    BackHandler(onBack = onBackPressed)
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = modifier,
    ) {
        item {
            MovieDetails(
                selectedMovie = selectedMovie,
                openVideoPlayer = openVideoPlayer
            )
        }

        item {
            CastAndCrewList(
                castAndCrew = selectedMovie.moviePeople
            )
        }
        item {
            MoviesRow(
                title = StringConstants
                    .Composable
                    .movieDetailsScreenSimilarTo(selectedMovie.title),
                titleStyle = MaterialTheme.typography.titleMedium,
                similarMovies = similarMovies,
                onMovieSelected = refreshScreenWithNewMovie
            )
        }

//        item {
//            MovieReviews(
//                modifier = Modifier.padding(top = childPadding.top),
//                reviewsAndRatings = selectedMovie.getImdbRating()
//            )
//        }

        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = childPadding.start)
                    .padding(BottomDividerPadding)
                    .fillMaxWidth()
                    .height(1.dp)
                    .alpha(0.15f)
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = childPadding.start),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val itemModifier = Modifier.width(192.dp)

                TitleValueText(
                    modifier = itemModifier,
                    title = stringResource(R.string.status),
                    value = "Released",
                    valueColor = Color.White
                )
                TitleValueText(
                    modifier = itemModifier,
                    title = stringResource(R.string.original_language),
                    value = selectedMovie.languages.first().englishName,
                    valueColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun MovieImageWithGradients(
    movie: MovieNew,
    modifier: Modifier = Modifier,
) {
    val imageUrl = "https://stage.nortv.xyz/" + "storage/" + movie.backdropImagePath
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(imageUrl)
            .crossfade(true).build(),
        contentDescription = StringConstants
            .Composable
            .ContentDescription
            .moviePoster(movie.title), modifier = modifier
    )
}


private val BottomDividerPadding = PaddingValues(vertical = 48.dp)
