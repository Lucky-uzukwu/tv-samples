package com.google.jetstream.presentation.screens.tvshows

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.Text
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.ImmersiveShowsList
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.TvShowHeroSectionCarousel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TVShowScreen(
    onTVShowClick: (tvShow: TvShow) -> Unit,
    goToVideoPlayer: (tvShow: TvShow) -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    isTopBarVisible: Boolean,
    tvShowScreenViewModel: TvShowScreenViewModel = hiltViewModel(),
) {
    val uiState by tvShowScreenViewModel.uiState.collectAsStateWithLifecycle()
    val heroSectionTvShows = tvShowScreenViewModel.heroSectionTvShows.collectAsLazyPagingItems()

    when (val currentState = uiState) {
        is TvShowScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is TvShowScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())

        is TvShowScreenUiState.Ready -> {
            Catalog(
                heroSectionTvShows = heroSectionTvShows,
                catalogToTvShows = currentState.catalogToTvShows,
                genreToTvShows = currentState.genreToTvShows,
                onTVShowClick = onTVShowClick,
                onScroll = onScroll,
                setSelectedTvShow = setSelectedTvShow,
                goToVideoPlayer = goToVideoPlayer,
                isTopBarVisible = isTopBarVisible,
                streamingProviders = currentState.streamingProviders,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun Catalog(
    heroSectionTvShows: LazyPagingItems<TvShow>,
    catalogToTvShows: Map<Catalog, StateFlow<PagingData<TvShow>>>,
    genreToTvShows: Map<Genre, StateFlow<PagingData<TvShow>>>,
    onTVShowClick: (tvShow: TvShow) -> Unit,
    onScroll: (isTopBarVisible: Boolean) -> Unit,
    goToVideoPlayer: (tvShow: TvShow) -> Unit,
    modifier: Modifier = Modifier,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
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
        item(contentType = "TvShowHeroSectionCarousel") {
            TvShowHeroSectionCarousel(
                tvShows = heroSectionTvShows,
                goToVideoPlayer = goToVideoPlayer,
                goToMoreInfo = {},
                setSelectedTvShow = setSelectedTvShow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .focusRequester(carouselFocusRequester),
            )
        }

        // Loop through catalogList to display each catalog and its movies
//        items(
//            items = catalogToTvShows.keys.toList(),
//            key = { catalog -> catalog.id }, // Use catalog ID as unique key
//            contentType = { "ImmersiveShowsList" },
//        ) { catalog ->
//            val tvShowsAsLazy = catalogToTvShows[catalog]?.collectAsLazyPagingItems()
//            val tvShows = tvShowsAsLazy?.itemSnapshotList?.items ?: emptyList()
//            if (tvShows.isNotEmpty()) {
//                ImmersiveShowsList(
//                    tvShows = tvShows,
//                    sectionTitle = catalog.name,
//                    onTvShowClick = onTVShowClick,
//                    setSelectedTvShow = setSelectedTvShow,
//                    modifier = Modifier.onFocusChanged {
//                        immersiveListHasFocus = it.hasFocus
//                    },
//                )
//            }
//        }

        // Loop through genreList to display each catalog and its movies
//        items(
//            items = genreToTvShows.keys.toList(),
//            key = { genre -> genre.id }, // Use catalog ID as unique key
//            contentType = { "ImmersiveShowsList" }
//        ) { genre ->
//            val tvShowsAsLazy = genreToTvShows[genre]?.collectAsLazyPagingItems()
//            val tvShows = tvShowsAsLazy?.itemSnapshotList?.items ?: emptyList()
//            ImmersiveShowsList(
//                tvShows = tvShows,
//                sectionTitle = genre.name,
//                onTvShowClick = onTVShowClick,
//                setSelectedTvShow = setSelectedTvShow,
//                modifier = Modifier.onFocusChanged {
//                    immersiveListHasFocus = it.hasFocus
//                },
//            )
//        }
    }
}