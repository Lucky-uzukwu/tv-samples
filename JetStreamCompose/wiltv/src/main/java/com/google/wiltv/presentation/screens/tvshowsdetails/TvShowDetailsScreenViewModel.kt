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

package com.google.wiltv.presentation.screens.tvshowsdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.paging.pagingsources.tvshow.TvShowPagingSources
import com.google.wiltv.data.repositories.TvShowsRepository
import com.google.wiltv.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TvShowDetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tvShowRepository: TvShowsRepository,
    userRepository: UserRepository,
) : ViewModel() {
    val uiState: StateFlow<TvShowDetailsScreenUiState> = combine(
        savedStateHandle
            .getStateFlow<String?>(TvShowDetailsScreen.TvShowIdBundleKey, null),
        userRepository.userToken,
    ) { tvShowId, userToken ->
        if (tvShowId == null || userToken == null) {
            TvShowDetailsScreenUiState.Error
        } else {
            val details = tvShowRepository.getTvShowsDetails(
                tvShowId = tvShowId,
                token = userToken
            ).firstOrNull() ?: return@combine TvShowDetailsScreenUiState.Error

            val genreId =
                if (details.genres?.isNotEmpty() == true) details.genres.first().id else 0

            val similarTvShows = getTvShowsByGenre(
                genreId = genreId,
                tvShowRepository = tvShowRepository,
                userRepository = userRepository
            )
            TvShowDetailsScreenUiState.Done(
                similarTvShows = similarTvShows,
                tvShow = details
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TvShowDetailsScreenUiState.Loading
    )


    private fun getTvShowsByGenre(
        tvShowRepository: TvShowsRepository,
        genreId: Int,
        userRepository: UserRepository
    ): StateFlow<PagingData<TvShow>> {
        return TvShowPagingSources().getTvShowsGenrePagingSource(
            genreId = genreId,
            tvShowRepository = tvShowRepository,
            userRepository = userRepository
        ).cachedIn(viewModelScope).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PagingData.empty()
        )
    }
}


sealed class TvShowDetailsScreenUiState {
    data object Loading : TvShowDetailsScreenUiState()
    data object Error : TvShowDetailsScreenUiState()
    data class Done(
        val tvShow: TvShow,
        val similarTvShows: StateFlow<PagingData<TvShow>>
    ) : TvShowDetailsScreenUiState()
}
