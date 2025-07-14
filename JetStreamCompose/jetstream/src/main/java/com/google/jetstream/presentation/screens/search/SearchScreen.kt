package com.google.jetstream.presentation.screens.search

import android.view.KeyEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.presentation.common.MovieCard
import com.google.jetstream.presentation.common.PosterImage
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.JetStreamBottomListPadding
import com.google.jetstream.presentation.theme.JetStreamCardShape
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
    modifier: Modifier = Modifier,
    lazyColumnState: LazyListState = rememberLazyListState(),
) {
    val childPadding = rememberChildPadding()
    var searchQuery by remember { mutableStateOf("") }
    val tfFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val tfInteractionSource = remember { MutableInteractionSource() }

    val isTfFocused by tfInteractionSource.collectIsFocusedAsState()

    val isMoviesEmpty =
        movies?.collectAsLazyPagingItems()?.itemSnapshotList?.items?.isEmpty() == true
    val isShowsEmpty =
        shows?.collectAsLazyPagingItems()?.itemSnapshotList?.items?.isEmpty() == true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LazyColumn(
            modifier = modifier,
            state = lazyColumnState
        ) {
            item {
                Surface(
                    shape = ClickableSurfaceDefaults.shape(shape = JetStreamCardShape),
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
                            shape = JetStreamCardShape
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
                                searchMovies(searchQuery)
                            }
                        ),
                        maxLines = 1,
                        interactionSource = tfInteractionSource,
                        textStyle = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        AnimatedContent(
            targetState = TargetState(movies, shows),
            label = "",
        ) { state ->
            val movieList = state.movies?.collectAsLazyPagingItems()?.itemSnapshotList?.items
            val showList = state.shows?.collectAsLazyPagingItems()?.itemSnapshotList?.items
            if (isMoviesEmpty && isShowsEmpty && searchQuery != "") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No result found",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    modifier = Modifier.padding(top = 80.dp, start = 40.dp),
                    columns = GridCells.Fixed(6),
                    contentPadding = PaddingValues(bottom = JetStreamBottomListPadding)
                ) {
                    itemsIndexed(
                        movieList.orEmpty(),
                        contentType = { _, _ -> "movie" },
                        key = { _, movie -> movie.id }) { index, movie ->
                        MovieCard(
                            onClick = { onMovieClick(movie) },
                            modifier = Modifier
                                .aspectRatio(1 / 1.5f)
                                .padding(8.dp)
                                .then(
                                    Modifier
                                ),
                        ) {
                            val imageUrl =
                                "https://api.nortv.xyz/" + "storage/" + movie.posterImagePath
                            PosterImage(
                                title = movie.title,
                                posterUrl = imageUrl,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
//                    itemsIndexed(
//                        showList.orEmpty(),
//                        contentType = { _, _ -> "show" },
//                        key = { _, show -> show.id }) { index, show ->
//                        MovieCard(
//                            onClick = { onShowClick(show) },
//                            modifier = Modifier
//                                .aspectRatio(1 / 1.5f)
//                                .padding(8.dp)
//                                .then(
//                                    Modifier
//                                ),
//                        ) {
//                            val imageUrl =
//                                "https://api.nortv.xyz/" + "storage/" + show.posterImagePath
//                            show.title?.let {
//                                PosterImage(
//                                    title = it,
//                                    posterUrl = imageUrl,
//                                    modifier = Modifier.fillMaxSize()
//                                )
//                            }
//                        }
//                    }
                }
            }
        }


        Column {

//            AnimatedContent(
//                targetState = shows,
//                label = "",
//            ) { state ->
//                val showList = state?.collectAsLazyPagingItems()?.itemSnapshotList?.items
//                if (isShowsEmpty && isMoviesEmpty && searchQuery != "") {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "No result found",
//                            style = MaterialTheme.typography.bodyLarge,
//                            modifier = Modifier.padding(16.dp)
//                        )
//                    }
//                } else {
//                    val paddingTop = if (isMoviesEmpty) 10.dp else 160.dp
//                    LazyVerticalGrid(
//                        modifier = Modifier.padding(top = paddingTop, start = 40.dp),
//                        columns = GridCells.Fixed(6),
//                        contentPadding = PaddingValues(bottom = JetStreamBottomListPadding)
//                    ) {
//                        itemsIndexed(
//                            showList.orEmpty(),
//                            key = { _, show -> show.id }) { index, show ->
//                            MovieCard(
//                                onClick = { onShowClick(show) },
//                                modifier = Modifier
//                                    .aspectRatio(1 / 1.5f)
//                                    .padding(8.dp)
//                                    .then(
//                                        Modifier
//                                    ),
//                            ) {
//                                val imageUrl =
//                                    "https://api.nortv.xyz/" + "storage/" + show.posterImagePath
//                                show.title?.let {
//                                    PosterImage(
//                                        title = it,
//                                        posterUrl = imageUrl,
//                                        modifier = Modifier.fillMaxSize()
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }


    }
}
