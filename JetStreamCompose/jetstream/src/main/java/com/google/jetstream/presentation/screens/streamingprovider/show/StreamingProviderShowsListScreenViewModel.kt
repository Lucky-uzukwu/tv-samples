package com.google.jetstream.presentation.screens.streamingprovider.show

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.paging.pagingsources.search.SearchPagingSources
import com.google.jetstream.data.repositories.SearchRepository
import com.google.jetstream.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class StreamingProviderShowsListScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    searchRepository: SearchRepository,
    userRepository: UserRepository,
) : ViewModel() {


    val uiState =
        savedStateHandle.getStateFlow<String?>(
            StreamingProviderShowsListScreen.StreamingProviderIdBundleKey,
            null
        ).map { streamingProvider ->
            if (streamingProvider == null) {
                StreamingProviderShowsListScreenUiState.Error
            } else {
                val streamingProviderId = streamingProvider.split("-")[0]
                val streamingProviderName = streamingProvider.split("-")[1]
                val tvShows = SearchPagingSources().searchTvShows(
                    query = "$streamingProviderId IN [‚id of sp‘]",
                    searchRepository = searchRepository,
                    userRepository = userRepository
                ).cachedIn(viewModelScope).stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    PagingData.empty()
                )
                StreamingProviderShowsListScreenUiState.Done(
                    streamingProviderName = streamingProviderName,
                    shows = tvShows
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StreamingProviderShowsListScreenUiState.Loading
        )

}

sealed interface StreamingProviderShowsListScreenUiState {
    object Loading : StreamingProviderShowsListScreenUiState
    object Error : StreamingProviderShowsListScreenUiState
    data class Done(
        val shows: StateFlow<PagingData<TvShow>>,
        val streamingProviderName: String
    ) :
        StreamingProviderShowsListScreenUiState
}
