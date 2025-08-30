package com.google.wiltv.presentation.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.SearchContent
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


const val QUERY_LENGTH = 2

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
        try {
            Log.d("SearchViewModel", "Starting search for: '$queryString'")

            // Validate query first
            val queryValidation = validateQuery(queryString)
            if (queryValidation != null) {
                Log.d("SearchViewModel", "Search Query validation failed: $queryValidation")
                val suggestion = generateSearchSuggestion(queryString)
                internalSearchState.emit(SearchState.QueryError(queryString, suggestion))
                return
            }

            internalSearchState.emit(SearchState.Searching)

            val searchResults: StateFlow<PagingData<SearchContent>> =
                SearchPagingSources().searchUnified(
                    query = queryString,
                    searchRepository = searchRepository,
                    userRepository = userRepository
                ).cachedIn(viewModelScope).stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    PagingData.empty()
                )

            Log.d("SearchViewModel", "Search completed for: '$queryString'")
            internalSearchState.emit(
                SearchState.Done(searchResults)
            )
        } catch (e: Exception) {
            Log.e("SearchViewModel", "Error in postQuery: ${e.message}", e)

            // Determine error type based on exception
            val errorMessage = e.message?.lowercase() ?: ""
            when {
                errorMessage.contains("network") ||
                        errorMessage.contains("timeout") ||
                        errorMessage.contains("connection") -> {
                    internalSearchState.emit(SearchState.NetworkError(queryString))
                }

                else -> {
                    internalSearchState.emit(SearchState.Error(UiText.DynamicString("Search failed: ${e.message}")))
                }
            }
        }
    }

    val searchState = internalSearchState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchState.Done(null)
    )

    fun handlePagingError(errorMessage: UiText) {
        viewModelScope.launch {
            internalSearchState.emit(SearchState.Error(errorMessage))
        }
    }

    private fun validateQuery(query: String): String? {
        return when {
            query.length < QUERY_LENGTH -> "Try using at least 2 characters"
            query.contains(Regex("[^\\w\\s]")) -> "Try using only letters, numbers, and spaces"
            else -> null
        }
    }

    private fun generateSearchSuggestion(query: String): String {
        return when {
            query.length < QUERY_LENGTH -> "Try using more specific keywords"
            query.contains("  ") -> "Try removing extra spaces"
            query.all { it.isUpperCase() } -> "Try using different capitalization"
            else -> "Try different keywords or check your spelling"
        }
    }

    fun handleNoResults(query: String) {
        viewModelScope.launch {
            internalSearchState.emit(SearchState.NoResults(query))
        }
    }
}

sealed interface SearchState {
    data object Searching : SearchState
    data class Error(val uiText: UiText) : SearchState
    data class NetworkError(val query: String) : SearchState
    data class NoResults(val query: String) : SearchState
    data class QueryError(val query: String, val suggestion: String) : SearchState
    data class Done(
        val content: StateFlow<PagingData<SearchContent>>?
    ) : SearchState
}
