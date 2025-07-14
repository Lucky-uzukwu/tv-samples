package com.google.jetstream.presentation.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import co.touchlab.kermit.Logger
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.ImmersiveListMoviesRow
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MovieHeroSectionCarousel
import com.google.jetstream.presentation.common.StreamingProvidersRow
import com.google.jetstream.presentation.screens.backgroundImageState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalTvMaterial3Api::class)
val carouselSaver =
    Saver<CarouselState, Int>(save = { it.activeItemIndex }, restore = { CarouselState(it) })


@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun HomeScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    onStreamingProviderClick: (streamingProvider: StreamingProvider) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    homeScreeViewModel: HomeScreeViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by homeScreeViewModel.uiState.collectAsStateWithLifecycle()
    val featuredMovies = homeScreeViewModel.heroSectionMovies.collectAsLazyPagingItems()
    val carouselState = rememberSaveable(saver = carouselSaver) { CarouselState(0) }

    when (val s = uiState) {
        is HomeScreenUiState.Ready -> {
            Catalog(
                featuredMovies = featuredMovies,
                catalogToMovies = s.catalogToMovies,
                genreToMovies = s.genreToMovies,
                onMovieClick = onMovieClick,
                onStreamingProviderClick = onStreamingProviderClick,
                setSelectedMovie = setSelectedMovie,
                goToVideoPlayer = goToVideoPlayer,
                streamingProviders = s.streamingProviders,
                carouselState = carouselState,
                modifier = Modifier.fillMaxSize(),
                navController = navController
            )
        }

        is HomeScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is HomeScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
private fun Catalog(
    featuredMovies: LazyPagingItems<MovieNew>,
    catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
    genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
    onMovieClick: (movie: MovieNew) -> Unit,
    onStreamingProviderClick: (streamingProvider: StreamingProvider) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    modifier: Modifier = Modifier,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    streamingProviders: List<StreamingProvider>,
    carouselState: CarouselState,
    navController: NavController
) {

    val lazyRowState = rememberTvLazyListState()

    val tvLazyColumnState = rememberTvLazyListState()
    val rowStates = remember { mutableStateMapOf<String, TvLazyListState>() }
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    val backgroundState = backgroundImageState()
    val catalogToLazyPagingItems = catalogToMovies.mapValues { (_, flow) ->
        flow.collectAsLazyPagingItems()
    }

    val (carouselFocusRequester, firstLazyRowItemUnderCarouselRequester) = remember { FocusRequester.createRefs() }


    var carouselScrollEnabled by remember { mutableStateOf(true) }

    // Restore scroll positions when navigating back
    savedStateHandle?.get<String>("target_row_id")?.let { targetRowId ->
        savedStateHandle.get<Int>("column_scroll_index")?.let { columnIndex ->
            savedStateHandle.get<Int>("column_scroll_offset")?.let { columnOffset ->
                savedStateHandle.get<Int>("${targetRowId}_scroll_offset")?.let { rowScrollOffset ->
                    savedStateHandle.get<Int>("${targetRowId}_target_item")
                        ?.let { targetItemIndex ->
                            LaunchedEffect(Unit) {
                                Logger.i {
                                    "RowsScreen"
                                    "Restoring: Column(index=$columnIndex, offset=$columnOffset), " +
                                            "Row($targetRowId, targetItem=$targetItemIndex)"
                                }
                                // Scroll LazyColumn to the correct row
                                tvLazyColumnState.scrollToItem(columnIndex, columnOffset)
                                // Scroll the target LazyRow to the correct item
                                val scrollOffset =
                                    if (targetItemIndex < 7) 100 else (targetItemIndex - 6) * 100
                                rowStates[targetRowId]?.scrollToItem(
                                    index = targetItemIndex,
                                    scrollOffset = scrollOffset
                                )

                                savedStateHandle.remove<String>("target_row_id")
                                savedStateHandle.remove<Int>("column_scroll_index")
                                savedStateHandle.remove<Int>("column_scroll_offset")
                                savedStateHandle.remove<Int>("${targetRowId}_target_item")
                            }
                        }
                }
            }
        }
    }

    Box(modifier = modifier) {
        val targetBitmap by remember(backgroundState) { backgroundState.drawable }

        val overlayColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)

        Crossfade(targetState = targetBitmap) {
            it?.let {
                Image(
                    modifier = modifier
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                Brush.horizontalGradient(
                                    listOf(
                                        overlayColor,
                                        overlayColor.copy(alpha = 0.8f),
                                        Color.Transparent
                                    )
                                )
                            )
                            drawRect(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent, overlayColor.copy(alpha = 0.5f)
                                    )
                                )
                            )
                        },
                    bitmap = it,
                    contentDescription = "Hero item background",
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }

    TvLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 28.dp)
            .semantics { contentDescription = "Home Screen" },
        state = tvLazyColumnState,
        verticalArrangement = Arrangement.spacedBy(30.dp),
        contentPadding = PaddingValues(vertical = 40.dp)
    ) {

        item(contentType = "HeroSectionCarousel") {
            MovieHeroSectionCarousel(
                movies = featuredMovies,
                goToVideoPlayer = goToVideoPlayer,
                goToMoreInfo = onMovieClick,
                setSelectedMovie = { movie ->
                    val imageUrl = "https://api.nortv.xyz/" + "storage/" + movie.backdropImagePath
                    backgroundState.load(
                        url = imageUrl
                    )
                    setSelectedMovie(movie)
                },
                carouselState = carouselState,
                carouselScrollEnabled = carouselScrollEnabled,
                modifier = Modifier
                    .height(340.dp)
                    .fillMaxWidth(),
                firstLazyRowItemUnderCarouselRequester = firstLazyRowItemUnderCarouselRequester,
                carouselFocusRequester = carouselFocusRequester,
                lazyRowState = lazyRowState
            )
        }


//        item(
//            contentType = "StreamingProvidersRow",
//            key = "homeScreenStreamingProvidersRow"
//        ) {
//            val rowId = "row_streaming_providers"
//            val rowState = rowStates.getOrPut(rowId) { rememberTvLazyListState() }
//
//            StreamingProvidersRow(
//                streamingProviders = streamingProviders.take(5),
//                onClick = { streamingProvider, itemIndex ->
//                    // Save parent LazyColumn scroll state
//                    Logger.i { "Setting saved state handle column_scroll_index to : ${tvLazyColumnState.firstVisibleItemIndex}" }
//                    savedStateHandle?.set(
//                        "column_scroll_index",
//                        tvLazyColumnState.firstVisibleItemIndex
//                    )
//                    Logger.i { "Setting saved state handle column_scroll_index to : ${tvLazyColumnState.firstVisibleItemScrollOffset}" }
//                    savedStateHandle?.set(
//                        "column_scroll_offset",
//                        tvLazyColumnState.firstVisibleItemScrollOffset
//                    )
//                    // Save the exact item index (not just firstVisibleItemIndex)
//                    savedStateHandle?.set("${rowId}_target_item", itemIndex)
//                    savedStateHandle?.set("${rowId}_scroll_offset", rowState.firstVisibleItemIndex)
//                    // Save target row ID
//                    savedStateHandle?.set("target_row_id", rowId)
//                    onStreamingProviderClick(streamingProvider)
//                },
//                modifier = Modifier,
//                firstItemFocusRequester = firstLazyRowItemUnderCarouselRequester,
//                aboveFocusRequester = carouselFocusRequester,
//                lazyRowState = rowState
//            )
//        }


//        items(
//            count = catalogToLazyPagingItems.size,
//            key = { catalog -> catalog.hashCode() }, // Use catalog ID as unique key
//            contentType = { "MoviesRow" }
//        ) { catalog ->
//            val catalogKey = catalogToLazyPagingItems.keys.elementAt(catalog)
//            val movies = catalogToLazyPagingItems[catalogKey]
//
//            if (movies != null && movies.itemCount > 0) {
//                ImmersiveListMoviesRow(
//                    movies = movies,
//                    sectionTitle = catalogKey.name,
//                    onMovieClick = onMovieClick,
//                    setSelectedMovie = { movie ->
//                        carouselScrollEnabled = false
//                        val imageUrl =
//                            "https://api.nortv.xyz/" + "storage/" + movie.backdropImagePath
//                        setSelectedMovie(movie)
//                        backgroundState.load(
//                            url = imageUrl
//                        )
//                    },
//                )
//            }
//        }
    }
}

