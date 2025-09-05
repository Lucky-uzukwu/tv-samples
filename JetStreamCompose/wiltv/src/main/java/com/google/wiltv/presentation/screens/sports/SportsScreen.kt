// ABOUTME: Main Sports screen composable for displaying live sports games
// ABOUTME: Shows hero carousel and sport type rows using catalog layout pattern
package com.google.wiltv.presentation.screens.sports

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.presentation.common.FocusManagementConfig
import com.google.wiltv.presentation.common.SportsCatalogLayout
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.screens.backgroundImageState
import com.google.wiltv.presentation.screens.home.carouselSaver
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.utils.getErrorState
import com.google.wiltv.presentation.utils.hasError

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun SportsScreen(
    onGameClick: (game: CompetitionGame) -> Unit,
    navController: NavController,
    sportsScreenViewModel: SportsScreenViewModel = hiltViewModel(),
) {
    val uiState by sportsScreenViewModel.uiState.collectAsStateWithLifecycle()
    val featuredGames = sportsScreenViewModel.heroSectionGames.collectAsLazyPagingItems()
    val carouselState = rememberSaveable(saver = carouselSaver) { CarouselState(0) }

    LaunchedEffect(featuredGames.loadState) {
        if (featuredGames.hasError()) {
            featuredGames.getErrorState()?.let { errorText ->
                sportsScreenViewModel.handlePagingError(errorText)
            }
        }
    }

    when (val currentState = uiState) {
        is SportsScreenUiState.Ready -> {
            val backgroundState = backgroundImageState()
            SportsCatalogLayout(
                featuredGames = featuredGames,
                sportTypeToGames = currentState.sportTypeToGames,
                sportTypes = currentState.sportTypes,
                onGameClick = onGameClick,
                carouselState = carouselState,
                backgroundState = backgroundState,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "Sports Screen",
                focusManagementConfig = FocusManagementConfig(
                    enableFocusRestoration = true,
                    enableFocusLogging = true
                ),
                onRowError = { errorText: UiText ->
                    sportsScreenViewModel.handlePagingError(errorText)
                }
            )
        }

        is SportsScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is SportsScreenUiState.Error -> ErrorScreen(
            uiText = currentState.message,
            onRetry = {
                sportsScreenViewModel.retryOperation()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}