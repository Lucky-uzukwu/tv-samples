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

package com.google.jetstream.presentation.screens.search

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
        viewModelScope.launch { postQuery(queryString) }
    }

    private suspend fun postQuery(queryString: String) {
        internalSearchState.emit(SearchState.Searching)

//        val tvShowResults = searchRepository.searchTvShowsByQuery(
//            token = token,
//            query = queryString,
//            itemsPerPage = 10,
//            page = 1
//        ).firstOrNull()?.member

        val tvShows: StateFlow<PagingData<TvShow>> = SearchPagingSources().searchTvShows(
            query = queryString,
            searchRepository = searchRepository,
            userRepository = userRepository
        ).cachedIn(viewModelScope).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PagingData.empty()
        )

        val movies: StateFlow<PagingData<MovieNew>> = SearchPagingSources().searchMovies(
            query = queryString,
            searchRepository = searchRepository,
            userRepository = userRepository
        ).cachedIn(viewModelScope).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PagingData.empty()
        )

        internalSearchState.emit(SearchState.Done(shows = tvShows, movies = movies))
    }

    val searchState = internalSearchState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchState.Done(
            null,
            null
        )
    )
}

sealed interface SearchState {
    data object Searching : SearchState
    data class Done(
        val shows: StateFlow<PagingData<TvShow>>?,
        val movies: StateFlow<PagingData<MovieNew>>?
    ) : SearchState
}
