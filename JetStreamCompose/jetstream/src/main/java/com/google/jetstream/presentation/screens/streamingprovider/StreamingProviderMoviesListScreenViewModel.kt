package com.google.jetstream.presentation.screens.streamingprovider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.jetstream.data.entities.MovieCategoryDetails
import com.google.jetstream.data.models.MovieNew
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
class StreamingProviderMoviesListScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    searchRepository: SearchRepository,
    userRepository: UserRepository,
) : ViewModel() {


    val uiState =
        savedStateHandle.getStateFlow<String?>(
            StreamingProviderMoviesListScreen.StreamingProviderIdBundleKey,
            null
        ).map { streamingProviderId ->
            if (streamingProviderId == null) {
                StreamingProviderMoviesListScreenUiState.Error
            } else {
                val movies = SearchPagingSources().searchMovies(
                    query = "$streamingProviderId IN [‚id of sp‘]",
                    searchRepository = searchRepository,
                    userRepository = userRepository
                ).cachedIn(viewModelScope).stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    PagingData.empty()
                )
                StreamingProviderMoviesListScreenUiState.Done(
                    streamingProviderName = streamingProviderId,
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
        val movies: StateFlow<PagingData<MovieNew>>,
        val streamingProviderName: String
    ) :
        StreamingProviderMoviesListScreenUiState
}
