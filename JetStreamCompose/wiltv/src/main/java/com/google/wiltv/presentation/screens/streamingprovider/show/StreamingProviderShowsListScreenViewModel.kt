package com.google.wiltv.presentation.screens.streamingprovider.show

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.google.wiltv.data.models.SearchContent
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.ContentType
import com.google.wiltv.data.paging.pagingsources.search.SearchPagingSources
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
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
                val tvShows = SearchPagingSources().searchUnified(
                    query = "$streamingProviderId IN [‚id of sp']",
                    searchRepository = searchRepository,
                    userRepository = userRepository,
                    contentTypes = listOf(ContentType.TV_SHOW)
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
        val shows: StateFlow<PagingData<SearchContent>>,
        val streamingProviderName: String
    ) :
        StreamingProviderShowsListScreenUiState
}
