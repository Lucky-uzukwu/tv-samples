package com.google.wiltv.presentation.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.SearchContent
import com.google.wiltv.data.paging.pagingsources.search.SearchPagingSources
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.data.repositories.MovieRepository
import com.google.wiltv.data.repositories.GenreRepository
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.paging.pagingsources.movie.MoviesHeroSectionPagingSource
import com.google.wiltv.presentation.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


const val QUERY_LENGTH = 2

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository,
    private val movieRepository: MovieRepository,
    private val genreRepository: GenreRepository
) : ViewModel() {

    private val internalSearchState = MutableSharedFlow<SearchState>()
    
    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()
    
    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()
    
    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()
    
    private var suggestionsJob: Job? = null

    init {
        fetchGenres()
    }

    // Initial content when user first arrives on search screen
    val initialMovies: StateFlow<PagingData<MovieNew>> = Pager(
        PagingConfig(pageSize = 20, initialLoadSize = 20)
    ) {
        MoviesHeroSectionPagingSource(movieRepository, userRepository)
    }.flow.cachedIn(viewModelScope).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
    )

    fun query(queryString: String) {
        viewModelScope.launch { postQuery(queryString) }
    }
    
    fun fetchSuggestions(queryString: String) {
        suggestionsJob?.cancel()
        
        if (queryString.length < QUERY_LENGTH) {
            _searchSuggestions.value = emptyList()
            return
        }
        
        suggestionsJob = viewModelScope.launch {
            delay(300) // Debounce to avoid excessive API calls
            
            try {
                val user = userRepository.getUser()
                if (user != null) {
                    val result = searchRepository.getSearchSuggestions(
                        token = user.token ?: "",
                        query = queryString
                    )
                    
                    when (result) {
                        is com.google.wiltv.domain.ApiResult.Success -> {
                            _searchSuggestions.value = result.data
                            Log.d("SearchViewModel", "Fetched ${result.data.size} suggestions for '$queryString'")
                        }
                        is com.google.wiltv.domain.ApiResult.Error -> {
                            Log.w("SearchViewModel", "Failed to fetch suggestions: ${result.error}")
                            _searchSuggestions.value = emptyList()
                        }
                    }
                } else {
                    Log.w("SearchViewModel", "No user found for suggestions")
                    _searchSuggestions.value = emptyList()
                }
            } catch (e: Exception) {
                Log.w("SearchViewModel", "Error fetching suggestions", e)
                _searchSuggestions.value = emptyList()
            }
        }
    }
    
//    fun clearSuggestions() {
//        suggestionsJob?.cancel()
//        _searchSuggestions.value = emptyList()
//    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                when (val result = genreRepository.getAllGenres()) {
                    is com.google.wiltv.domain.ApiResult.Success -> {
                        _genres.value = result.data.member
                        Log.d("SearchViewModel", "Fetched ${result.data.member.size} genres")
                    }
                    is com.google.wiltv.domain.ApiResult.Error -> {
                        Log.w("SearchViewModel", "Failed to fetch genres: ${result.error}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error fetching genres", e)
            }
        }
    }

    fun selectGenre(genreId: Int?) {
        _selectedGenreId.value = genreId
    }

    private suspend fun postQuery(queryString: String) {
        try {
            Log.d("SearchViewModel", "Starting search for: '$queryString'")

            internalSearchState.emit(SearchState.Searching)

            val searchResults: StateFlow<PagingData<SearchContent>> =
                SearchPagingSources().searchUnified(
                    query = queryString,
                    searchRepository = searchRepository,
                    userRepository = userRepository,
                    genreId = _selectedGenreId.value
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
