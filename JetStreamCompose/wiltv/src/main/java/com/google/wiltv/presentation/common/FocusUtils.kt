// ABOUTME: Focus management utilities for TV navigation and state restoration
// ABOUTME: Provides reusable focus tracking, restoration, and navigation logic for catalog screens

package com.google.wiltv.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.network.Catalog

data class FocusManagementConfig(
    val enableFocusRestoration: Boolean = true,
    val enableFocusLogging: Boolean = true,
    val maxFocusRequestersPerRow: Int = 50,  // Increased from hardcoded 20
    val focusRestorationDelayMs: Long = 300L,  // Configurable delay
    val enableMemoryOptimization: Boolean = true,  // Enable cleanup of unused FocusRequesters
    val enableErrorRecovery: Boolean = true  // Enable fallback focus behavior on errors
)

class FocusManager {
    var tvLazyColumnState = TvLazyListState()
    val rowStates = mutableStateMapOf<String, TvLazyListState>()
    val focusRequesters = mutableMapOf<Pair<Int, Int>, FocusRequester>()
    
    var lastFocusedItem by mutableStateOf(Pair(0, 0))  // Will be replaced by saveable version
    var shouldRestoreFocus by mutableStateOf(true)
    var clearCatalogDetails by mutableStateOf(false)
    var carouselTargetStreamingProvider by mutableStateOf(0)
    
    fun getOrCreateRowState(rowId: String): TvLazyListState {
        return rowStates.getOrPut(rowId) { TvLazyListState() }
    }
    
    fun getOrCreateFocusRequester(row: Int, item: Int): FocusRequester {
        return focusRequesters.getOrPut(Pair(row, item)) { FocusRequester() }
    }
    
    fun getCarouselTargetFocusRequester(streamingProviderCount: Int, fallback: FocusRequester): FocusRequester {
        if (streamingProviderCount == 0) {
            Logger.d { "No streaming providers, using fallback focus requester" }
            return fallback
        }
        
        val targetStreamingProviderIndex = if (carouselTargetStreamingProvider >= 0 && 
            carouselTargetStreamingProvider < streamingProviderCount) {
            carouselTargetStreamingProvider
        } else {
            Logger.d { "Invalid or initial carousel target ($carouselTargetStreamingProvider), using first provider. Total providers: $streamingProviderCount" }
            0
        }
        
        val focusRequester = focusRequesters[Pair(1, targetStreamingProviderIndex)]
        if (focusRequester != null) {
            Logger.d { "Found focus requester for streaming provider $targetStreamingProviderIndex" }
            return focusRequester
        } else {
            Logger.w { "FocusRequester not found for streaming provider $targetStreamingProviderIndex (may not be created yet), using fallback" }
            return fallback
        }
    }
    
    fun onItemFocused(row: Int, item: Int, totalItems: Int, movie: MovieNew? = null) {
        Logger.i { "Focusing: Row=$row, Item=$item, Total=$totalItems" + (movie?.let { ", Movie=${it.title}" } ?: "") }
        lastFocusedItem = Pair(row, item)
        shouldRestoreFocus = false
        clearCatalogDetails = false
        
        if (row == 1 && item >= 0 && item < totalItems) {
            carouselTargetStreamingProvider = item
            Logger.d { "Updated carousel target to streaming provider: $item" }
        }
    }
    
    fun onInvisibleRowFocused() {
        lastFocusedItem = Pair(-1, -1)
        clearCatalogDetails = true
    }
}

@Composable
fun rememberFocusManager(
    config: FocusManagementConfig,
    streamingProviderCount: Int = 0
): FocusManager {
    val focusManager = remember(streamingProviderCount) { 
        val manager = FocusManager()
        
        // Pre-create focus requesters for streaming providers that carousel needs
        if (streamingProviderCount > 0) {
            // Create focus requesters for streaming providers (row 1) synchronously
            for (i in 0 until streamingProviderCount) {
                manager.getOrCreateFocusRequester(1, i)
            }
            Logger.d { "Pre-created ${streamingProviderCount} focus requesters for streaming providers" }
        }
        
        manager
    }
    
    // Initialize focus manager state
    focusManager.tvLazyColumnState = rememberTvLazyListState()
    
    return focusManager
}

