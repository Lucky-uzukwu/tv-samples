package com.google.wiltv.presentation.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.paging.pagingsources.search.SearchPagingSources
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.presentation.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val internalSearchState = MutableSharedFlow<SearchState>()

    fun query(queryString: String) {
        Log.d("SearchViewModel", "Query requested: '$queryString'")
        viewModelScope.launch { postQuery(queryString) }
    }

    private suspend fun postQuery(queryString: String) {
        try {
            Log.d("SearchViewModel", "Starting search for: '$queryString'")
            internalSearchState.emit(SearchState.Searching)

            Log.d("SearchViewModel", "Creating TV Shows paging source")
            val tvShows: StateFlow<PagingData<TvShow>> = SearchPagingSources().searchTvShows(
                query = queryString,
                searchRepository = searchRepository,
                userRepository = userRepository
            ).cachedIn(viewModelScope).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PagingData.empty()
            )

            Log.d("SearchViewModel", "Creating Movies paging source")
            val movies: StateFlow<PagingData<MovieNew>> = SearchPagingSources().searchMovies(
                query = queryString,
                searchRepository = searchRepository,
                userRepository = userRepository
            ).cachedIn(viewModelScope).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PagingData.empty()
            )

            Log.d("SearchViewModel", "Creating TV Channels paging source")
            val channels: StateFlow<PagingData<TvChannel>> = SearchPagingSources().searchTvChannels(
                query = queryString,
                searchRepository = searchRepository,
                userRepository = userRepository
            ).cachedIn(viewModelScope).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PagingData.empty()
            )

            Log.d("SearchViewModel", "Emitting Done state with paging sources")
            internalSearchState.emit(SearchState.Done(shows = tvShows, movies = movies, channels = channels))
        } catch (e: Exception) {
            Log.e("SearchViewModel", "Error in postQuery: ${e.message}", e)
            internalSearchState.emit(SearchState.Error(UiText.DynamicString("Search failed: ${e.message}")))
        }
    }

    val searchState = internalSearchState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchState.Done(
            null,
            null,
            null
        )
    )

    fun handlePagingError(errorMessage: UiText) {
        viewModelScope.launch {
            internalSearchState.emit(SearchState.Error(errorMessage))
        }
    }
}

sealed interface SearchState {
    data object Searching : SearchState
    data class Error(val uiText: UiText) : SearchState
    data class Done(
        val shows: StateFlow<PagingData<TvShow>>?,
        val movies: StateFlow<PagingData<MovieNew>>?,
        val channels: StateFlow<PagingData<TvChannel>>?
    ) : SearchState
}
