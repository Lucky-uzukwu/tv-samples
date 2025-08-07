// ABOUTME: TV show-specific catalog layout composable for TV streaming screens
// ABOUTME: Handles hero carousel, content rows, and optional streaming providers with focus management for TV shows

package com.google.wiltv.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
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
import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.Catalog
import com.google.wiltv.presentation.screens.BackgroundState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCatalogLayout(
    modifier: Modifier = Modifier,
    featuredTvShows: LazyPagingItems<TvShow>,
    catalogToTvShows: Map<Catalog, StateFlow<PagingData<TvShow>>>,
    genreToTvShows: Map<Genre, StateFlow<PagingData<TvShow>>>? = null,
    onTvShowClick: (tvShow: TvShow) -> Unit,
    goToVideoPlayer: (tvShow: TvShow) -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    carouselState: CarouselState,
    backgroundState: BackgroundState,
    contentDescription: String = "TV Shows Catalog Screen",
    streamingProviders: List<StreamingProvider>,
    onStreamingProviderClick: ((streamingProvider: StreamingProvider) -> Unit),
    focusManagementConfig: FocusManagementConfig? = null
) {
    val tvLazyColumnState = rememberTvLazyListState()
    val rowStates = remember { mutableStateMapOf<String, TvLazyListState>() }

    val catalogToLazyPagingItems = catalogToTvShows.mapValues { (catalog, flow) ->
        Logger.d { "Collecting paging items for catalog: ${catalog.name}" }
        flow.collectAsLazyPagingItems()
    }

    val genreToLazyPagingItems = genreToTvShows?.mapValues { (genre, flow) ->
        Logger.d { "Collecting paging items for genre: ${genre.name}" }
        flow.collectAsLazyPagingItems()
    }

    val (carouselFocusRequester, firstLazyRowItemUnderCarouselRequester) = remember { FocusRequester.createRefs() }

    var carouselScrollEnabled by remember { mutableStateOf(true) }

    // Focus management state - memoized for performance
    val focusRequesters =
        remember(streamingProviders.size, catalogToTvShows.size, genreToTvShows?.size ?: 0) {
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
            delay(50)
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
                    val catalogTvShows = catalogToLazyPagingItems[catalogKey]

                    if (catalogRowState != null && catalogTvShows != null) {
                        try {
                            // Check bounds before scrolling - with configurable performance limit
                            val maxFocusItems =
                                focusManagementConfig?.maxFocusRequestersPerRow ?: 50
                            val maxScrollIndex =
                                minOf(catalogTvShows.itemCount - 1, maxFocusItems - 1)
                            val safeScrollIndex = minOf(lastFocusedItem.second, maxScrollIndex)

                            if (safeScrollIndex >= 0 && safeScrollIndex < catalogTvShows.itemCount) {
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
                    val genreTvShows = genreToLazyPagingItems?.get(genreKey)

                    if (genreRowState != null && genreTvShows != null) {
                        try {
                            val maxFocusItems =
                                focusManagementConfig?.maxFocusRequestersPerRow ?: 50
                            val maxScrollIndex =
                                minOf(genreTvShows.itemCount - 1, maxFocusItems - 1)
                            val safeScrollIndex = minOf(lastFocusedItem.second, maxScrollIndex)

                            if (safeScrollIndex >= 0 && safeScrollIndex < genreTvShows.itemCount) {
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

        item(contentType = "TvShowHeroSectionCarousel") {
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

            TvShowHeroSectionCarousel(
                tvShows = featuredTvShows,
                goToMoreInfo = onTvShowClick,
                setSelectedTvShow = { tvShow ->
                    tvShow.backdropImageUrl?.let {
                        backgroundState.load(url = it)
                    }
                    setSelectedTvShow(tvShow)
                },
                carouselState = carouselState,
                carouselScrollEnabled = carouselScrollEnabled,
                modifier = Modifier
                    .height(340.dp)
                    .fillMaxWidth(),
                firstLazyRowItemUnderCarouselRequester = targetStreamingProviderFocusRequester,
                carouselFocusRequester = carouselFocusRequester,
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


        // Genre rows (if provided)
        if (genreToLazyPagingItems != null) {
            items(
                count = genreToLazyPagingItems.size,
                key = { genre ->
                    genreToLazyPagingItems.keys.elementAtOrNull(genre)?.hashCode() ?: genre
                },
                contentType = { "TvShowsRow" }
            ) { genreIndex ->
                val genreKey =
                    genreToLazyPagingItems.keys.elementAtOrNull(genreIndex) ?: return@items
                val tvShows = genreToLazyPagingItems[genreKey]

                if (tvShows != null && tvShows.itemCount > 0) {
                    val adjustedIndex = catalogToLazyPagingItems.size + genreIndex
                    val genreRowIndex = 2 + adjustedIndex
                    val genreRowId = "genre_${genreKey.name}"
                    val genreRowState = rowStates.getOrPut(genreRowId) { TvLazyListState() }


                    val genreFocusRequesters = rememberTvShowRowFocusRequesters(
                        tvShows = tvShows,
                        rowIndex = genreRowIndex,
                        focusRequesters = focusRequesters,
                        focusManagementConfig = focusManagementConfig
                    )

                    ImmersiveShowsList(
                        tvShows = tvShows,
                        sectionTitle = genreKey.name,
                        onTvShowClick = onTvShowClick,
                        setSelectedTvShow = { tvShow ->
                            carouselScrollEnabled = false
                            val imageUrl = tvShow.backdropImageUrl
                            setSelectedTvShow(tvShow)
                            imageUrl?.let {
                                backgroundState.load(url = it)
                            }
                        },
                        lazyRowState = genreRowState,
                        focusRequesters = genreFocusRequesters,
                        onItemFocused = { tvShow, index ->
                            lastFocusedItem = Pair(genreRowIndex, index)
                            shouldRestoreFocus = false
                            clearCatalogDetails = false
                        },
                        clearDetailsSignal = clearCatalogDetails
                    )
                }
            }
        }

        // Catalog rows
        items(
            count = catalogToLazyPagingItems.size,
            key = { catalog ->
                catalogToLazyPagingItems.keys.elementAtOrNull(catalog)?.hashCode() ?: catalog
            },
            contentType = { "TvShowsRow" }
        ) { catalogIndex ->
            val catalogKey =
                catalogToLazyPagingItems.keys.elementAtOrNull(catalogIndex) ?: return@items
            val tvShows = catalogToLazyPagingItems[catalogKey]

            if (tvShows != null && tvShows.itemCount > 0) {
                val catalogRowIndex = 2 + catalogIndex
                val catalogRowId = "catalog_${catalogKey.name}"
                val catalogRowState = rowStates.getOrPut(catalogRowId) { TvLazyListState() }


                val catalogFocusRequesters = rememberTvShowRowFocusRequesters(
                    tvShows = tvShows,
                    rowIndex = catalogRowIndex,
                    focusRequesters = focusRequesters,
                    focusManagementConfig = focusManagementConfig
                )

                ImmersiveShowsList(
                    tvShows = tvShows,
                    sectionTitle = catalogKey.name,
                    onTvShowClick = onTvShowClick,
                    setSelectedTvShow = { tvShow ->
                        carouselScrollEnabled = false
                        val imageUrl = tvShow.backdropImageUrl
                        setSelectedTvShow(tvShow)
                        imageUrl?.let {
                            backgroundState.load(url = it)
                        }
                    },
                    lazyRowState = catalogRowState,
                    focusRequesters = catalogFocusRequesters,
                    onItemFocused = { tvShow, index ->
                        lastFocusedItem = Pair(catalogRowIndex, index)
                        shouldRestoreFocus = false
                        clearCatalogDetails = false
                    },
                    clearDetailsSignal = clearCatalogDetails
                )

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