@Composable
fun FocusRestorationEffect(
    focusManager: FocusManager,
    streamingProviderCount: Int,
    catalogToLazyPagingItems: Map<Catalog, androidx.paging.compose.LazyPagingItems<MovieNew>>
) {
    LaunchedEffect(focusManager.lastFocusedItem, streamingProviderCount) {
        if (focusManager.lastFocusedItem.first == 1 && 
            focusManager.lastFocusedItem.second >= 0 && 
            focusManager.lastFocusedItem.second < streamingProviderCount) {
            focusManager.carouselTargetStreamingProvider = focusManager.lastFocusedItem.second
        }
    }

    LaunchedEffect(Unit) {
        if (focusManager.shouldRestoreFocus && 
            focusManager.lastFocusedItem.first >= 0 && focusManager.lastFocusedItem.second >= 0 && 
            focusManager.lastFocusedItem != Pair(-1, -1)) {
            
            if (focusManager.lastFocusedItem.first == 1 && focusManager.lastFocusedItem.second < streamingProviderCount) {
                restoreStreamingProviderFocus(focusManager, streamingProviderCount)
            } else if (focusManager.lastFocusedItem.first >= 2) {
                restoreCatalogFocus(focusManager, catalogToLazyPagingItems)
            } else {
                Logger.w { "Focus restoration skipped - invalid bounds: row=${focusManager.lastFocusedItem.first}, item=${focusManager.lastFocusedItem.second}, providers=$streamingProviderCount" }
            }
        }
    }
}

private suspend fun restoreStreamingProviderFocus(focusManager: FocusManager, streamingProviderCount: Int) {
    Logger.i { "Attempting focus restoration: row=${focusManager.lastFocusedItem.first}, item=${focusManager.lastFocusedItem.second}, total providers=$streamingProviderCount" }

    val streamingRowState = focusManager.rowStates["row_streaming_providers"]
    if (streamingRowState != null) {
        try {
            streamingRowState.scrollToItem(focusManager.lastFocusedItem.second)
            Logger.i { "Scrolled to streaming provider item: ${focusManager.lastFocusedItem.second}" }
            
            kotlinx.coroutines.delay(200)
            
            val focusRequester = focusManager.focusRequesters[focusManager.lastFocusedItem]
            if (focusRequester != null) {
                try {
                    focusRequester.requestFocus()
                    Logger.i { "Focus restoration successful" }
                    focusManager.shouldRestoreFocus = false
                } catch (e: Exception) {
                    Logger.w(e) { "Failed to request focus for streaming provider ${focusManager.lastFocusedItem}" }
                }
            } else {
                Logger.w { "FocusRequester not found for ${focusManager.lastFocusedItem} after scroll" }
            }
        } catch (e: Exception) {
            Logger.w(e) { "Failed to scroll to streaming provider item: ${focusManager.lastFocusedItem.second}" }
        }
    } else {
        Logger.w { "StreamingRowState not found for focus restoration" }
    }
}

