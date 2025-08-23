package com.google.wiltv.presentation.screens.moviedetails

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import com.google.wiltv.R
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.util.StringConstants
import com.google.wiltv.presentation.common.MoviesRow
import com.google.wiltv.presentation.screens.movies.MovieDetails
import com.google.wiltv.presentation.screens.movies.TitleValueText
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MovieDetailTabs(
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
    selectedMovie: MovieNew,
    similarMovies: StateFlow<PagingData<MovieNew>>,
    refreshScreenWithNewMovie: (MovieNew) -> Unit,
    episodesTabFocusRequester: FocusRequester,
    suggestedTabFocusRequester: FocusRequester,
    detailsTabFocusRequester: FocusRequester,
    playButtonFocusRequester: FocusRequester,
    onTabsFocusChanged: (Boolean) -> Unit
) {
//    val tabs = listOf("Episodes", "Suggested", "Details")
    val tabs = listOf("Suggested", "Details")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val focusRequesters =
        listOf(episodesTabFocusRequester, suggestedTabFocusRequester, detailsTabFocusRequester)

    Column(modifier = modifier) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            contentColor = Color.White,
            containerColor = Color.Transparent // adjust to your theme
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier
                        .focusRequester(focusRequesters[index])
                        .focusProperties {
                            // Always allow UP navigation to PlayButton since it's now always in composition
                            up = playButtonFocusRequester
                            left =
                                if (index > 0) focusRequesters[index - 1] else FocusRequester.Default
                            right =
                                if (index < focusRequesters.size - 1) focusRequesters[index + 1] else FocusRequester.Default
                        }
                        .onFocusChanged { focusState ->
                            try {
                                if (focusState.hasFocus) {
                                    selectedTabIndex = index
                                    onTabsFocusChanged(true)
                                }
                            } catch (e: Exception) {
                                // Handle any focus-related exceptions gracefully
                                // In a real app, you might want to log this
                            }
                        }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> SuggestedTab(
                similarMovies = similarMovies,
                selectedMovie = selectedMovie,
                refreshScreenWithNewMovie = refreshScreenWithNewMovie,
                isFullScreen = isFullScreen
            )

            1 -> DetailsTab(selectedMovie = selectedMovie, isFullScreen = isFullScreen)
        }
    }
}

@Composable
fun EpisodesTab(isFullScreen: Boolean = false) {
    LazyColumn(
        modifier = if (isFullScreen)
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        else
            Modifier.height(400.dp) // Fixed height to prevent infinite constraints
    ) {
        item {
            Text("Season 1 - 6 Episodes", style = MaterialTheme.typography.bodyLarge)
        }
        items(6) { episodeIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.Gray) // placeholder for episode image
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Episode ${episodeIndex + 1} - Resurrection")
                    Text("Description if necessary")
                }
            }
        }
    }
}

@Composable
fun SuggestedTab(
    selectedMovie: MovieNew,
    similarMovies: StateFlow<PagingData<MovieNew>>,
    refreshScreenWithNewMovie: (MovieNew) -> Unit,
    isFullScreen: Boolean = false
) {
    Box(
        modifier = Modifier
            .height(400.dp)
            .padding(16.dp)
    ) {
        MoviesRow(
            title = StringConstants
                .Composable
                .movieDetailsScreenSimilarTo(selectedMovie.title),
            titleStyle = MaterialTheme.typography.titleMedium,
            similarMovies = similarMovies,
            onMovieSelected = refreshScreenWithNewMovie
        )
    }
}

@Composable
fun DetailsTab(
    selectedMovie: MovieNew,
    isFullScreen: Boolean = false
) {
    Column(
        modifier = if (isFullScreen)
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        else
            Modifier.padding(16.dp)
    ) {
        if (isFullScreen) {
            Text(
                text = "Movie Details",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Extract year from release date
        val year = selectedMovie.releaseDate?.split("-")?.firstOrNull() ?: "Unknown"
        Text("Year: $year", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Format duration from minutes to hours and minutes
        val duration = selectedMovie.duration?.let { minutes ->
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            "${hours}h ${remainingMinutes}m"
        } ?: "Unknown"
        Text(
            "Duration: $duration",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Display genres
        val genres = selectedMovie.genres.joinToString(", ") { it.name }
        Text(
            "Genres: ${genres.ifEmpty { "Unknown" }}",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )

        // Show additional details if available
        selectedMovie.imdbRating?.let { rating ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "IMDB Rating: $rating",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        selectedMovie.contentRating?.let { contentRating ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Content Rating: $contentRating",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isFullScreen) {
            Spacer(modifier = Modifier.height(16.dp))

            // Show languages if available
//            if (selectedMovie.languages.isNotEmpty()) {
//                val languages = selectedMovie.languages.joinToString(", ") { it.englishName }
//                Text(
//                    "Languages: $languages",
//                    color = Color.White,
//                    style = MaterialTheme.typography.bodySmall
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//
//            // Show countries if available
//            if (!selectedMovie.countries.isNullOrEmpty()) {
//                val countries = selectedMovie.countries.joinToString(", ") { it.englishName }
//                Text(
//                    "Countries: $countries",
//                    color = Color.White,
//                    style = MaterialTheme.typography.bodySmall
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//            }

            // Show plot if available
            selectedMovie.plot?.let { plot ->
                Text(
                    "Plot: $plot",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
