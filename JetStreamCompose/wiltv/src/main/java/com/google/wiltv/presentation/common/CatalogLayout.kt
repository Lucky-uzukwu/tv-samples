// ABOUTME: Generic catalog layout composable for TV streaming screens
// ABOUTME: Handles hero carousel, content rows, and optional streaming providers with focus management

package com.google.wiltv.presentation.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
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
import com.google.wiltv.presentation.screens.BackgroundState
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.utils.getErrorState
import com.google.wiltv.presentation.utils.hasError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CatalogLayout(
    modifier: Modifier = Modifier,
    featuredMovies: LazyPagingItems<MovieNew>,
    catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
    genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>? = null,
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    carouselState: CarouselState,
    backgroundState: BackgroundState,
    contentDescription: String = "Catalog Screen",
    streamingProviders: List<StreamingProvider>,
    onStreamingProviderClick: ((streamingProvider: StreamingProvider) -> Unit),
    focusManagementConfig: FocusManagementConfig? = null,
    onRowError: ((errorText: UiText) -> Unit)? = null,
    watchlistItemIds: Set<String> = emptySet()
) {
    val tvLazyColumnState = rememberTvLazyListState()
    val rowStates = remember { mutableStateMapOf<String, TvLazyListState>() }

    val catalogToLazyPagingItems = catalogToMovies.mapValues { (catalog, flow) ->
        Logger.d { "Collecting paging items for catalog: ${catalog.name}" }
        flow.collectAsLazyPagingItems()
    }

    val genreToLazyPagingItems = genreToMovies?.mapValues { (genre, flow) ->
        flow.collectAsLazyPagingItems()
    }

    val (carouselFocusRequester, firstLazyRowItemUnderCarouselRequester) = remember { FocusRequester.createRefs() }

    var carouselScrollEnabled by remember { mutableStateOf(true) }

    // Focus management state - memoized for performance
    val focusRequesters =
        remember(streamingProviders.size, catalogToMovies.size, genreToMovies?.size ?: 0) {
            mutableMapOf<Pair<Int, Int>, FocusRequester>().apply {
                // Pre-create focus requesters for streaming providers
                for (i in 0 until streamingProviders.size) {
                    put(Pair(1, i), FocusRequester())
                }
                // Note: Catalog and genre focus requesters are created on-demand in rememberRowFocusRequesters
            }
        }
    var lastFocusedItem by rememberSaveable { mutableStateOf(Pair(0, 0)) }
    var shouldRestoreFocus by remember { mutableStateOf(true) }  // Must reset to true when composable re-enters
    var clearCatalogDetails by remember { mutableStateOf(false) }  // OK to reset on config change
    var carouselTargetStreamingProvider by rememberSaveable { mutableIntStateOf(0) }  // Persist across config changes

    // Clean up focus requesters when component unmounts or data changes significantly
//    if (enableFocusManagement && focusManagementConfig?.enableMemoryOptimization == true) {
//        DisposableEffect(catalogToLazyPagingItems.size, genreToLazyPagingItems?.size) {
//            onDispose {
//                // Clear unused focus requesters to prevent memory leaks
//                val maxRows =
//                    2 + catalogToLazyPagingItems.size + (genreToLazyPagingItems?.size ?: 0)
//                val keysToRemove = focusRequesters.keys.filter { (row, _) -> row > maxRows }
//                keysToRemove.forEach { focusRequesters.remove(it) }
//                Logger.d { "Cleaned up ${keysToRemove.size} unused focus requesters" }
//            }
//        }
//    }

    // Combined focus restoration and carousel sync (if focus management enabled)
    LaunchedEffect(
        lastFocusedItem, streamingProviders.size, shouldRestoreFocus,
        catalogToLazyPagingItems.size, genreToLazyPagingItems?.size ?: 0
    ) {
        // Initialize carousel target from focus restoration state
        if (lastFocusedItem.first == 1 &&
            lastFocusedItem.second >= 0 &&
            lastFocusedItem.second < streamingProviders.size
        ) {
            carouselTargetStreamingProvider = lastFocusedItem.second
        }

        // Focus restoration with viewport safety - single coroutine prevents races
        if (shouldRestoreFocus &&
            lastFocusedItem.first >= 0 && lastFocusedItem.second >= 0 &&
            lastFocusedItem != Pair(-1, -1)
        ) {

            // Short initial delay to beat drawer focus request
            delay(20)
            Logger.d { "Starting focus restoration for position: $lastFocusedItem" }

            val streamingProviderCount = streamingProviders.size

            if (lastFocusedItem.first == 1 &&
                lastFocusedItem.second >= 0 &&
                lastFocusedItem.second < streamingProviderCount
            ) {
                // Restore streaming provider focus with bounds checking
                val streamingRowState = rowStates["row_streaming_providers"]
                if (streamingRowState != null) {
                    try {
                        // Ensure index is within bounds before scrolling
                        if (lastFocusedItem.second < streamingProviderCount) {
                            streamingRowState.scrollToItem(lastFocusedItem.second)
                            delay(100) // Quick delay for scroll to complete

                            val focusRequester = focusRequesters[lastFocusedItem]
                            if (focusRequester != null) {
                                // Try to request focus with retry logic
                                var focusSuccess = false
                                repeat(3) { attempt ->
                                    if (!focusSuccess) {
                                        try {
                                            focusRequester.requestFocus()
                                            focusSuccess = true
                                            shouldRestoreFocus = false
                                            Logger.i { "Focus restoration successful on attempt ${attempt + 1}" }
                                        } catch (e: Exception) {
                                            if (attempt < 2) {
                                                delay(50) // Small delay before retry
                                                Logger.d { "Focus restoration attempt ${attempt + 1} failed, retrying..." }
                                            } else {
                                                Logger.w(e) { "Failed to restore focus after 3 attempts" }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Logger.w { "FocusRequester not found for streaming provider at $lastFocusedItem" }
                            }
                        }
                    } catch (e: Exception) {
                        Logger.w(e) { "Failed to restore streaming provider focus at index ${lastFocusedItem.second}" }
                        shouldRestoreFocus = false
                    }
                }
            } else if (lastFocusedItem.first >= 2) {
                // Determine if this is a catalog or genre row
                val streamingRowOffset = 1
                val adjustedRowIndex = lastFocusedItem.first - 1 - streamingRowOffset
                val catalogKeys = catalogToLazyPagingItems.keys.toList()
                val genreKeys = genreToLazyPagingItems?.keys?.toList() ?: emptyList()

                if (adjustedRowIndex < catalogKeys.size) {
                    // It's a catalog row
                    val catalogKey = catalogKeys[adjustedRowIndex]
                    val catalogRowId = "catalog_${catalogKey.name}"
                    val catalogRowState = rowStates[catalogRowId]
                    val catalogMovies = catalogToLazyPagingItems[catalogKey]

                    if (catalogRowState != null && catalogMovies != null) {
                        try {
                            // Check bounds before scrolling - with configurable performance limit
                            val maxFocusItems =
                                focusManagementConfig?.maxFocusRequestersPerRow ?: 50
                            val maxScrollIndex =
                                minOf(catalogMovies.itemCount - 1, maxFocusItems - 1)
                            val safeScrollIndex = minOf(lastFocusedItem.second, maxScrollIndex)

                            if (safeScrollIndex >= 0 && safeScrollIndex < catalogMovies.itemCount) {
                                catalogRowState.scrollToItem(safeScrollIndex)
                                delay(100) // Quick delay for scroll

                                // Only request focus if focus requester exists within our limit
                                val focusRequester =
                                    if (lastFocusedItem.second < maxFocusItems) {
                                        focusRequesters[lastFocusedItem]
                                    } else {
                                        null
                                    }

                                if (focusRequester != null) {
                                    // Try to request focus with retry logic
                                    var focusSuccess = false
                                    repeat(3) { attempt ->
                                        if (!focusSuccess) {
                                            try {
                                                focusRequester.requestFocus()
                                                focusSuccess = true
                                                shouldRestoreFocus = false
                                                Logger.i { "Catalog focus restoration successful on attempt ${attempt + 1}" }
                                            } catch (e: Exception) {
                                                if (attempt < 2) {
                                                    delay(50)
                                                    Logger.d { "Catalog focus attempt ${attempt + 1} failed, retrying..." }
                                                } else {
                                                    Logger.w(e) { "Failed to restore catalog focus after 3 attempts" }
                                                    shouldRestoreFocus = false
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Logger.d { "No focus requester available for item at $lastFocusedItem" }
                                    shouldRestoreFocus = false
                                }
                            }
                        } catch (e: Exception) {
                            Logger.w(e) { "Failed to restore catalog focus at row $adjustedRowIndex, item ${lastFocusedItem.second}" }
                            shouldRestoreFocus = false
                        }
                    }
                } else if (adjustedRowIndex - catalogKeys.size < genreKeys.size) {
                    // It's a genre row
                    val genreIndex = adjustedRowIndex - catalogKeys.size
                    val genreKey = genreKeys[genreIndex]
                    val genreRowId = "genre_${genreKey.name}"  // Fixed: use 'genre_' prefix
                    val genreRowState = rowStates[genreRowId]
                    val genreMovies = genreToLazyPagingItems?.get(genreKey)

                    if (genreRowState != null && genreMovies != null) {
                        try {
                            val maxFocusItems =
                                focusManagementConfig?.maxFocusRequestersPerRow ?: 50
                            val maxScrollIndex =
                                minOf(genreMovies.itemCount - 1, maxFocusItems - 1)
                            val safeScrollIndex = minOf(lastFocusedItem.second, maxScrollIndex)

                            if (safeScrollIndex >= 0 && safeScrollIndex < genreMovies.itemCount) {
                                genreRowState.scrollToItem(safeScrollIndex)
                                delay(100) // Quick delay for scroll

                                val focusRequester =
                                    if (lastFocusedItem.second < maxFocusItems) {
                                        focusRequesters[lastFocusedItem]
                                    } else {
                                        null
                                    }

                                if (focusRequester != null) {
                                    // Try to request focus with retry logic
                                    var focusSuccess = false
                                    repeat(3) { attempt ->
                                        if (!focusSuccess) {
                                            try {
                                                focusRequester.requestFocus()
                                                focusSuccess = true
                                                shouldRestoreFocus = false
                                                Logger.i { "Genre focus restoration successful on attempt ${attempt + 1}" }
                                            } catch (e: Exception) {
                                                if (attempt < 2) {
                                                    delay(50)
                                                    Logger.d { "Genre focus attempt ${attempt + 1} failed, retrying..." }
                                                } else {
                                                    Logger.w(e) { "Failed to restore genre focus after 3 attempts" }
                                                    shouldRestoreFocus = false
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Logger.d { "No focus requester available for genre item at $lastFocusedItem" }
                                    shouldRestoreFocus = false
                                }
                            }
                        } catch (e: Exception) {
                            Logger.w(e) { "Failed to restore genre focus at genre row $genreIndex, item ${lastFocusedItem.second}" }
                            shouldRestoreFocus = false
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier) {
        CatalogBackground(
            backgroundState = backgroundState,
            modifier = modifier
        )
    }

    TvLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 28.dp)
            .semantics { this.contentDescription = contentDescription },
        state = tvLazyColumnState,
        verticalArrangement = Arrangement.spacedBy(30.dp),
        contentPadding = PaddingValues(vertical = 40.dp)
    ) {

        item(contentType = "HeroSectionCarousel") {
            val targetStreamingProviderFocusRequester =
                if (streamingProviders.isNotEmpty()) {
                    val targetIndex = if (carouselTargetStreamingProvider >= 0 &&
                        carouselTargetStreamingProvider < streamingProviders.size
                    ) {
                        carouselTargetStreamingProvider
                    } else {
                        0
                    }
                    focusRequesters[Pair(1, targetIndex)] ?: firstLazyRowItemUnderCarouselRequester
                } else {
                    firstLazyRowItemUnderCarouselRequester
                }

            MovieHeroSectionCarousel(
                movies = featuredMovies,
                goToVideoPlayer = goToVideoPlayer,
                goToMoreInfo = onMovieClick,
                setSelectedMovie = { movie ->
                    backgroundState.clear()
                    movie.backdropImageUrl?.let { backgroundState.load(it) }
                    setSelectedMovie(movie)
                },
                carouselState = carouselState,
                carouselScrollEnabled = carouselScrollEnabled,
                modifier = Modifier
                    .height(340.dp)
                    .fillMaxWidth(),
                firstLazyRowItemUnderCarouselRequester = targetStreamingProviderFocusRequester,
                carouselFocusRequester = carouselFocusRequester
            )
        }

        // Streaming providers row (if provided)
        item(
            contentType = "StreamingProvidersRow",
            key = "streamingProvidersRow"
        ) {
            val rowId = "row_streaming_providers"
            val rowState = rowStates.getOrPut(rowId) { TvLazyListState() }
            val streamingRowIndex = 1

            val streamingFocusRequesters =
                remember(streamingProviders.size) {
                    streamingProviders.mapIndexed { index, _ ->
                        index to (focusRequesters.getOrPut(
                            Pair(
                                streamingRowIndex,
                                index
                            )
                        ) { FocusRequester() })
                    }.toMap()
                }

            StreamingProvidersRow(
                streamingProviders = streamingProviders,
                onClick = { streamingProvider, itemIndex ->
                    onStreamingProviderClick(streamingProvider)
                },
                modifier = Modifier,
                aboveFocusRequester = carouselFocusRequester,
                lazyRowState = rowState,
                focusRequesters = streamingFocusRequesters,
                downFocusRequester = null,
                onItemFocused = { itemIndex ->
                    lastFocusedItem = Pair(streamingRowIndex, itemIndex)
                    shouldRestoreFocus = false

                    if (itemIndex >= 0 && itemIndex < (streamingProviders.size)) {
                        carouselTargetStreamingProvider = itemIndex
                    }
                }
            )
        }


//        // Catalog rows
        items(
            count = catalogToLazyPagingItems.size,
            key = { catalog ->
                catalogToLazyPagingItems.keys.elementAtOrNull(catalog)?.hashCode() ?: catalog
            },
            contentType = { "MoviesRow" }
        ) { catalogIndex ->
            val catalogKey =
                catalogToLazyPagingItems.keys.elementAtOrNull(catalogIndex) ?: return@items
            val movies = catalogToLazyPagingItems[catalogKey]

            // Monitor for errors in this catalog row
            LaunchedEffect(movies?.hasError()) {
                val catalogName = catalogKey.name
                val hasError = movies?.hasError() == true
                Logger.d { "📚 LaunchedEffect triggered for catalog '$catalogName' - hasError: $hasError, movies != null: ${movies != null}" }

                if (hasError) {
                    movies?.getErrorState()?.let { errorText ->
                        Logger.e { "🚨 Catalog row error detected for '$catalogName': $errorText" }
                        Logger.e { "🚨 Calling onRowError callback for catalog '$catalogName'" }
                        onRowError?.invoke(errorText)
                    } ?: run {
                        Logger.w { "🚨 hasError=true but getErrorState() returned null for catalog '$catalogName'" }
                    }
                } else {
                    Logger.v { "📚 No error for catalog '$catalogName'" }
                }
            }

            if (movies != null && (movies.itemCount > 0 || movies.hasError())) {
                val catalogRowIndex = 2 + catalogIndex
                val catalogRowId = "catalog_${catalogKey.name}"
                val catalogRowState = rowStates.getOrPut(catalogRowId) { TvLazyListState() }

                val catalogFocusRequesters = rememberRowFocusRequesters(
                    movies = movies,
                    rowIndex = catalogRowIndex,
                    focusRequesters = focusRequesters,
                    focusManagementConfig = focusManagementConfig
                )

                ImmersiveListMoviesRow(
                    movies = movies,
                    sectionTitle = catalogKey.name,
                    onMovieClick = onMovieClick,
                    setSelectedMovie = { movie ->
                        backgroundState.clear()
                        carouselScrollEnabled = false
                        val imageUrl = movie.backdropImageUrl
                        setSelectedMovie(movie)
                        imageUrl?.let {
                            backgroundState.load(url = it)
                        }
                    },
                    lazyRowState = catalogRowState,
                    focusRequesters = catalogFocusRequesters,
                    onItemFocused = { movie, index ->
                        lastFocusedItem = Pair(catalogRowIndex, index)
                        shouldRestoreFocus = false
                        clearCatalogDetails = false
                    },
                    clearDetailsSignal = clearCatalogDetails,
                    watchlistItemIds = watchlistItemIds
                )
            }
        }


        // Genre rows (if provided)
        if (genreToLazyPagingItems != null) {
            items(
                count = genreToLazyPagingItems.size,
                key = { genre ->
                    genreToLazyPagingItems.keys.elementAtOrNull(genre)?.hashCode() ?: genre
                },
                contentType = { "MoviesRow" }
            ) { genreIndex ->
                val genreKey =
                    genreToLazyPagingItems.keys.elementAtOrNull(genreIndex) ?: return@items
                val movies = genreToLazyPagingItems[genreKey]

                // Monitor for errors in this genre row
                LaunchedEffect(movies?.hasError()) {
                    val genreName = genreKey.name
                    val hasError = movies?.hasError() == true
                    Logger.d { "🎬 LaunchedEffect triggered for genre '$genreName' - hasError: $hasError, movies != null: ${movies != null}" }

                    if (hasError) {
                        movies?.getErrorState()?.let { errorText ->
                            Logger.e { "🚨 Genre row error detected for '$genreName': $errorText" }
                            Logger.e { "🚨 Calling onRowError callback for genre '$genreName'" }
                            onRowError?.invoke(errorText)
                        } ?: run {
                            Logger.w { "🚨 hasError=true but getErrorState() returned null for genre '$genreName'" }
                        }
                    } else {
                        Logger.v { "🎬 No error for genre '$genreName'" }
                    }
                }

                val shouldRenderRow = movies != null && (movies.itemCount > 0 || movies.hasError())
                Logger.d { "🎬 Genre '${genreKey.name}' render check - movies!=null: ${movies != null}, itemCount: ${movies?.itemCount ?: 0}, hasError: ${movies?.hasError() ?: false}, shouldRender: $shouldRenderRow" }

                if (shouldRenderRow) {
                    val adjustedIndex = catalogToLazyPagingItems.size + genreIndex
                    val genreRowIndex = 2 + adjustedIndex
                    val genreRowId = "genre_${genreKey.name}"
                    val genreRowState = rowStates.getOrPut(genreRowId) { TvLazyListState() }

                    Logger.d { "🎬 Rendering genre row '${genreKey.name}' at index $genreRowIndex" }

                    val genreFocusRequesters = rememberRowFocusRequesters(
                        movies = movies,
                        rowIndex = genreRowIndex,
                        focusRequesters = focusRequesters,
                        focusManagementConfig = focusManagementConfig
                    )

                    ImmersiveListMoviesRow(
                        movies = movies,
                        sectionTitle = genreKey.name,
                        onMovieClick = onMovieClick,
                        setSelectedMovie = { movie ->
                            backgroundState.clear()
                            carouselScrollEnabled = false
                            val imageUrl = movie.backdropImageUrl
                            setSelectedMovie(movie)
                            imageUrl?.let {
                                backgroundState.load(url = it)
                            }
                        },
                        lazyRowState = genreRowState,
                        focusRequesters = genreFocusRequesters,
                        onItemFocused = { movie, index ->
                            lastFocusedItem = Pair(genreRowIndex, index)
                            shouldRestoreFocus = false
                            clearCatalogDetails = false
                        },
                        clearDetailsSignal = clearCatalogDetails,
                        watchlistItemIds = watchlistItemIds
                    )
                } else {
                    Logger.w { "🎬 NOT rendering genre row '${genreKey.name}' - movies!=null: ${movies != null}, itemCount: ${movies?.itemCount ?: 0}, hasError: ${movies?.hasError() ?: false}" }
                }
            }
        }

        // Invisible bottom row (only if focus management enabled AND no genre rows)
        // This prevents focus from getting stuck at the bottom of catalog rows
        item(
            contentType = "InvisibleBottomRow",
            key = "invisible_bottom_row"
        ) {
            InvisibleBottomRow(
                onFocused = {
                    lastFocusedItem = Pair(-1, -1)
                    clearCatalogDetails = true
                }
            )
        }
    }
}


@Composable
fun CatalogBackground(
    backgroundState: BackgroundState,
    modifier: Modifier = Modifier
) {
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


