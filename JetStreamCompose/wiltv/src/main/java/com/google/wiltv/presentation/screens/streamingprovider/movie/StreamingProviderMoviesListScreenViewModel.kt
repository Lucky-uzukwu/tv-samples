package com.google.wiltv.presentation.screens.streamingprovider.movie

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.SearchContent
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
class StreamingProviderMoviesListScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    searchRepository: SearchRepository,
    userRepository: UserRepository,
) : ViewModel() {


    val uiState =
        savedStateHandle.getStateFlow<String?>(
            StreamingProviderMoviesListScreen.StreamingProviderIdBundleKey,
            null
        ).map { streamingProvider ->
            if (streamingProvider == null) {
                StreamingProviderMoviesListScreenUiState.Error
            } else {
                val streamingProviderId = streamingProvider.split("-")[0]
                val streamingProviderName = streamingProvider.split("-")[1]
                val movies = SearchPagingSources().searchUnified(
                    query = "$streamingProviderId IN [‚id of sp']",
                    searchRepository = searchRepository,
                    userRepository = userRepository,
                    contentTypes = listOf(ContentType.MOVIE)
                ).cachedIn(viewModelScope).stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    PagingData.empty()
                )
                StreamingProviderMoviesListScreenUiState.Done(
                    streamingProviderName = streamingProviderName,
                    movies = movies
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StreamingProviderMoviesListScreenUiState.Loading
        )

}

sealed interface StreamingProviderMoviesListScreenUiState {
    object Loading : StreamingProviderMoviesListScreenUiState
    object Error : StreamingProviderMoviesListScreenUiState
    data class Done(
        val movies: StateFlow<PagingData<SearchContent>>,
        val streamingProviderName: String
    ) :
        StreamingProviderMoviesListScreenUiState
}
