package com.google.wiltv.presentation.screens.search

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Log
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.wiltv.R
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.presentation.common.MovieCard
import com.google.wiltv.presentation.common.PosterImage
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.theme.WilTvBottomListPadding
import com.google.wiltv.presentation.theme.WilTvCardShape
import kotlinx.coroutines.flow.StateFlow


data class TargetState(
    val movies: StateFlow<PagingData<MovieNew>>? = null,
    val shows: StateFlow<PagingData<TvShow>>? = null
)

@Composable
fun SearchScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    onShowClick: (show: TvShow) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel(),
) {
    val searchState by searchScreenViewModel.searchState.collectAsStateWithLifecycle()

    when (val s = searchState) {
        is SearchState.Searching -> {
            Text(text = "Searching...")
        }

        is SearchState.Error -> {
            ErrorScreen(
                uiText = s.uiText,
                onRetry = { /* Could retry search */ },
                modifier = Modifier.fillMaxSize()
            )
        }

        is SearchState.Done -> {
            SearchResult(
                movies = s.movies,
                shows = s.shows,
                searchMovies = searchScreenViewModel::query,
                onMovieClick = onMovieClick,
                onShowClick = onShowClick
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchResult(
    movies: StateFlow<PagingData<MovieNew>>?,
    shows: StateFlow<PagingData<TvShow>>?,
    searchMovies: (queryString: String) -> Unit,
    onMovieClick: (movie: MovieNew) -> Unit,
    onShowClick: (show: TvShow) -> Unit,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()
    var searchQuery by remember { mutableStateOf("") }
    var lastSearchedQuery by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    val tfFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val tfInteractionSource = remember { MutableInteractionSource() }

    val isTfFocused by tfInteractionSource.collectIsFocusedAsState()

    val movieItems = movies?.collectAsLazyPagingItems()
    val showItems = shows?.collectAsLazyPagingItems()

    val isMoviesEmpty = movieItems?.itemCount == 0
    val isShowsEmpty = showItems?.itemCount == 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Search field at the top
        Surface(
            shape = ClickableSurfaceDefaults.shape(shape = WilTvCardShape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                pressedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                focusedContentColor = MaterialTheme.colorScheme.onSurface,
                pressedContentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(
                        width = if (isTfFocused) 2.dp else 1.dp,
                        color = animateColorAsState(
                            targetValue = if (isTfFocused) MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 1f
                            )
                            else MaterialTheme.colorScheme.border,
                            label = ""
                        ).value
                    ),
                    shape = WilTvCardShape
                )
            ),
            tonalElevation = 2.dp,
            modifier = Modifier
                .padding(horizontal = childPadding.start)
                .padding(start = 28.dp)
                .padding(top = 18.dp),
            onClick = { tfFocusRequester.requestFocus() }
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { updatedQuery -> searchQuery = updatedQuery },
                decorationBox = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .padding(start = 20.dp),
                    ) {
                        it()
                        if (searchQuery.isEmpty()) {
                            Text(
                                modifier = Modifier.graphicsLayer { alpha = 0.6f },
                                text = stringResource(R.string.search_screen_et_placeholder),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 4.dp,
                        horizontal = 8.dp
                    )
                    .focusRequester(tfFocusRequester)
                    .onKeyEvent {
                        if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                            when (it.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_DOWN -> {
                                    focusManager.moveFocus(FocusDirection.Down)
                                }

                                KeyEvent.KEYCODE_DPAD_UP -> {
                                    focusManager.moveFocus(FocusDirection.Up)
                                }

                                KeyEvent.KEYCODE_BACK -> {
                                    focusManager.moveFocus(FocusDirection.Exit)
                                }
                            }
                        }
                        true
                    },
                cursorBrush = Brush.verticalGradient(
                    colors = listOf(
                        LocalContentColor.current,
                        LocalContentColor.current,
                    )
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotEmpty()) {
                            Log.d("SearchScreen", "Initiating search for query: '$searchQuery'")
                            lastSearchedQuery = searchQuery
                            hasSearched = true
                            searchMovies(searchQuery)
                        }
                    }
                ),
                maxLines = 1,
                interactionSource = tfInteractionSource,
                textStyle = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Search results display - using GenreTvChannelsListScreen pattern
        if (!hasSearched) {
            // Show empty state or instructions
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enter a search term and press Enter",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Handle loading states properly
            val moviesLoadState = movieItems?.loadState?.refresh
            val showsLoadState = showItems?.loadState?.refresh

            // Log detailed state information
            Log.d("SearchScreen", "=== Search State Debug ===")
            Log.d("SearchScreen", "Query: '$lastSearchedQuery'")
            Log.d("SearchScreen", "Movies LoadState: $moviesLoadState")
            Log.d("SearchScreen", "Shows LoadState: $showsLoadState")
            Log.d("SearchScreen", "Movies ItemCount: ${movieItems?.itemCount}")
            Log.d("SearchScreen", "Shows ItemCount: ${showItems?.itemCount}")
            Log.d("SearchScreen", "Movies Empty: $isMoviesEmpty, Shows Empty: $isShowsEmpty")

            when {
                moviesLoadState is LoadState.Loading ||
                        showsLoadState is LoadState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Searching...",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                moviesLoadState is LoadState.Error &&
                        showsLoadState is LoadState.Error -> {
                    // Only show error when BOTH fail
                    val movieError = moviesLoadState.error.message
                    val showError = showsLoadState.error.message
                    Log.e(
                        "SearchScreen",
                        "Both searches failed - Movies: $movieError, Shows: $showError"
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading search results:\nMovies: ${movieError ?: "Unknown error"}\nShows: ${showError ?: "Unknown error"}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                else -> {
                    // Handle success and partial success cases
                    val moviesReady = moviesLoadState is LoadState.NotLoading
                    val showsReady = showsLoadState is LoadState.NotLoading
                    val moviesSuccess = moviesReady && !isMoviesEmpty
                    val showsSuccess = showsReady && !isShowsEmpty

                    if ((moviesReady && showsReady) && (isMoviesEmpty && isShowsEmpty)) {
                        // Show no results found only when both are loaded and both empty
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No result found for \"$lastSearchedQuery\"",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else if (moviesSuccess || showsSuccess) {
                        // Show results grid using GenreTvChannelsListScreen pattern
                        LazyVerticalGrid(
                            modifier = Modifier.fillMaxSize(),
                            columns = GridCells.Fixed(5),
                            contentPadding = PaddingValues(
                                start = childPadding.start + 28.dp,
                                end = 16.dp,
                                bottom = WilTvBottomListPadding
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Movies using proper paging pattern
                            movieItems?.let { movies ->
                                items(
                                    count = movies.itemCount,
                                    key = { index ->
                                        movies[index]?.id?.let { "movie_$it" }
                                            ?: "movie_loading_$index"
                                    }
                                ) { index ->
                                    val movie = movies[index]
                                    if (movie != null) {
                                        MovieCard(
                                            onClick = { onMovieClick(movie) },
                                            modifier = Modifier
                                                .aspectRatio(1 / 1.5f)
                                                .padding(6.dp),
                                        ) {

                                            if (movie.posterImageUrl == null) {
                                                run {
                                                    Log.w(
                                                        "SearchScreen",
                                                        "No imageUrl for movie ${movie.title} - showing fallback"
                                                    )
                                                    // Fallback when no image URL
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = movie.title,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            } else {
                                                val imageUrl = movie.posterImageUrl
                                                imageUrl.let {
                                                    Log.d(
                                                        "SearchScreen",
                                                        "Loading PosterImage for ${movie.title}"
                                                    )
                                                    PosterImage(
                                                        title = movie.title,
                                                        posterUrl = it,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // TV Shows using proper paging pattern
                            showItems?.let { shows ->
                                Log.d(
                                    "SearchScreen",
                                    "Adding ${shows.itemCount} show items to grid"
                                )
                                items(
                                    count = shows.itemCount,
                                    key = { index ->
                                        shows[index]?.id?.let { "show_$it" }
                                            ?: "show_loading_$index"
                                    }
                                ) { index ->
                                    val show = shows[index]
                                    Log.d("SearchScreen", "Rendering show $index: ${show?.title}")
                                    if (show != null) {
                                        MovieCard(
                                            onClick = { onShowClick(show) },
                                            modifier = Modifier
                                                .aspectRatio(1 / 1.5f)
                                                .padding(6.dp),
                                        ) {

                                            if (show.posterImageUrl == null) {
                                                run {
                                                    Log.w(
                                                        "SearchScreen",
                                                        "No imageUrl for show ${show.title} - showing fallback"
                                                    )
                                                    // Fallback when no image URL
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = show.title ?: "-",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            } else {
                                                val imageUrl = show.posterImageUrl
                                                imageUrl.let {
                                                    Log.d(
                                                        "SearchScreen",
                                                        "Loading PosterImage for ${show.title}"
                                                    )
                                                    PosterImage(
                                                        title = show.title ?: "-",
                                                        posterUrl = it,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d("SearchScreen", ">>> NO CONDITION MATCHED - THIS IS THE ISSUE!")
                        Log.d("SearchScreen", "MoviesReady: $moviesReady, ShowsReady: $showsReady")
                        Log.d(
                            "SearchScreen",
                            "MoviesSuccess: $moviesSuccess, ShowsSuccess: $showsSuccess"
                        )
                        Log.d(
                            "SearchScreen",
                            "IsMoviesEmpty: $isMoviesEmpty, IsShowsEmpty: $isShowsEmpty"
                        )
                    }
                }
            }
        }
    }
}
