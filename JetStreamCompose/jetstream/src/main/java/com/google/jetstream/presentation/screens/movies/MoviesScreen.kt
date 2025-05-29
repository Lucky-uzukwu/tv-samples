package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MovieHeroSectionCarousel
import com.google.jetstream.presentation.common.StreamingProviderIcon
import com.google.jetstream.presentation.common.Top10MoviesList
import com.google.jetstream.presentation.screens.home.HomeScreenUiState
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.forEach

@Composable
fun MoviesScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    isTopBarVisible: Boolean,
    moviesScreenViewModel: MoviesScreenViewModel = hiltViewModel(),
) {
    val uiState by moviesScreenViewModel.uiState.collectAsStateWithLifecycle()
    val featuredMovies = moviesScreenViewModel.heroSectionMovies.collectAsLazyPagingItems()

    when (val s = uiState) {
        is MoviesScreenUiState.Ready -> {
            Catalog(
                featuredMoviesNew = featuredMovies,
                catalogToMovies = s.catalogToMovies,
                genreToMovies = s.genreToMovies,
                onMovieClick = onMovieClick,
                onScroll = onScroll,
                setSelectedMovie = setSelectedMovie,
                goToVideoPlayer = goToVideoPlayer,
                isTopBarVisible = isTopBarVisible,
                streamingProviders = s.streamingProviders,
                modifier = Modifier.fillMaxSize(),
            )
        }

        is MoviesScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is MoviesScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun Catalog(
    featuredMoviesNew: LazyPagingItems<MovieNew>,
    catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
    genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
    onMovieClick: (movie: MovieNew) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    modifier: Modifier = Modifier,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    streamingProviders: List<StreamingProvider>,
    isTopBarVisible: Boolean = true,
) {
    val lazyListState = rememberLazyListState()
    var immersiveListHasFocus by remember { mutableStateOf(false) }

    val shouldShowTopBar by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset < 300
        }
    }
    val carouselFocusRequester = remember { FocusRequester() }
    LaunchedEffect(shouldShowTopBar) {
        onScroll(shouldShowTopBar)
    }
    LaunchedEffect(isTopBarVisible) {
        if (isTopBarVisible) lazyListState.animateScrollToItem(0)
    }


    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 108.dp),
        modifier = modifier,
    ) {
        item(contentType = "HeroSectionCarousel") {
            MovieHeroSectionCarousel(
                movies = featuredMoviesNew,
                goToVideoPlayer = goToVideoPlayer,
                goToMoreInfo = {},
                setSelectedMovie = setSelectedMovie,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .focusRequester(carouselFocusRequester),
            )
        }

        // Loop through catalogList to display each catalog and its movies
        items(
            items = catalogToMovies.keys.toList(),
            key = { catalog -> catalog.id }, // Use catalog ID as unique key
            contentType = { "MoviesRow" }
        ) { catalog ->
            val movies = catalogToMovies[catalog]?.collectAsLazyPagingItems()
            val movieList = movies?.itemSnapshotList?.items ?: emptyList()


            Top10MoviesList(
                movieList = movieList,
                sectionTitle = catalog.name,
                onMovieClick = onMovieClick,
                setSelectedMovie = setSelectedMovie,
                modifier = Modifier.onFocusChanged {
                    immersiveListHasFocus = it.hasFocus
                },
            )
        }

        // Loop through genreList to display each catalog and its movies
        items(
            items = genreToMovies.keys.toList(),
            key = { genre -> genre.id }, // Use catalog ID as unique key
            contentType = { "MoviesRow" }
        ) { genre ->
            val movies = genreToMovies[genre]?.collectAsLazyPagingItems()
            val movieList = movies?.itemSnapshotList?.items ?: emptyList()

            Top10MoviesList(
                movieList = movieList,
                sectionTitle = genre.name,
                onMovieClick = onMovieClick,
                setSelectedMovie = setSelectedMovie,
                modifier = Modifier.onFocusChanged {
                    immersiveListHasFocus = it.hasFocus
                },
            )
        }
    }

}