// ABOUTME: Watchlist screen displaying user's saved movies and TV shows in grid layout
// ABOUTME: Provides remove functionality and empty state handling for watchlist management
package com.google.wiltv.presentation.screens.watchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.common.MovieCard
import com.google.wiltv.presentation.common.PosterImage
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WatchlistScreen(
    onMovieClick: (com.google.wiltv.data.models.MovieNew) -> Unit = {},
    onTvShowClick: (com.google.wiltv.data.models.TvShow) -> Unit = {},
    viewModel: WatchlistScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val childPadding = rememberChildPadding()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is WatchlistScreenUiState.Success && currentState.watchlistItems.isNotEmpty()) {
            focusRequester.requestFocus()
        }
    }

    when (val state = uiState) {
        is WatchlistScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }
        
        is WatchlistScreenUiState.Empty -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your watchlist is empty\nAdd movies and TV shows from their detail pages",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        is WatchlistScreenUiState.Success -> {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(
                    start = childPadding.start + 28.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.watchlistItems,
                    key = { item -> "${item::class.simpleName}_${item.contentId}" }
                ) { item ->
                    when (item) {
                        is WatchlistContentItem.Movie -> {
                            MovieCard(
                                onClick = { onMovieClick(item.movie) },
                                modifier = Modifier
                                    .aspectRatio(1 / 1.5f)
                                    .padding(6.dp)
                                    .then(
                                        if (state.watchlistItems.indexOf(item) == 0) {
                                            Modifier.focusRequester(focusRequester)
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                item.movie.posterImageUrl?.let { imageUrl ->
                                    PosterImage(
                                        title = item.movie.title,
                                        posterUrl = imageUrl,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } ?: run {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.movie.title,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        is WatchlistContentItem.TvShow -> {
                            MovieCard(
                                onClick = { onTvShowClick(item.tvShow) },
                                modifier = Modifier
                                    .aspectRatio(1 / 1.5f)
                                    .padding(6.dp)
                                    .then(
                                        if (state.watchlistItems.indexOf(item) == 0) {
                                            Modifier.focusRequester(focusRequester)
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                item.tvShow.posterImageUrl?.let { imageUrl ->
                                    PosterImage(
                                        title = item.tvShow.title ?: "Unknown Title",
                                        posterUrl = imageUrl,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } ?: run {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.tvShow.title ?: "Unknown Title",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        is WatchlistScreenUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error loading watchlist\nPlease try again",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}