package com.google.wiltv.presentation.screens.home

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
import androidx.compose.runtime.State
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
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.network.Catalog
import com.google.wiltv.presentation.common.Error
import com.google.wiltv.presentation.common.ImmersiveListMoviesRow
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.common.MovieHeroSectionCarousel
import com.google.wiltv.presentation.common.StreamingProvidersRow
import com.google.wiltv.presentation.screens.backgroundImageState
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

    // Global focus restoration state  
    val focusRequesters = remember { mutableMapOf<Pair<Int, Int>, FocusRequester>() }
    var lastFocusedItem by rememberSaveable { mutableStateOf(Pair(0, 0)) }

    val backgroundState = backgroundImageState()
    val catalogToLazyPagingItems = catalogToMovies.mapValues { (_, flow) ->
        flow.collectAsLazyPagingItems()
    }

    val (carouselFocusRequester, firstLazyRowItemUnderCarouselRequester) = remember { FocusRequester.createRefs() }


    var carouselScrollEnabled by remember { mutableStateOf(true) }

    // Focus restoration
    LaunchedEffect(Unit) {
        if (lastFocusedItem.first >= 0 && lastFocusedItem.second >= 0) {
            // Add bounds checking for streaming providers
            if (lastFocusedItem.first == 1 && lastFocusedItem.second < streamingProviders.size) {
                Logger.i { "Attempting focus restoration: row=${lastFocusedItem.first}, item=${lastFocusedItem.second}, total providers=${streamingProviders.size}" }

                // Get the streaming row state to scroll first
                val streamingRowState = rowStates["row_streaming_providers"]

                if (streamingRowState != null) {
                    // First scroll to the target item to ensure it's composed
                    streamingRowState.scrollToItem(lastFocusedItem.second)
                    Logger.i { "Scrolled to streaming provider item: ${lastFocusedItem.second}" }

                    // Wait for scroll and composition to complete
                    kotlinx.coroutines.delay(200)

                    // Then request focus
                    val focusRequester = focusRequesters[lastFocusedItem]
                    if (focusRequester != null) {
                        focusRequester.requestFocus()
                        Logger.i { "Focus restoration successful" }
                    } else {
                        Logger.w { "FocusRequester not found for $lastFocusedItem after scroll" }
                    }
                } else {
                    Logger.w { "StreamingRowState not found for focus restoration" }
                }
            } else {
                Logger.w { "Focus restoration skipped - invalid bounds: row=${lastFocusedItem.first}, item=${lastFocusedItem.second}, providers=${streamingProviders.size}" }
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
            // Get the first streaming provider's FocusRequester for carousel navigation
            val firstStreamingProviderFocusRequester = focusRequesters[Pair(1, 0)] ?: firstLazyRowItemUnderCarouselRequester

            MovieHeroSectionCarousel(
                movies = featuredMovies,
                goToVideoPlayer = goToVideoPlayer,
                goToMoreInfo = onMovieClick,
                setSelectedMovie = { movie ->
                    movie.backdropImageUrl?.let {
                        backgroundState.load(
                            url = it
                        )
                    }
                    setSelectedMovie(movie)
                },
                carouselState = carouselState,
                carouselScrollEnabled = carouselScrollEnabled,
                modifier = Modifier
                    .height(340.dp)
                    .fillMaxWidth(),
                firstLazyRowItemUnderCarouselRequester = firstStreamingProviderFocusRequester,
                carouselFocusRequester = carouselFocusRequester,
                lazyRowState = lazyRowState
            )
        }


        item(
            contentType = "StreamingProvidersRow",
            key = "homeScreenStreamingProvidersRow"
        ) {
            val rowId = "row_streaming_providers"
            val rowState = rowStates.getOrPut(rowId) { rememberTvLazyListState() }
            val streamingRowIndex = 1 // StreamingProviders is row 1 (after carousel row 0)

            // Create focus requesters for streaming providers
            val streamingFocusRequesters = streamingProviders.mapIndexed { index, _ ->
                index to (focusRequesters.getOrPut(
                    Pair(
                        streamingRowIndex,
                        index
                    )
                ) { FocusRequester() })
            }.toMap()

            StreamingProvidersRow(
                streamingProviders = streamingProviders,
                onClick = { streamingProvider, itemIndex ->
                    onStreamingProviderClick(streamingProvider)
                },
                modifier = Modifier,
                aboveFocusRequester = carouselFocusRequester,
                lazyRowState = rowState,
                focusRequesters = streamingFocusRequesters,
                onItemFocused = { itemIndex ->
                    // Log the focused item
                    Logger.i {
                        "StreamingProvidersRow"
                        "Focusing: Column(index=$streamingRowIndex), " +
                                "Row(targetItem=$itemIndex)"
                    }
                    lastFocusedItem = Pair(streamingRowIndex, itemIndex)
                }
            )
        }


        items(
            count = catalogToLazyPagingItems.size,
            key = { catalog -> catalog.hashCode() }, // Use catalog ID as unique key
            contentType = { "MoviesRow" }
        ) { catalog ->
            val catalogKey = catalogToLazyPagingItems.keys.elementAt(catalog)
            val movies = catalogToLazyPagingItems[catalogKey]

            if (movies != null && movies.itemCount > 0) {
                ImmersiveListMoviesRow(
                    movies = movies,
                    sectionTitle = catalogKey.name,
                    onMovieClick = onMovieClick,
                    setSelectedMovie = { movie ->
                        carouselScrollEnabled = false
                        val imageUrl = movie.backdropImageUrl
                        setSelectedMovie(movie)
                        imageUrl?.let {
                            backgroundState.load(
                                url = it
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun rememberCurrentOffset(state: TvLazyListState): State<Int> {
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