private suspend fun restoreCatalogFocus(
    focusManager: FocusManager,
    catalogToLazyPagingItems: Map<Catalog, LazyPagingItems<MovieNew>>
) {
    Logger.i { "Attempting catalog row focus restoration: row=${focusManager.lastFocusedItem.first}, item=${focusManager.lastFocusedItem.second}" }
    
    val catalogRowIndex = focusManager.lastFocusedItem.first - 2
    val catalogKeys = catalogToLazyPagingItems.keys.toList()
    
    if (catalogRowIndex < catalogKeys.size) {
        val catalogKey = catalogKeys[catalogRowIndex]
        val catalogRowId = "catalog_${catalogKey.name}"
        val catalogRowState = focusManager.rowStates[catalogRowId]
        val catalogMovies = catalogToLazyPagingItems[catalogKey]
        
        val safeItemIndex = focusManager.lastFocusedItem.second
        val actualItemCount = catalogMovies?.itemCount ?: 0
        val actualSnapshotSize = catalogMovies?.itemSnapshotList?.items?.size ?: 0
        val maxSafeIndex = minOf(actualItemCount, actualSnapshotSize) - 1
        
        if (catalogRowState != null && catalogMovies != null && 
            safeItemIndex >= 0 && safeItemIndex <= maxSafeIndex &&
            catalogMovies.itemSnapshotList.items.getOrNull(safeItemIndex) != null) {
            
            try {
                catalogRowState.scrollToItem(safeItemIndex)
                Logger.i { "Scrolled to catalog row $catalogRowId item: $safeItemIndex" }
                
                kotlinx.coroutines.delay(300)
                
                val visibleItems = catalogRowState.layoutInfo.visibleItemsInfo
                val isItemVisible = visibleItems.any { it.index == safeItemIndex }
                
                if (isItemVisible) {
                    val focusRequester = focusManager.focusRequesters[focusManager.lastFocusedItem]
                    if (focusRequester != null) {
                        try {
                            focusRequester.requestFocus()
                            Logger.i { "Catalog row focus restoration successful for visible item $safeItemIndex" }
                            focusManager.shouldRestoreFocus = false
                        } catch (e: Exception) {
                            Logger.w(e) { "Failed to request focus for visible catalog item ${focusManager.lastFocusedItem}" }
                        }
                    } else {
                        Logger.w { "FocusRequester not found for catalog ${focusManager.lastFocusedItem} after scroll" }
                    }
                } else {
                    handleFallbackFocus(focusManager, visibleItems, focusManager.lastFocusedItem.first)
                }
            } catch (e: Exception) {
                Logger.w(e) { "Failed to scroll to catalog row $catalogRowId item: $safeItemIndex" }
            }
        } else {
            Logger.w { "Catalog row state or movies not found for focus restoration: rowState=${catalogRowState != null}, movies=${catalogMovies != null}" }
        }
    } else {
        Logger.w { "Catalog row index out of bounds: $catalogRowIndex >= ${catalogKeys.size}" }
    }
}

private suspend fun handleFallbackFocus(
    focusManager: FocusManager,
    visibleItems: List<androidx.tv.foundation.lazy.list.TvLazyListItemInfo>,
    actualRowIndex: Int
) {
    val firstVisibleIndex = visibleItems.firstOrNull()?.index
    if (firstVisibleIndex != null) {
        val fallbackFocusRequester = focusManager.focusRequesters[Pair(actualRowIndex, firstVisibleIndex)]
        if (fallbackFocusRequester != null) {
            try {
                fallbackFocusRequester.requestFocus()
                Logger.i { "Focus restored to first visible item $firstVisibleIndex instead of target ${focusManager.lastFocusedItem.second}" }
                focusManager.lastFocusedItem = Pair(actualRowIndex, firstVisibleIndex)
                focusManager.shouldRestoreFocus = false
            } catch (e: Exception) {
                Logger.w(e) { "Failed to request focus for fallback item $firstVisibleIndex" }
            }
        } else {
            Logger.w { "No fallback FocusRequester found for first visible item $firstVisibleIndex" }
        }
    }
}

@Composable
fun rememberRowFocusRequesters(
    movies: LazyPagingItems<MovieNew>?,
    rowIndex: Int,
    focusRequesters: MutableMap<Pair<Int, Int>, FocusRequester>,
    focusManagementConfig: FocusManagementConfig?
): Map<Int, FocusRequester> {
    return remember(movies?.itemCount, rowIndex) {
        if (movies == null || movies.itemCount == 0) {
            emptyMap()
        } else {
            val startTime = System.currentTimeMillis()
            
            // Use cached values to avoid multiple data access
            val itemCount = movies.itemCount
            val snapshotSize = movies.itemSnapshotList.items.size
            val actualItemCount = minOf(itemCount, snapshotSize)
            val maxFocusItems = focusManagementConfig?.maxFocusRequestersPerRow ?: 50
            val limitedItemCount = minOf(actualItemCount, maxFocusItems)
            
            // Simple range creation without expensive null checks
            // Focus requesters will be created lazily when actually needed
            val result = (0 until limitedItemCount).associate { index ->
                index to focusRequesters.getOrPut(Pair(rowIndex, index)) { FocusRequester() }
            }
            
            val endTime = System.currentTimeMillis()
            Logger.d { "Row $rowIndex focus requesters created in ${endTime - startTime}ms (${result.size} items)" }
            
            result
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InvisibleBottomRow(
    onFocused: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .focusProperties {
                down = FocusRequester.Default
            }
            .onFocusChanged { focusState: FocusState ->
                if (focusState.hasFocus) {
                    onFocused()
                }
            }
    ) {
        // Empty content - this row is invisible to the user
    }
}