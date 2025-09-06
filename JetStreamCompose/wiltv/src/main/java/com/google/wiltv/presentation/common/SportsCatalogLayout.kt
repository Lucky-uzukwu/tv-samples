// ABOUTME: Catalog layout composable specifically for sports content
// ABOUTME: Displays hero carousel and sport type rows with focus management

package com.google.wiltv.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
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
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.data.entities.SportType
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.screens.BackgroundState
import com.google.wiltv.presentation.utils.hasError
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SportsCatalogLayout(
    modifier: Modifier = Modifier,
    featuredGames: LazyPagingItems<CompetitionGame>,
    sportTypeToGames: Map<SportType, StateFlow<PagingData<CompetitionGame>>>,
    sportTypes: List<SportType>,
    onGameClick: (game: CompetitionGame) -> Unit,
    carouselState: CarouselState,
    backgroundState: BackgroundState,
    contentDescription: String = "Sports Catalog Screen",
    focusManagementConfig: FocusManagementConfig? = null,
    onRowError: ((errorText: UiText) -> Unit)? = null
) {
    val tvLazyColumnState = rememberTvLazyListState()
    val rowStates = remember { mutableStateMapOf<String, TvLazyListState>() }

    val sportTypeToLazyPagingItems = sportTypeToGames.mapValues { (sportType, flow) ->
        flow.collectAsLazyPagingItems()
    }

    val (carouselFocusRequester, firstLazyRowItemUnderCarouselRequester) = remember { FocusRequester.createRefs() }

    var carouselScrollEnabled by remember { mutableStateOf(true) }

    val focusRequesters = remember(sportTypes.size, sportTypeToGames.size) {
        mutableMapOf<ItemPosition, FocusRequester>().apply {
            // Pre-create focus requesters for first sport row that carousel needs
            for (i in 0 until sportTypes.size) {
                put(Pair(1, i), FocusRequester())
            }
        }
    }
    var lastFocusedItem by rememberSaveable { mutableStateOf(Pair(0, 0)) }
    var shouldRestoreFocus by remember { mutableStateOf(true) }
    var carouselTargetSportTypeProvider by rememberSaveable { mutableIntStateOf(0) }  // Persist across config changes

    LaunchedEffect(
        lastFocusedItem, sportTypes.size, shouldRestoreFocus,
        sportTypeToLazyPagingItems.size,
    ) {
        // Initialize carousel target from focus restoration state
        if (lastFocusedItem.first == 1 &&
            lastFocusedItem.second >= 0 &&
            lastFocusedItem.second < sportTypes.size
        ) {
            carouselTargetSportTypeProvider = lastFocusedItem.second
        }

        // Focus restoration with viewport safety - single coroutine prevents races
        if (shouldRestoreFocus &&
            lastFocusedItem.first >= 0 && lastFocusedItem.second >= 0 &&
            lastFocusedItem != Pair(-1, -1)
        ) {

            // Short initial delay to beat drawer focus request
            delay(50)
            Logger.d { "Starting focus restoration for position: $lastFocusedItem" }

            val sportTypesCount = sportTypes.size

            if (lastFocusedItem.first == 1 &&
                lastFocusedItem.second >= 0 &&
                lastFocusedItem.second < sportTypesCount
            ) {
                // Restore streaming provider focus with bounds checking
                val sportTypeRowState = rowStates["row_sport_type"]
                if (sportTypeRowState != null) {
                    try {
                        // Ensure index is within bounds before scrolling
                        if (lastFocusedItem.second < sportTypesCount) {
                            sportTypeRowState.scrollToItem(lastFocusedItem.second)
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
                val sportTypes = sportTypeToLazyPagingItems.keys.toList()

                if (adjustedRowIndex < sportTypes.size) {
                    // It's a catalog row
                    val sportType = sportTypes[adjustedRowIndex]
                    val sportTypeRowId = "sport_${sportType.name}"
                    val sportTypeRowState = rowStates[sportTypeRowId]
                    val sportTypePagingItems = sportTypeToLazyPagingItems[sportType]

                    if (sportTypeRowState != null && sportTypePagingItems != null) {
                        try {
                            // Check bounds before scrolling - with configurable performance limit
                            val maxFocusItems =
                                focusManagementConfig?.maxFocusRequestersPerRow ?: 50
                            val maxScrollIndex =
                                minOf(sportTypePagingItems.itemCount - 1, maxFocusItems - 1)
                            val safeScrollIndex = minOf(lastFocusedItem.second, maxScrollIndex)

                            if (safeScrollIndex >= 0 && safeScrollIndex < sportTypePagingItems.itemCount) {
                                sportTypeRowState.scrollToItem(safeScrollIndex)
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
        item(contentType = "SportsHeroCarousel") {
            val targetSportTypeIconFocusRequester =
                if (sportTypes.isNotEmpty()) {
                    val targetIndex = if (carouselTargetSportTypeProvider >= 0 &&
                        carouselTargetSportTypeProvider < sportTypes.size
                    ) {
                        carouselTargetSportTypeProvider
                    } else {
                        0
                    }
                    focusRequesters[Pair(1, targetIndex)] ?: firstLazyRowItemUnderCarouselRequester
                } else {
                    firstLazyRowItemUnderCarouselRequester
                }

            SportsHeroCarousel(
                games = featuredGames,
                onGameClick = onGameClick,
                setSelectedGame = { game ->
                    setSelectedGame(backgroundState, game)
                },
                carouselState = carouselState,
                modifier = Modifier
                    .height(340.dp)
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.hasFocus) {
                            carouselScrollEnabled = true
                        }
                    },
                firstLazyRowItemUnderCarouselRequester = targetSportTypeIconFocusRequester,
                carouselFocusRequester = carouselFocusRequester,
            )
        }

        item(
            contentType = "SportTypeRow",
            key = "SportTypeRow"
        ) {
            val rowId = "row_sport_type"
            val rowState = rowStates.getOrPut(rowId) { TvLazyListState() }
            val sportTypeRowIndex = 1

            val sportTypeFocusRequesters =
                remember(sportTypes.size) {
                    sportTypes.mapIndexed { index, _ ->
                        index to (focusRequesters.getOrPut(
                            Pair(
                                sportTypeRowIndex,
                                index
                            )
                        ) { FocusRequester() })
                    }.toMap()
                }

            SportTypeRow(
                sportTypes = sportTypes,
                onClick = { streamingProvider, itemIndex -> },
                modifier = Modifier,
                aboveFocusRequester = carouselFocusRequester,
                lazyRowState = rowState,
                focusRequesters = sportTypeFocusRequesters,
                downFocusRequester = null,
                onItemFocused = { itemIndex ->
                    lastFocusedItem = Pair(sportTypeRowIndex, itemIndex)
                    shouldRestoreFocus = false

                    if (itemIndex >= 0 && itemIndex < (sportTypes.size)) {
                        carouselTargetSportTypeProvider = itemIndex
                    }
                }
            )
        }

        items(
            count = sportTypeToLazyPagingItems.size,
            key = { sportIndex ->
                sportTypeToLazyPagingItems.keys.elementAtOrNull(sportIndex)?.hashCode()
                    ?: sportIndex
            },
            contentType = { "GamesRow" }
        ) { sportIndex ->
            val sportType =
                sportTypeToLazyPagingItems.keys.elementAtOrNull(sportIndex) ?: return@items
            val games = sportTypeToLazyPagingItems[sportType]


            val shouldRenderRow = games != null && (games.itemCount > 0 || games.hasError())
            Logger.d { "🏈 Sport '${sportType.name}' render check - games!=null: ${games != null}, itemCount: ${games?.itemCount ?: 0}, hasError: ${games?.hasError() ?: false}, shouldRender: $shouldRenderRow" }

            if (shouldRenderRow) {
                val rowIndex = sportIndex + 1
                val rowId = "sport_${sportType.name}"
                val rowState = rowStates.getOrPut(rowId) { TvLazyListState() }

                Logger.d { "🏈 Rendering sport row '${sportType.name}' at index $rowIndex" }

                val sportRowFocusRequesters = rememberCompetitionGameRowFocusRequesters(
                    games = games,
                    rowIndex = sportIndex,
                    focusRequesters = focusRequesters,
                    focusManagementConfig = focusManagementConfig
                )

                GamesRow(
                    games = games,
                    title = sportType.name,
                    onGameSelected = onGameClick,
                    lazyRowState = rowState,
                    focusRequesters = sportRowFocusRequesters,
                    onItemFocused = { game, itemIndex ->
                        lastFocusedItem = Pair(rowIndex, itemIndex)
                        shouldRestoreFocus = false
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                Logger.w { "🏈 NOT rendering sport row '${sportType.name}' - games!=null: ${games != null}, itemCount: ${games?.itemCount ?: 0}, hasError: ${games?.hasError() ?: false}" }
            }
        }

        item(
            contentType = "InvisibleFallbackRow",
            key = "invisible_fallback_row"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .focusRequester(firstLazyRowItemUnderCarouselRequester)
                    .focusProperties {
                        up = carouselFocusRequester
                    }
            ) {
            }
        }
    }
}

private fun setSelectedGame(
    backgroundState: BackgroundState,
    game: CompetitionGame
) {
    backgroundState.clear()
//    val patternUrl = sequenceOf(
////        game.coverImageUrl,
//        game.competition.coverImageUrl,
//        game.competition.featuredImageUrl,
//        game.competition.coverImageUrl
//    ).firstOrNull { !it.isNullOrBlank() }

    game.coverImageUrl?.let { backgroundState.load(url = it) }
}