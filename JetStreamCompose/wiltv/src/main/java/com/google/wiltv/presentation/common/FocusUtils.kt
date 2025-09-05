// ABOUTME: Focus management utilities for TV navigation and state restoration
// ABOUTME: Provides reusable focus tracking, restoration, and navigation logic for catalog screens

package com.google.wiltv.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import co.touchlab.kermit.Logger
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.TvShow

data class FocusManagementConfig(
    val enableFocusRestoration: Boolean = true,
    val enableFocusLogging: Boolean = true,
    val maxFocusRequestersPerRow: Int = 50,  // Increased from hardcoded 20
    val focusRestorationDelayMs: Long = 300L,  // Configurable delay
    val enableMemoryOptimization: Boolean = true,  // Enable cleanup of unused FocusRequesters
    val enableErrorRecovery: Boolean = true  // Enable fallback focus behavior on errors
)

@Composable
fun rememberMovieRowFocusRequesters(
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

@Composable
fun rememberTvShowRowFocusRequesters(
    tvShows: LazyPagingItems<TvShow>?,
    rowIndex: Int,
    focusRequesters: MutableMap<Pair<Int, Int>, FocusRequester>,
    focusManagementConfig: FocusManagementConfig?
): Map<Int, FocusRequester> {
    return remember(tvShows?.itemCount, rowIndex) {
        if (tvShows == null || tvShows.itemCount == 0) {
            emptyMap()
        } else {
            val startTime = System.currentTimeMillis()

            // Use cached values to avoid multiple data access
            val itemCount = tvShows.itemCount
            val snapshotSize = tvShows.itemSnapshotList.items.size
            val actualItemCount = minOf(itemCount, snapshotSize)
            val maxFocusItems = focusManagementConfig?.maxFocusRequestersPerRow ?: 50
            val limitedItemCount = minOf(actualItemCount, maxFocusItems)

            // Simple range creation without expensive null checks
            // Focus requesters will be created lazily when actually needed
            val result = (0 until limitedItemCount).associate { index ->
                index to focusRequesters.getOrPut(Pair(rowIndex, index)) { FocusRequester() }
            }

            val endTime = System.currentTimeMillis()
            Logger.d { "TV Show Row $rowIndex focus requesters created in ${endTime - startTime}ms (${result.size} items)" }

            result
        }
    }
}


@Composable
fun rememberCompetitionGameRowFocusRequesters(
    games: LazyPagingItems<CompetitionGame>?,
    rowIndex: Int,
    focusRequesters: MutableMap<Pair<Int, Int>, FocusRequester>,
    focusManagementConfig: FocusManagementConfig?
): Map<Int, FocusRequester> {
    return remember(games?.itemCount, rowIndex) {
        if (games == null || games.itemCount == 0) {
            emptyMap()
        } else {
            val startTime = System.currentTimeMillis()

            // Use cached values to avoid multiple data access
            val itemCount = games.itemCount
            val snapshotSize = games.itemSnapshotList.items.size
            val actualItemCount = minOf(itemCount, snapshotSize)
            val maxFocusItems = focusManagementConfig?.maxFocusRequestersPerRow ?: 50
            val limitedItemCount = minOf(actualItemCount, maxFocusItems)

            // Simple range creation without expensive null checks
            // Focus requesters will be created lazily when actually needed
            val result = (0 until limitedItemCount).associate { index ->
                index to focusRequesters.getOrPut(Pair(rowIndex, index)) { FocusRequester() }
            }

            val endTime = System.currentTimeMillis()
            Logger.d { "TV Show Row $rowIndex focus requesters created in ${endTime - startTime}ms (${result.size} items)" }

            result
        }
    }
}

// ABOUTME: Represents a pair of column and row indices
typealias ItemPosition = Pair<Int, Int>


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