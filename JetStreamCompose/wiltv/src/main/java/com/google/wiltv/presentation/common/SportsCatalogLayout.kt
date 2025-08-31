// ABOUTME: Catalog layout composable specifically for sports content
// ABOUTME: Displays hero carousel and sport type rows with focus management

package com.google.wiltv.presentation.common

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.data.entities.SportType
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.screens.BackgroundState
import com.google.wiltv.presentation.screens.dashboard.ParentPadding
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SportsCatalogLayout(
    modifier: Modifier = Modifier,
    featuredGames: LazyPagingItems<CompetitionGame>,
    sportTypeToGames: Map<SportType, StateFlow<PagingData<CompetitionGame>>>,
    onGameClick: (game: CompetitionGame) -> Unit,
    carouselState: CarouselState,
    backgroundState: BackgroundState,
    contentDescription: String = "Sports Catalog Screen",
    focusManagementConfig: FocusManagementConfig? = null,
    onRowError: ((errorText: UiText) -> Unit)? = null
) {
    val tvLazyColumnState = rememberTvLazyListState()
    val rowStates = remember { mutableStateMapOf<String, TvLazyListState>() }

    val (carouselFocusRequester, firstLazyRowItemUnderCarouselRequester) = remember { FocusRequester.createRefs() }
    var carouselScrollEnabled by remember { mutableStateOf(true) }

    val focusRequesters = remember(sportTypeToGames.size) {
        mutableMapOf<Pair<Int, Int>, FocusRequester>()
    }
    var lastFocusedItem by rememberSaveable { mutableStateOf(Pair(0, 0)) }
    var shouldRestoreFocus by remember { mutableStateOf(true) }

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

//    Box(modifier = modifier.semantics { this.contentDescription = contentDescription }) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(MaterialTheme.colorScheme.background)
//        ) {
//            backgroundState.drawable.value?.let { imageBitmap ->
//                androidx.compose.foundation.Image(
//                    bitmap = imageBitmap,
//                    contentDescription = null,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxSize()
//                )
//            }
//        }


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
        item {
            if (featuredGames.itemCount > 0) {
                SportsHeroCarousel(
                    games = featuredGames,
                    onGameClick = onGameClick,
                    setSelectedGame = { game ->
                        backgroundState.clear()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(432.dp),
                    carouselState = carouselState,
                    carouselScrollEnabled = carouselScrollEnabled,
                    carouselFocusRequester = carouselFocusRequester,
                    firstLazyRowItemUnderCarouselRequester = firstLazyRowItemUnderCarouselRequester
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(432.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Loading Sports...",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        sportTypeToGames.toList().forEachIndexed { index, (sportType, gamesFlow) ->
            item(
                key = "sport_${sportType.id}",
                contentType = "GamesRow"
            ) {
                val rowIndex = index + 1
                val rowId = "sport_${sportType.name}"
                val rowState = rowStates.getOrPut(rowId) { TvLazyListState() }
                val gamesAsLazyItems = gamesFlow.collectAsLazyPagingItems()

                val sportRowFocusRequesters = remember(gamesAsLazyItems.itemCount, rowIndex) {
                    val maxFocusItems = focusManagementConfig?.maxFocusRequestersPerRow ?: 50
                    val limitedItemCount = minOf(gamesAsLazyItems.itemCount, maxFocusItems)
                    (1 until limitedItemCount).associate { itemIndex ->
                        itemIndex to focusRequesters.getOrPut(
                            Pair(
                                rowIndex,
                                itemIndex
                            )
                        ) { FocusRequester() }
                    }
                }

                GamesRow(
                    games = gamesFlow,
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