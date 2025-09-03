package com.google.wiltv.presentation.screens.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import android.view.KeyEvent
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
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.SearchContent
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.common.GameCard
import com.google.wiltv.presentation.common.MovieCard
import com.google.wiltv.presentation.common.PosterImage
import com.google.wiltv.presentation.common.SearchErrorSuggestions
import com.google.wiltv.presentation.common.SearchErrorType
import com.google.wiltv.presentation.common.SearchLoadingShimmer
import com.google.wiltv.presentation.common.SearchQueryDisplay
import com.google.wiltv.presentation.common.SearchSuggestions
import com.google.wiltv.presentation.common.TvChannelCard
import com.google.wiltv.presentation.common.TvVirtualKeyboard
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.theme.WilTvBottomListPadding
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
    onGameClick: (game: CompetitionGame) -> Unit,
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
                onGameClick = onGameClick,
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
    onGameClick: (game: CompetitionGame) -> Unit,
    searchScreenViewModel: SearchScreenViewModel,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()
    val gridState = rememberLazyGridState()

    // Focus management state - similar to CatalogLayout
    val focusRequesters = remember {
        mutableMapOf<Int, FocusRequester>()
    }
    var shouldRestoreFocus by remember { mutableStateOf(true) }

    // Collect search suggestions and initial movies from ViewModel
    val searchSuggestions by searchScreenViewModel.searchSuggestions.collectAsStateWithLifecycle()
    val initialMovies = searchScreenViewModel.initialMovies.collectAsLazyPagingItems()
    val genres by searchScreenViewModel.genres.collectAsStateWithLifecycle()
    val selectedGenreId by searchScreenViewModel.selectedGenreId.collectAsStateWithLifecycle()

    // Focus requesters for navigation between suggestions and keyboard
    val suggestionsFocusRequester = remember { FocusRequester() }
    val genreListFocusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    val focusManager = LocalFocusManager.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var lastSearchedQuery by rememberSaveable { mutableStateOf("") }
    var hasSearched by rememberSaveable { mutableStateOf(false) }
    var lastFocusedIndex by rememberSaveable { mutableIntStateOf(0) }

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
                .padding(28.dp)
        ) {
            SearchQueryDisplay(
                query = searchQuery,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SearchSuggestions(
                suggestions = searchSuggestions,
                onSuggestionClick = { suggestion ->
                    searchQuery = suggestion
                    if (searchQuery.isNotBlank()) {
                        val quotedQuery = "\"$searchQuery\""
                        searchScreenViewModel.query(quotedQuery)
                        lastSearchedQuery = quotedQuery
                        hasSearched = true
                        // Focus stays on the suggestion - user decides where to navigate
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp),
                isVisible = searchQuery.isNotEmpty(),
                focusRequester = suggestionsFocusRequester,
                downFocusRequester = null,
                rightFocusRequester = if (hasSearched && !isEmpty) focusRequesters[0] else if (!hasSearched) focusRequesters[0] else null
            )

            TvVirtualKeyboard(
                onKeyPress = { key ->
                    searchQuery += key
                    searchScreenViewModel.fetchSuggestions(searchQuery)
                },
                onClear = {
                    searchQuery = ""
                    hasSearched = false
                },
                onDelete = {
                    if (searchQuery.isNotEmpty()) {
                        searchQuery = searchQuery.dropLast(1)
                        searchScreenViewModel.fetchSuggestions(searchQuery)
                    }
                },
                onSpace = {
                    searchQuery += " "
                    searchScreenViewModel.fetchSuggestions(searchQuery)
                },
                onEnter = {
                    if (searchQuery.isNotBlank()) {
                        val quotedQuery = "\"$searchQuery\""
                        Log.d("UnifiedSearchScreen", "Search triggered: '$quotedQuery'")
                        searchScreenViewModel.query(quotedQuery)
                        lastSearchedQuery = quotedQuery
                        hasSearched = true
                    }
                },
                initialFocus = !hasSearched,
                upFocusRequester = if (searchQuery.isNotEmpty()) suggestionsFocusRequester else null,
                downFocusRequester = if (genres.isNotEmpty()) genreListFocusRequesters["all"] else null
            )

            if (genres.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                GenreFilterList(
                    genres = genres,
                    selectedGenreId = selectedGenreId,
                    onGenreSelected = { genreId ->
                        searchScreenViewModel.selectGenre(genreId)
                        // Always trigger search when genre is selected
                        if (genreId != null) {
                            // Genre selected - search with genre (with or without text)
                            val queryToUse =
                                if (searchQuery.isNotBlank()) "\"$searchQuery\"" else ""
                            searchScreenViewModel.query(queryToUse)
                            lastSearchedQuery = queryToUse
                            hasSearched = true
                        } else if (searchQuery.isNotBlank()) {
                            // "All" selected with text - search without genre filter
                            val quotedQuery = "\"$searchQuery\""
                            searchScreenViewModel.query(quotedQuery)
                            lastSearchedQuery = quotedQuery
                            hasSearched = true
                        } else {
                            // "All" selected with no text - show initial movies
                            hasSearched = false
                        }
                    },
                    focusRequesters = genreListFocusRequesters,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Right side: Search results (60% width)
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            if (!hasSearched) {
                // Show initial/popular movies as default content
                LazyVerticalGrid(
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = WilTvBottomListPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = initialMovies.itemCount,
                        key = { index ->
                            val movie = initialMovies[index]
                            "initial_movie_${movie?.id ?: index}"
                        }
                    ) { index ->
                        val movie = initialMovies[index]
                        if (movie != null) {
                            val focusRequester =
                                focusRequesters.getOrPut(index) { FocusRequester() }

                            MovieCard(
                                onClick = { onMovieClick(movie) },
                                modifier = Modifier
                                    .aspectRatio(1 / 1.5f)
                                    .padding(6.dp)
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { focusState ->
                                        if (focusState.hasFocus) {
                                            lastFocusedIndex = index
                                            shouldRestoreFocus = false
                                        }
                                    }
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
                    }
                }
            } else {
                val loadState = contentItems?.loadState?.refresh
                when {
                    loadState is LoadState.Loading -> {
                        SearchLoadingShimmer(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp),
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
                                modifier = Modifier.fillMaxSize(),
                                columns = GridCells.Fixed(3),
                                contentPadding = PaddingValues(
                                    top = 16.dp,
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
                                            is SearchContent.CompetitionGameContent -> "game_${item.game.id}"
                                            null -> "loading_$index"
                                        }
                                    }
                                ) { index ->
                                    val searchContent = contentItems[index]
                                    if (searchContent != null) {
                                        // Get or create focus requester for this index - similar to CatalogLayout
                                        val focusRequester =
                                            focusRequesters.getOrPut(index) { FocusRequester() }

                                        val itemModifier = Modifier
                                            .focusRequester(focusRequester)
                                            .onFocusChanged { focusState ->
                                                if (focusState.hasFocus) {
                                                    lastFocusedIndex = index
                                                    shouldRestoreFocus =
                                                        false  // Similar to CatalogLayout
                                                }
                                            }

                                        when (searchContent) {
                                            is SearchContent.MovieContent -> {
                                                MovieSearchContent(
                                                    searchContent = searchContent,
                                                    onMovieClick = { movie ->
                                                        shouldRestoreFocus = true
                                                        onMovieClick(movie)
                                                    },
                                                    modifier = itemModifier
                                                )
                                            }

                                            is SearchContent.TvShowContent -> {
                                                TvShowSearchContent(
                                                    searchContent = searchContent,
                                                    onShowClick = { show ->
                                                        shouldRestoreFocus = true
                                                        onShowClick(show)
                                                    },
                                                    modifier = itemModifier
                                                )
                                            }

                                            is SearchContent.TvChannelContent -> {
                                                TvChannelSearchConent(
                                                    searchContent = searchContent,
                                                    onChannelClick = { channel ->
                                                        shouldRestoreFocus = true
                                                        onChannelClick(channel)
                                                    },
                                                    modifier = itemModifier
                                                )
                                            }

                                            is SearchContent.CompetitionGameContent -> {
                                                GameSearchContent(
                                                    searchContent = searchContent,
                                                    onGameClick = { game ->
                                                        shouldRestoreFocus = true
                                                        onGameClick(game)
                                                    },
                                                    modifier = itemModifier
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Focus restoration with grid scrolling - aligned with CatalogLayout
                            LaunchedEffect(
                                lastFocusedIndex,
                                contentItems?.itemCount,
                                shouldRestoreFocus
                            ) {
                                if (shouldRestoreFocus &&
                                    lastFocusedIndex >= 0 && (contentItems?.itemCount
                                        ?: 0) > lastFocusedIndex
                                ) {

                                    // Short delay to ensure UI is ready - from CatalogLayout
                                    kotlinx.coroutines.delay(20)

                                    try {
                                        // First scroll to make the item visible
                                        gridState.scrollToItem(lastFocusedIndex)
                                        kotlinx.coroutines.delay(100) // Wait for scroll to complete

                                        // Get focus requester for this index
                                        val focusRequester = focusRequesters[lastFocusedIndex]
                                        if (focusRequester != null) {
                                            // Try to request focus with retry logic from CatalogLayout
                                            var focusSuccess = false
                                            repeat(3) { attempt ->
                                                if (!focusSuccess) {
                                                    try {
                                                        focusRequester.requestFocus()
                                                        focusSuccess = true
                                                        shouldRestoreFocus = false
                                                        Log.d(
                                                            "SearchScreen",
                                                            "Focus restoration successful on attempt ${attempt + 1}"
                                                        )
                                                    } catch (e: Exception) {
                                                        if (attempt < 2) {
                                                            kotlinx.coroutines.delay(50)
                                                            Log.d(
                                                                "SearchScreen",
                                                                "Focus restoration attempt ${attempt + 1} failed, retrying..."
                                                            )
                                                        } else {
                                                            Log.w(
                                                                "SearchScreen",
                                                                "Failed to restore focus after 3 attempts",
                                                                e
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.w(
                                                "SearchScreen",
                                                "FocusRequester not found for index $lastFocusedIndex"
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.w(
                                            "SearchScreen",
                                            "Failed to restore focus at index $lastFocusedIndex",
                                            e
                                        )
                                        shouldRestoreFocus = false
                                    }
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

@Composable
private fun GameSearchContent(
    searchContent: SearchContent.CompetitionGameContent,
    onGameClick: (CompetitionGame) -> Unit,
    modifier: Modifier = Modifier
) {
    val game = searchContent.game
    GameCard(
        game = game,
        onClick = { onGameClick(game) },
        modifier = modifier
            .aspectRatio(1 / 1.5f)
            .padding(6.dp)
    )
}

@Composable
private fun GenreFilterList(
    genres: List<Genre>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit,
    focusRequesters: MutableMap<String, FocusRequester>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val purpleColor = Color(0xFFA855F7)

    Column(modifier = modifier) {
        Text(
            text = "Genres",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(200.dp)
        ) {
            item {
                val isAllSelected = selectedGenreId == null
                val allFocusRequester = focusRequesters.getOrPut("all") { FocusRequester() }
                Button(
                    onClick = { onGenreSelected(null) },
                    modifier = Modifier
                        .width(150.dp)
                        .focusRequester(allFocusRequester)
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                        keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                            ) {
                                onGenreSelected(null)
                                true
                            } else {
                                false
                            }
                        },
                    colors = ButtonDefaults.colors(
                        containerColor = if (isAllSelected) purpleColor else MaterialTheme.colorScheme.surface,
                        contentColor = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = purpleColor,
                        focusedContentColor = Color.White
                    )
                ) {
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            items(genres) { genre ->
                val isSelected = selectedGenreId == genre.id
                val genreFocusRequester =
                    focusRequesters.getOrPut("genre_${genre.id}") { FocusRequester() }
                Button(
                    onClick = { onGenreSelected(genre.id) },
                    modifier = Modifier
                        .width(150.dp)
                        .focusRequester(genreFocusRequester)
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                        keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                            ) {
                                onGenreSelected(genre.id)
                                true
                            } else {
                                false
                            }
                        },
                    colors = ButtonDefaults.colors(
                        containerColor = if (isSelected) purpleColor else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = purpleColor,
                        focusedContentColor = Color.White
                    )
                ) {
                    Text(
                        text = genre.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
