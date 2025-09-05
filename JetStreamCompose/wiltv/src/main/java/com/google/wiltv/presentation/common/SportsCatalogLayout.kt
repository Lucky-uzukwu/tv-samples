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
import com.google.wiltv.presentation.utils.getErrorState
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
    var carouselTargetSportTypeIcon by rememberSaveable { mutableIntStateOf(0) }  // Persist across config changes

    LaunchedEffect(featuredGames.itemCount) {
        if (featuredGames.itemCount > 0) {
            carouselScrollEnabled = true
            if (shouldRestoreFocus && lastFocusedItem == Pair(0, 0)) {
                delay(20)
                carouselFocusRequester.requestFocus()
                shouldRestoreFocus = false
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
                    val targetIndex = if (carouselTargetSportTypeIcon >= 0 &&
                        carouselTargetSportTypeIcon < sportTypes.size
                    ) {
                        carouselTargetSportTypeIcon
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
                        carouselTargetSportTypeIcon = itemIndex
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

            LaunchedEffect(games?.hasError()) {
                val sportName = sportType.name
                val hasError = games?.hasError() == true
                Logger.d { "🏈 LaunchedEffect triggered for sport '$sportName' - hasError: $hasError, games != null: ${games != null}" }

                if (hasError) {
                    games?.getErrorState()?.let { errorText ->
                        Logger.e { "🚨 Sport row error detected for '$sportName': $errorText" }
                        Logger.e { "🚨 Calling onRowError callback for sport '$sportName'" }
                        onRowError?.invoke(errorText)
                    } ?: run {
                        Logger.w { "🚨 hasError=true but getErrorState() returned null for sport '$sportName'" }
                    }
                } else {
                    Logger.v { "🏈 No error for sport '$sportName'" }
                }
            }

            val shouldRenderRow = games != null && (games.itemCount > 0 || games.hasError())
            Logger.d { "🏈 Sport '${sportType.name}' render check - games!=null: ${games != null}, itemCount: ${games?.itemCount ?: 0}, hasError: ${games?.hasError() ?: false}, shouldRender: $shouldRenderRow" }

            if (shouldRenderRow) {
                val rowIndex = sportIndex + 1
                val rowId = "sport_${sportType.name}"
                val rowState = rowStates.getOrPut(rowId) { TvLazyListState() }

                Logger.d { "🏈 Rendering sport row '${sportType.name}' at index $rowIndex" }

                val sportRowFocusRequesters = remember(games?.itemCount, rowIndex) {
                    if (games == null || games.itemCount == 0) {
                        emptyMap()
                    } else {
                        val maxFocusItems = focusManagementConfig?.maxFocusRequestersPerRow ?: 50
                        val limitedItemCount = minOf(games.itemCount, maxFocusItems)
                        (0 until limitedItemCount).associate { itemIndex ->
                            itemIndex to focusRequesters.getOrPut(
                                Pair(rowIndex, itemIndex)
                            ) { FocusRequester() }
                        }
                    }
                }

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
    val patternUrl = sequenceOf(
        game.competition.logoUrl,
        game.competition.featuredImageUrl,
        game.competition.coverImageUrl
    ).firstOrNull { !it.isNullOrBlank() }

    patternUrl?.let { backgroundState.load(url = it) }
}