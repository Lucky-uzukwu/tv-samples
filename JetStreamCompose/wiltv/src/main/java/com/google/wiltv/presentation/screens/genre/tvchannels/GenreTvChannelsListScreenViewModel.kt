// ABOUTME: ViewModel for managing genre-specific TV channels list screen state
// ABOUTME: Handles genre-based TV channel search and paging data with error states

package com.google.wiltv.presentation.screens.genre.tvchannels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.paging.pagingsources.tvchannel.TvChannelPagingSources
import com.google.wiltv.data.repositories.TvChannelsRepository
import com.google.wiltv.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GenreTvChannelsListScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tvChannelsRepository: TvChannelsRepository,
    userRepository: UserRepository,
) : ViewModel() {

    val uiState =
        savedStateHandle.getStateFlow<String?>(
            GenreTvChannelsListScreen.GenreIdBundleKey,
            null
        ).map { genreInfo ->
            if (genreInfo == null) {
                GenreTvChannelsListScreenUiState.Error
            } else {
                val genreId = genreInfo.split("-")[0].toInt()
                val genreName = genreInfo.split("-")[1]
                val channels = TvChannelPagingSources().getTvChannelsGenrePagingSource(
                    genreId = genreId,
                    tvChannelsRepository = tvChannelsRepository,
                    userRepository = userRepository
                ).cachedIn(viewModelScope).stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    PagingData.empty()
                )
                GenreTvChannelsListScreenUiState.Done(
                    genreName = genreName,
                    channels = channels
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GenreTvChannelsListScreenUiState.Loading
        )
}

sealed interface GenreTvChannelsListScreenUiState {
    object Loading : GenreTvChannelsListScreenUiState
    object Error : GenreTvChannelsListScreenUiState
    data class Done(
        val channels: StateFlow<PagingData<TvChannel>>,
        val genreName: String
    ) : GenreTvChannelsListScreenUiState
}