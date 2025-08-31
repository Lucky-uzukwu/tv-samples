// ABOUTME: UI state sealed class for Sports screen
// ABOUTME: Handles Loading, Ready, and Error states for sports content display

/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.wiltv.presentation.screens.sports

import androidx.paging.PagingData
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.data.entities.SportType
import com.google.wiltv.presentation.UiText
import kotlinx.coroutines.flow.StateFlow

sealed class SportsScreenUiState {
    data object Loading : SportsScreenUiState()
    
    data class Ready(
        val sportTypeToGames: Map<SportType, StateFlow<PagingData<CompetitionGame>>>
    ) : SportsScreenUiState()
    
    data class Error(val message: UiText) : SportsScreenUiState()
}