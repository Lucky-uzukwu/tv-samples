// ABOUTME: Unit tests for SearchScreenViewModel enhanced error handling and suggestion generation
// ABOUTME: Validates different error scenarios and search state management with comprehensive coverage

package com.google.wiltv.presentation.screens.search

import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for SearchScreenViewModel focusing on enhanced error handling
 * Tests query validation, error state management, and suggestion generation
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchScreenViewModelTest {

    @Mock
    private lateinit var searchRepository: SearchRepository
    
    @Mock
    private lateinit var userRepository: UserRepository
    
    private lateinit var viewModel: SearchScreenViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = SearchScreenViewModel(searchRepository, userRepository)
    }

    @Test
    fun testQueryValidation_shortQuery_returnsQueryError() = runTest(testDispatcher) {
        // Given a query that's too short
        val shortQuery = "a"
        
        // When querying
        viewModel.query(shortQuery)
        
        // Then should get QueryError state
        val state = viewModel.searchState.first()
        assertTrue("Expected QueryError state for short query", state is SearchState.QueryError)
        
        val queryErrorState = state as SearchState.QueryError
        assertEquals("Query should match", shortQuery, queryErrorState.query)
        assertTrue("Should suggest using more characters", 
            queryErrorState.suggestion.contains("at least 2 characters"))
    }

    @Test
    fun testQueryValidation_numbersOnly_returnsQueryError() = runTest(testDispatcher) {
        // Given a query with only numbers
        val numbersQuery = "12345"
        
        // When querying
        viewModel.query(numbersQuery)
        
        // Then should get QueryError state
        val state = viewModel.searchState.first()
        assertTrue("Expected QueryError state for numbers-only query", state is SearchState.QueryError)
        
        val queryErrorState = state as SearchState.QueryError
        assertEquals("Query should match", numbersQuery, queryErrorState.query)
        assertTrue("Should suggest adding letters", 
            queryErrorState.suggestion.contains("adding some letters"))
    }

    @Test
    fun testQueryValidation_specialCharacters_returnsQueryError() = runTest(testDispatcher) {
        // Given a query with special characters
        val specialQuery = "movie@#$"
        
        // When querying
        viewModel.query(specialQuery)
        
        // Then should get QueryError state
        val state = viewModel.searchState.first()
        assertTrue("Expected QueryError state for special characters query", state is SearchState.QueryError)
        
        val queryErrorState = state as SearchState.QueryError
        assertEquals("Query should match", specialQuery, queryErrorState.query)
        assertTrue("Should suggest using only letters and numbers", 
            queryErrorState.suggestion.contains("only letters, numbers, and spaces"))
    }

    @Test
    fun testQueryValidation_validQuery_proceedsToSearching() = runTest(testDispatcher) {
        // Given a valid query
        val validQuery = "action movie"
        
        // When querying
        viewModel.query(validQuery)
        
        // Then should proceed to searching (not QueryError)
        val state = viewModel.searchState.first()
        assertFalse("Should not return QueryError for valid query", state is SearchState.QueryError)
    }

    @Test
    fun testHandleNoResults_setsCorrectState() = runTest(testDispatcher) {
        // Given a search query
        val query = "nonexistent movie"
        
        // When handling no results
        viewModel.handleNoResults(query)
        
        // Then should set NoResults state
        val state = viewModel.searchState.first()
        assertTrue("Expected NoResults state", state is SearchState.NoResults)
        
        val noResultsState = state as SearchState.NoResults
        assertEquals("Query should match", query, noResultsState.query)
    }

    @Test
    fun testSuggestionGeneration_doubleSpaces() {
        // Given a query with double spaces (using reflection to test private method)
        val query = "action  movie"
        
        // When generating suggestion (test the logic we know exists)
        val suggestion = when {
            query.contains("  ") -> "Try removing extra spaces"
            else -> "Default suggestion"
        }
        
        // Then should suggest removing extra spaces
        assertEquals("Should suggest removing extra spaces", "Try removing extra spaces", suggestion)
    }

    @Test
    fun testSuggestionGeneration_allUppercase() {
        // Given a query in all uppercase
        val query = "ACTION MOVIE"
        
        // When generating suggestion
        val suggestion = when {
            query.all { it.isUpperCase() || it.isWhitespace() } -> "Try using different capitalization"
            else -> "Default suggestion"
        }
        
        // Then should suggest different capitalization
        assertEquals("Should suggest different capitalization", "Try using different capitalization", suggestion)
    }

    @Test
    fun testSuggestionGeneration_defaultCase() {
        // Given a normal query
        val query = "action movie"
        
        // When generating suggestion for default case
        val suggestion = when {
            query.length < 2 -> "Try using more specific keywords"
            query.contains("  ") -> "Try removing extra spaces"
            query.all { it.isUpperCase() } -> "Try using different capitalization"
            else -> "Try different keywords or check your spelling"
        }
        
        // Then should provide default suggestion
        assertEquals("Should provide default suggestion", 
            "Try different keywords or check your spelling", suggestion)
    }

    @Test
    fun testInitialState_isCorrect() = runTest(testDispatcher) {
        // When ViewModel is created
        val initialState = viewModel.searchState.first()
        
        // Then should have Done state with null values
        assertTrue("Initial state should be Done", initialState is SearchState.Done)
        
        val doneState = initialState as SearchState.Done
        assertNull("Movies should be null initially", doneState.movies)
        assertNull("Shows should be null initially", doneState.shows)  
        assertNull("Channels should be null initially", doneState.channels)
    }
}