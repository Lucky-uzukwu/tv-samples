package com.google.wiltv.presentation.screens.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.SearchContent
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.common.MovieCard
import com.google.wiltv.presentation.common.PosterImage
import com.google.wiltv.presentation.common.SearchErrorSuggestions
import com.google.wiltv.presentation.common.SearchErrorType
import com.google.wiltv.presentation.common.SearchLoadingShimmer
import com.google.wiltv.presentation.common.SearchQueryDisplay
import com.google.wiltv.presentation.common.TvChannelCard
import com.google.wiltv.presentation.common.TvVirtualKeyboard
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.theme.WilTvBottomListPadding
import com.google.wiltv.presentation.utils.createInitialFocusRestorerModifiers
import com.google.wiltv.presentation.utils.focusOnInitialVisibility
import kotlinx.coroutines.flow.StateFlow


@Composable
fun SearchSectionHeader(
    title: String,
    count: Int? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        val headerText = if (count != null && count > 0) {
            "$title ($count)"
        } else {
            title
        }

        Text(
            text = headerText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Decorative line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun SearchScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    onShowClick: (show: TvShow) -> Unit,
    onChannelClick: (channel: TvChannel) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    onBrowseCategoriesClick: () -> Unit = {},
    onTrendingContentClick: () -> Unit = {},
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

        is SearchState.NetworkError -> {
            SearchErrorSuggestions(
                errorType = SearchErrorType.NetworkError,
                query = s.query,
                onBrowseCategoriesClick = onBrowseCategoriesClick,
                onTrendingContentClick = onTrendingContentClick,
                onRetryClick = { searchScreenViewModel.query(s.query) },
                modifier = Modifier.fillMaxSize()
            )
        }

        is SearchState.NoResults -> {
            SearchErrorSuggestions(
                errorType = SearchErrorType.NoResults,
                query = s.query,
                onBrowseCategoriesClick = onBrowseCategoriesClick,
                onTrendingContentClick = onTrendingContentClick,
                onRetryClick = { /* User needs to modify search */ },
                modifier = Modifier.fillMaxSize()
            )
        }

        is SearchState.QueryError -> {
            SearchErrorSuggestions(
                errorType = SearchErrorType.QueryError,
                query = s.query,
                suggestion = s.suggestion,
                onBrowseCategoriesClick = {},
                onTrendingContentClick = {},
                onRetryClick = { /* User needs to modify search */ },
                modifier = Modifier.fillMaxSize()
            )
        }

        is SearchState.Done -> {
            UnifiedSearchResult(
                content = s.content,
                searchMovies = searchScreenViewModel::query,
                onMovieClick = onMovieClick,
                onShowClick = onShowClick,
                onChannelClick = onChannelClick,
                searchScreenViewModel = searchScreenViewModel
            )
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UnifiedSearchResult(
    content: StateFlow<PagingData<SearchContent>>?,
    searchMovies: (queryString: String) -> Unit,
    onMovieClick: (movie: MovieNew) -> Unit,
    onShowClick: (show: TvShow) -> Unit,
    onChannelClick: (channel: TvChannel) -> Unit,
    searchScreenViewModel: SearchScreenViewModel,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()
    val gridState = rememberLazyGridState()
    val focusRestorerModifiers = createInitialFocusRestorerModifiers()
    val isGridItemVisible = remember { mutableStateOf(false) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var lastSearchedQuery by rememberSaveable { mutableStateOf("") }
    var hasSearched by rememberSaveable { mutableStateOf(false) }
    var lastFocusedIndex by rememberSaveable { mutableIntStateOf(0) }
    var isReturningFromNavigation by rememberSaveable { mutableStateOf(false) }

    val contentItems = content?.collectAsLazyPagingItems()

    val isEmpty = contentItems?.itemCount == 0

    // Split layout: Virtual keyboard on left, results on right
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 28.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Left side: Virtual Keyboard (40% width)
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            SearchQueryDisplay(
                query = searchQuery,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TvVirtualKeyboard(
                onKeyPress = { key ->
                    searchQuery += key
                },
                onClear = {
                    searchQuery = ""
                    hasSearched = false
                    isReturningFromNavigation = false
                },
                onDelete = {
                    if (searchQuery.isNotEmpty()) {
                        searchQuery = searchQuery.dropLast(1)
                    }
                },
                onSpace = {
                    searchQuery += " "
                },
                onEnter = {
                    if (searchQuery.isNotBlank()) {
                        val trimmedQuery = searchQuery.trim()
                        Log.d("UnifiedSearchScreen", "Search triggered: '$trimmedQuery'")
                        searchScreenViewModel.query(trimmedQuery)
                        lastSearchedQuery = trimmedQuery
                        hasSearched = true
                        isReturningFromNavigation = false
                    }
                },
                initialFocus = !isReturningFromNavigation && !hasSearched
            )
        }

        // Right side: Search results (60% width)
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            if (!hasSearched) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Use the virtual keyboard to search",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                val loadState = contentItems?.loadState?.refresh
                when {
                    loadState is LoadState.Loading -> {
                        SearchLoadingShimmer(
                            modifier = Modifier.fillMaxSize(),
                            childPadding = childPadding
                        )
                    }

                    loadState is LoadState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error loading search results: ${loadState.error.message}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    else -> {
                        if (!isEmpty && contentItems != null) {
                            LazyVerticalGrid(
                                state = gridState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(focusRestorerModifiers.parentModifier),
                                columns = GridCells.Fixed(3),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = WilTvBottomListPadding
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    count = contentItems.itemCount,
                                    key = { index ->
                                        val item = contentItems[index]
                                        when (item) {
                                            is SearchContent.MovieContent -> "movie_${item.movie.id}"
                                            is SearchContent.TvShowContent -> "show_${item.tvShow.id}"
                                            is SearchContent.TvChannelContent -> "channel_${item.tvChannel.id}"
                                            null -> "loading_$index"
                                        }
                                    }
                                ) { index ->
                                    val searchContent = contentItems[index]
                                    if (searchContent != null) {
                                        val itemModifier = Modifier
                                            .then(
                                                when {
                                                    index == lastFocusedIndex && isReturningFromNavigation ->
                                                        focusRestorerModifiers.childModifier.focusOnInitialVisibility(isGridItemVisible)
                                                    index == 0 && !isGridItemVisible.value ->
                                                        focusRestorerModifiers.childModifier.focusOnInitialVisibility(isGridItemVisible)
                                                    else -> Modifier
                                                }
                                            )
                                            .onFocusChanged { focusState ->
                                                if (focusState.hasFocus) {
                                                    lastFocusedIndex = index
                                                }
                                            }

                                        when (searchContent) {
                                            is SearchContent.MovieContent -> {
                                                MovieSearchContent(
                                                    searchContent = searchContent,
                                                    onMovieClick = { movie ->
                                                        isReturningFromNavigation = true
                                                        onMovieClick(movie)
                                                    },
                                                    modifier = itemModifier
                                                )
                                            }

                                            is SearchContent.TvShowContent -> {
                                                TvShowSearchContent(
                                                    searchContent = searchContent,
                                                    onShowClick = { show ->
                                                        isReturningFromNavigation = true
                                                        onShowClick(show)
                                                    },
                                                    modifier = itemModifier
                                                )
                                            }

                                            is SearchContent.TvChannelContent -> {
                                                TvChannelSearchConent(
                                                    searchContent = searchContent,
                                                    onChannelClick = { channel ->
                                                        isReturningFromNavigation = true
                                                        onChannelClick(channel)
                                                    },
                                                    modifier = itemModifier
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Handle focus restoration when returning from navigation
                            LaunchedEffect(isReturningFromNavigation, contentItems?.itemCount) {
                                if (isReturningFromNavigation && (contentItems?.itemCount ?: 0) > 0) {
                                    // Small delay to ensure UI is ready
                                    kotlinx.coroutines.delay(100)
                                    // Reset the flag - focus will be handled by focusRestorer
                                    isReturningFromNavigation = false
                                }
                            }
                        } else {
                            // Show empty state when no results found
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No results found for '$lastSearchedQuery'",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvChannelSearchConent(
    searchContent: SearchContent.TvChannelContent,
    onChannelClick: (TvChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    val channel = searchContent.tvChannel
    TvChannelCard(
        onClick = { onChannelClick(channel) },
        modifier = modifier
            .aspectRatio(1 / 1.5f)
            .padding(6.dp)
    ) {
        if (channel.logoUrl.isNullOrEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            PosterImage(
                title = channel.name,
                posterUrl = channel.logoUrl,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun TvShowSearchContent(
    searchContent: SearchContent.TvShowContent,
    onShowClick: (TvShow) -> Unit,
    modifier: Modifier = Modifier
) {
    val show = searchContent.tvShow
    MovieCard(
        onClick = { onShowClick(show) },
        modifier = modifier
            .aspectRatio(1 / 1.5f)
            .padding(6.dp),
    ) {
        if (show.posterImageUrl == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = show.title ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            PosterImage(
                title = show.title ?: "",
                posterUrl = show.posterImageUrl,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MovieSearchContent(
    searchContent: SearchContent.MovieContent,
    onMovieClick: (MovieNew) -> Unit,
    modifier: Modifier = Modifier
) {
    val movie = searchContent.movie
    MovieCard(
        onClick = { onMovieClick(movie) },
        modifier = modifier
            .aspectRatio(1 / 1.5f)
            .padding(6.dp),
    ) {
        if (movie.posterImageUrl == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            PosterImage(
                title = movie.title,
                posterUrl = movie.posterImageUrl,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
