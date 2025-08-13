// ABOUTME: TV channel-specific catalog layout composable for TV streaming screens
// ABOUTME: Handles hero carousel, genre rows, and optional streaming providers with focus management for TV channels

package com.google.wiltv.presentation.common

import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.screens.BackgroundState
import com.google.wiltv.presentation.theme.WilTvBorderWidth
import com.google.wiltv.presentation.theme.WilTvCardShape
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
    val focusRequesters =
        remember(genres.size, genreToTvChannels.size) {
            mutableMapOf<Pair<Int, Int>, FocusRequester>().apply {
                // Pre-create focus requesters for genres
                for (i in 0 until genres.size) {
                    put(Pair(1, i), FocusRequester())
                }
            }
        }
    var lastFocusedItem by rememberSaveable { mutableStateOf(Pair(0, 0)) }
    var shouldRestoreFocus by remember { mutableStateOf(true) }
    var clearChannelDetails by remember { mutableStateOf(false) }
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

            val genreCount = genres.size

            if (lastFocusedItem.first == 1 &&
                lastFocusedItem.second >= 0 &&
                lastFocusedItem.second < genreCount
            ) {
                // Restore genre focus with bounds checking
                val genreRowState = rowStates["row_genres"]
                if (genreRowState != null) {
                    try {
                        // Ensure index is within bounds before scrolling
                        if (lastFocusedItem.second < genreCount) {
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
            } else if (lastFocusedItem.first >= 2) {
                // Determine if this is a catalog or genre row
                val streamingRowOffset = 1
                val adjustedRowIndex = lastFocusedItem.first - 1 - streamingRowOffset
                val genreKeys = genreToLazyPagingItems?.keys?.toList() ?: emptyList()

                if (adjustedRowIndex < genreKeys.size) {
                    // It's a genre row
                    val genreIndex = adjustedRowIndex
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

        item(contentType = "TvChannelHeroSectionCarousel") {
            val targetGenreFocusRequester =
                if (genres.isNotEmpty()) {
                    val targetIndex = if (carouselTargetGenre >= 0 &&
                        carouselTargetGenre < genres.size
                    ) {
                        carouselTargetGenre
                    } else {
                        0
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
                remember(genres.size) {
                    genres.mapIndexed { index, _ ->
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

                    if (itemIndex >= 0 && itemIndex < (genres.size)) {
                        carouselTargetGenre = itemIndex
                    }
                }
            )
        }

        // Genre rows
//        items(
//            count = genreToLazyPagingItems.size,
//            key = { genre ->
//                genreToLazyPagingItems.keys.elementAtOrNull(genre)?.hashCode() ?: genre
//            },
//            contentType = { "TvChannelsRow" }
//        ) { genreIndex ->
//            val genreKey =
//                genreToLazyPagingItems.keys.elementAtOrNull(genreIndex) ?: return@items
//            val tvChannels = genreToLazyPagingItems[genreKey]
//
//            if (tvChannels != null && tvChannels.itemCount > 0) {
//                val genreRowIndex = 2 + genreIndex
//                val genreRowId = "genre_${genreKey.name}"
//                val genreRowState = rowStates.getOrPut(genreRowId) { TvLazyListState() }
//
//                val genreFocusRequesters = rememberChannelRowFocusRequesters(
//                    tvChannels = tvChannels,
//                    rowIndex = genreRowIndex,
//                    focusRequesters = focusRequesters,
//                    focusManagementConfig = focusManagementConfig
//                )
//
//                ImmersiveTvChannelsList(
//                    tvChannels = tvChannels,
//                    sectionTitle = genreKey.name,
//                    onChannelClick = onChannelClick,
//                    keyPrefix = "genre_${genreKey.id}",
//                    setSelectedTvChannel = { channel ->
//                        carouselScrollEnabled = false
//                        val imageUrl = channel.logoUrl
//                        // Channel background loading could be done here if needed
//                        imageUrl?.let {
//                            backgroundState.load(url = it)
//                        }
//                    },
//                    lazyRowState = genreRowState,
//                    focusRequesters = genreFocusRequesters,
//                    onItemFocused = { channel, index ->
//                        lastFocusedItem = Pair(genreRowIndex, index)
//                        shouldRestoreFocus = false
//                        clearChannelDetails = false
//                    },
//                    clearDetailsSignal = clearChannelDetails
//                )
//            }
//        }

        // Invisible bottom row
        item(
            contentType = "InvisibleBottomRow",
            key = "invisible_bottom_row"
        ) {
            InvisibleBottomRow(
                onFocused = {
                    lastFocusedItem = Pair(-1, -1)
                    clearChannelDetails = true
                }
            )
        }
    }
}

@Composable
fun ImmersiveTvChannelsList(
    tvChannels: LazyPagingItems<TvChannel>,
    sectionTitle: String? = null,
    modifier: Modifier = Modifier,
    keyPrefix: String = "channel",
    setSelectedTvChannel: (TvChannel) -> Unit,
    onChannelClick: (channel: TvChannel) -> Unit,
    lazyRowState: TvLazyListState? = null,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
    onItemFocused: (TvChannel, Int) -> Unit = { _, _ -> },
    clearDetailsSignal: Boolean = false
) {
    SimpleTvChannelsRow(
        tvChannels = tvChannels,
        title = sectionTitle,
        onChannelSelected = { channel ->
            setSelectedTvChannel(channel)
            onChannelClick(channel)
        },
        focusRequesters = focusRequesters,
        onItemFocused = onItemFocused,
        modifier = modifier,
        keyPrefix = keyPrefix
    )
}

@Composable
fun SimpleTvChannelsRow(
    tvChannels: LazyPagingItems<TvChannel>,
    title: String?,
    onChannelSelected: (TvChannel) -> Unit,
    focusRequesters: Map<Int, FocusRequester>,
    onItemFocused: (TvChannel, Int) -> Unit,
    modifier: Modifier = Modifier,
    keyPrefix: String = "channel"
) {
    Column(
        modifier = modifier.focusGroup()
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 30.sp
                ),
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .alpha(1f)
                    .padding(start = 8.dp, top = 16.dp, bottom = 16.dp)
            )
        }

        TvLazyRow(
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(
                count = tvChannels.itemCount,
                key = { index -> "${keyPrefix}_${tvChannels[index]?.id ?: "index_$index"}" }) { index ->
                val channel = tvChannels[index] ?: return@items
                val focusRequester = focusRequesters[index]

                var isFocused by remember { mutableStateOf(false) }

                MovieCard(
                    onClick = { onChannelSelected(channel) },
                    modifier = Modifier
                        .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                        .border(
                            width = WilTvBorderWidth,
                            color = if (isFocused) Color.White else Color.Transparent,
                            shape = WilTvCardShape
                        )
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (focusState.isFocused) {
                                onItemFocused(channel, index)
                            }
                        }
                ) {
                    AuthenticatedAsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(16F / 9F)
                    )
                }
            }
        }
    }
}

@Composable
fun rememberChannelRowFocusRequesters(
    tvChannels: LazyPagingItems<TvChannel>?,
    rowIndex: Int,
    focusRequesters: MutableMap<Pair<Int, Int>, FocusRequester>,
    focusManagementConfig: FocusManagementConfig?
): Map<Int, FocusRequester> {
    return remember(tvChannels?.itemCount, rowIndex) {
        if (tvChannels == null || tvChannels.itemCount == 0) {
            emptyMap()
        } else {
            val itemCount = tvChannels.itemCount
            val snapshotSize = tvChannels.itemSnapshotList.items.size
            val actualItemCount = minOf(itemCount, snapshotSize)
            val maxFocusItems = focusManagementConfig?.maxFocusRequestersPerRow ?: 50
            val limitedItemCount = minOf(actualItemCount, maxFocusItems)

            // Create focus requesters for this row
            (0 until limitedItemCount).associateWith { itemIndex ->
                focusRequesters.getOrPut(Pair(rowIndex, itemIndex)) { FocusRequester() }
            }
        }
    }
}