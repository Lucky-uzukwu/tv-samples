// ABOUTME: TV channel-specific catalog layout composable for TV streaming screens
// ABOUTME: Handles hero carousel, genre rows, and optional streaming providers with focus management for TV channels

package com.google.wiltv.presentation.common

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.compose.foundation.BorderStroke
import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.screens.BackgroundState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvChannelCatalogLayout(
    modifier: Modifier = Modifier,
    featuredTvChannels: LazyPagingItems<TvChannel>,
    genreToTvChannels: Map<Genre, StateFlow<PagingData<TvChannel>>>,
    onChannelClick: (channel: TvChannel) -> Unit,
    carouselState: CarouselState,
    backgroundState: BackgroundState,
    contentDescription: String = "TV Channels Catalog Screen",
    genres: List<Genre>,
    onGenreClick: ((genre: Genre) -> Unit),
    onViewAllChannelsClick: () -> Unit,
    focusManagementConfig: FocusManagementConfig? = null
) {
    val tvLazyColumnState = rememberTvLazyListState()
    val rowStates = remember { mutableStateMapOf<String, TvLazyListState>() }

    val genreToLazyPagingItems = genreToTvChannels.mapValues { (genre, flow) ->
        flow.collectAsLazyPagingItems()
    }

    val (carouselFocusRequester, firstLazyRowItemUnderCarouselRequester) = remember { FocusRequester.createRefs() }

    var carouselScrollEnabled by remember { mutableStateOf(true) }

    // Focus management state - memoized for performance
    // Account for "All" item by adding 1 to genre count
    val totalGenreItemCount = genres.size + 1
    val focusRequesters =
        remember(totalGenreItemCount, genreToTvChannels.size) {
            mutableMapOf<Pair<Int, Int>, FocusRequester>().apply {
                // Pre-create focus requesters for "All" item + genres
                for (i in 0 until totalGenreItemCount) {
                    put(Pair(1, i), FocusRequester())
                }
            }
        }
    var lastFocusedItem by rememberSaveable { mutableStateOf(Pair(0, 0)) }
    var shouldRestoreFocus by remember { mutableStateOf(true) }
    var carouselTargetGenre by rememberSaveable { mutableIntStateOf(0) }

    // Combined focus restoration and carousel sync (if focus management enabled)
    LaunchedEffect(
        lastFocusedItem, genres.size, shouldRestoreFocus,
        genreToLazyPagingItems.size
    ) {
        // Initialize carousel target from focus restoration state
        if (lastFocusedItem.first == 1 &&
            lastFocusedItem.second >= 0 &&
            lastFocusedItem.second < genres.size
        ) {
            carouselTargetGenre = lastFocusedItem.second
        }

        // Focus restoration with viewport safety - single coroutine prevents races
        if (shouldRestoreFocus &&
            lastFocusedItem.first >= 0 && lastFocusedItem.second >= 0 &&
            lastFocusedItem != Pair(-1, -1)
        ) {

            // Short initial delay to beat drawer focus request
            delay(50)
            Logger.d { "Starting focus restoration for position: $lastFocusedItem" }

            if (lastFocusedItem.first == 1 &&
                lastFocusedItem.second >= 0 &&
                lastFocusedItem.second < totalGenreItemCount
            ) {
                // Restore genre focus with bounds checking
                val genreRowState = rowStates["row_genres"]
                if (genreRowState != null) {
                    try {
                        // Ensure index is within bounds before scrolling
                        if (lastFocusedItem.second < totalGenreItemCount) {
                            genreRowState.scrollToItem(lastFocusedItem.second)
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
                                Logger.w { "FocusRequester not found for genre at $lastFocusedItem" }
                            }
                        }
                    } catch (e: Exception) {
                        Logger.w(e) { "Failed to restore genre focus at index ${lastFocusedItem.second}" }
                        shouldRestoreFocus = false
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

        item(contentType = "TvChannelHeroSectionCarousel") {
            val targetGenreFocusRequester =
                if (genres.isNotEmpty()) {
                    // Add 1 to account for "All" item when targeting genres
                    val targetIndex = if (carouselTargetGenre >= 0 &&
                        carouselTargetGenre < genres.size
                    ) {
                        carouselTargetGenre + 1  // Offset by 1 for "All" item
                    } else {
                        0  // Default to "All" item
                    }
                    focusRequesters[Pair(1, targetIndex)] ?: firstLazyRowItemUnderCarouselRequester
                } else {
                    firstLazyRowItemUnderCarouselRequester
                }

            TvChannelHeroSectionCarousel(
                tvChannels = featuredTvChannels,
                onChannelClick = onChannelClick,
                carouselState = carouselState,
                carouselScrollEnabled = carouselScrollEnabled,
                modifier = Modifier
                    .height(340.dp)
                    .fillMaxWidth(),
                firstLazyRowItemUnderCarouselRequester = targetGenreFocusRequester,
                carouselFocusRequester = carouselFocusRequester,
                setSelectedTvChannel = { channel ->
                    val imageUrl = channel.logoUrl
                    // Channel background loading could be done here if needed
                    imageUrl.let {
                        backgroundState.load(url = it)
                    }

                },
            )
        }

        // Genre names row
        item(
            contentType = "GenreNamesRow",
            key = "genreNamesRow"
        ) {
            val rowId = "row_genres"
            val rowState = rowStates.getOrPut(rowId) { TvLazyListState() }
            val genreRowIndex = 1

            val genreFocusRequesters =
                remember(totalGenreItemCount) {
                    (0 until totalGenreItemCount).map { index ->
                        index to (focusRequesters.getOrPut(
                            Pair(
                                genreRowIndex,
                                index
                            )
                        ) { FocusRequester() })
                    }.toMap()
                }

            GenreNamesRow(
                genres = genres,
                onClick = { genre, itemIndex ->
                    onGenreClick(genre)
                },
                modifier = Modifier,
                aboveFocusRequester = carouselFocusRequester,
                lazyRowState = rowState,
                focusRequesters = genreFocusRequesters,
                downFocusRequester = null,
                onItemFocused = { itemIndex ->
                    lastFocusedItem = Pair(genreRowIndex, itemIndex)
                    shouldRestoreFocus = false

                    // Handle carousel targeting with "All" item offset
                    val adjustedIndex = if (itemIndex > 0) itemIndex - 1 else 0
                    if (adjustedIndex >= 0 && adjustedIndex < genres.size) {
                        carouselTargetGenre = adjustedIndex
                    }
                },
                onAllClick = onViewAllChannelsClick
            )
        }
    }
}