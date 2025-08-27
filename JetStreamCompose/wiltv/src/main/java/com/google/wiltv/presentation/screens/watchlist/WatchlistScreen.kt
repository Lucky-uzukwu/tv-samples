// ABOUTME: Watchlist screen displaying user's saved movies and TV shows in grid layout
// ABOUTME: Provides remove functionality and empty state handling for watchlist management
package com.google.wiltv.presentation.screens.watchlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.common.MovieCard
import com.google.wiltv.presentation.common.PosterImage
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.theme.WilTvCardShape

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WatchlistScreen(
    onMovieClick: (com.google.wiltv.data.models.MovieNew) -> Unit = {},
    onTvShowClick: (com.google.wiltv.data.models.TvShow) -> Unit = {},
    viewModel: WatchlistScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val contentTypeFilter by viewModel.contentTypeFilter.collectAsStateWithLifecycle()
    val childPadding = rememberChildPadding()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is WatchlistScreenUiState.Success && currentState.watchlistItems.isNotEmpty()) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search and filter controls
        SearchAndFilterControls(
            searchQuery = searchQuery,
            contentTypeFilter = contentTypeFilter,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onContentTypeFilterChange = viewModel::updateContentTypeFilter,
            modifier = Modifier.padding(horizontal = childPadding.start + 28.dp, vertical = 16.dp)
        )
        
        // Content based on state
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
                        text = if (searchQuery.isNotEmpty() || contentTypeFilter != ContentTypeFilter.ALL) {
                            "No items found matching your search criteria"
                        } else {
                            "Your watchlist is empty\nAdd movies and TV shows from their detail pages"
                        },
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
}

@Composable
private fun SearchAndFilterControls(
    searchQuery: String,
    contentTypeFilter: ContentTypeFilter,
    onSearchQueryChange: (String) -> Unit,
    onContentTypeFilterChange: (ContentTypeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Search input field
        SearchInputField(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Filter buttons
        FilterButtons(
            contentTypeFilter = contentTypeFilter,
            onContentTypeFilterChange = onContentTypeFilterChange
        )
    }
}

@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    
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
                    width = if (isFocused) 2.dp else 1.dp,
                    color = animateColorAsState(
                        targetValue = if (isFocused) MaterialTheme.colorScheme.onSurface.copy(alpha = 1f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        label = ""
                    ).value
                ),
                shape = WilTvCardShape
            )
        ),
        tonalElevation = 2.dp,
        modifier = modifier,
        onClick = { textFieldFocusRequester.requestFocus() }
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .padding(horizontal = 20.dp),
                ) {
                    innerTextField()
                    if (query.isEmpty()) {
                        Text(
                            modifier = Modifier.graphicsLayer { alpha = 0.6f },
                            text = "Search your watchlist...",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() }
            ),
            interactionSource = interactionSource,
            modifier = Modifier.focusRequester(textFieldFocusRequester)
        )
    }
}

@Composable
private fun FilterButtons(
    contentTypeFilter: ContentTypeFilter,
    onContentTypeFilterChange: (ContentTypeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterButton(
            text = "All",
            isSelected = contentTypeFilter == ContentTypeFilter.ALL,
            onClick = { onContentTypeFilterChange(ContentTypeFilter.ALL) }
        )
        
        FilterButton(
            text = "Movies",
            isSelected = contentTypeFilter == ContentTypeFilter.MOVIES,
            onClick = { onContentTypeFilterChange(ContentTypeFilter.MOVIES) }
        )
        
        FilterButton(
            text = "TV Shows",
            isSelected = contentTypeFilter == ContentTypeFilter.TV_SHOWS,
            onClick = { onContentTypeFilterChange(ContentTypeFilter.TV_SHOWS) }
        )
    }
}

@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    
    Button(
        onClick = {
            onClick()
            // Keep focus on this button after selection
            focusRequester.requestFocus()
        },
        modifier = modifier.focusRequester(focusRequester),
        colors = if (isSelected) {
            androidx.tv.material3.ButtonDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            androidx.tv.material3.ButtonDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}