@Composable
fun rememberCurrentOffset(state: TvLazyListState): androidx.compose.runtime.State<Int> {
    val position = remember { derivedStateOf { state.firstVisibleItemIndex } }
    val itemOffset = remember { derivedStateOf { state.firstVisibleItemScrollOffset } }
    val lastPosition = rememberPrevious(position.value)
    val lastItemOffset = rememberPrevious(itemOffset.value)
    val currentOffset = remember { mutableStateOf(0) }

    LaunchedEffect(position.value, itemOffset.value) {
        if (lastPosition == null || position.value == 0) {
            currentOffset.value = itemOffset.value
        } else if (lastPosition == position.value) {
            currentOffset.value += (itemOffset.value - (lastItemOffset ?: 0))
        } else if (lastPosition > position.value) {
            currentOffset.value -= (lastItemOffset ?: 0)
        } else { // lastPosition.value < position.value
            currentOffset.value += itemOffset.value
        }
    }

    return currentOffset
}

/**
 * Returns a dummy MutableState that does not cause render when setting it
 */
@Composable
fun <T> rememberRef(): MutableState<T?> {
    // for some reason it always recreated the value with vararg keys,
    // leaving out the keys as a parameter for remember for now
    return remember() {
        object : MutableState<T?> {
            override var value: T? = null

            override fun component1(): T? = value

            override fun component2(): (T?) -> Unit = { value = it }
        }
    }
}

@Composable
fun <T> rememberPrevious(
    current: T,
    shouldUpdate: (prev: T?, curr: T) -> Boolean = { a: T?, b: T -> a != b },
): T? {
    val ref = rememberRef<T>()

    // launched after render, so the current render will have the old value anyway
    SideEffect {
        if (shouldUpdate(ref.value, current)) {
            ref.value = current
        }
    }

    return ref.value
}