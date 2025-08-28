// ABOUTME: Utility for monitoring PagingData LoadState and converting errors to UiText
// ABOUTME: Bridges gap between PagingSource errors and ViewModel error states for ErrorScreen display

package com.google.wiltv.presentation.utils

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.asUiText
import com.google.wiltv.domain.DataError
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Converts a PagingSource error to user-friendly UiText
 * Extracts DataError from exception message or uses fallback message
 */
private fun convertPagingErrorToUiText(error: Throwable, fallbackMessage: String): UiText {
    // Try to extract DataError from exception message
    val message = error.message ?: ""
    
    // Look for patterns that indicate specific DataError types
    return when {
        message.contains("timeout", ignoreCase = true) || 
        message.contains("timed out", ignoreCase = true) -> 
            DataError.Network.REQUEST_TIMEOUT.asUiText()
            
        message.contains("no internet", ignoreCase = true) || 
        message.contains("network", ignoreCase = true) ||
        message.contains("connection", ignoreCase = true) -> 
            DataError.Network.NO_INTERNET.asUiText()
            
        message.contains("server error", ignoreCase = true) ||
        message.contains("5", ignoreCase = true) ||
        message.contains("service", ignoreCase = true) -> 
            DataError.Network.SERVER_ERROR.asUiText()
            
        message.contains("unauthorized", ignoreCase = true) -> 
            DataError.Network.UNAUTHORIZED.asUiText()
            
        message.contains("not found", ignoreCase = true) -> 
            DataError.Network.NOT_FOUND.asUiText()
            
        else -> UiText.DynamicString(fallbackMessage)
    }
}

/**
 * Extension function to monitor LazyPagingItems LoadState and extract error messages
 * Returns UiText that can be used in ErrorScreen composables with enhanced error handling
 */
fun <T : Any> LazyPagingItems<T>.getErrorState(): UiText? {
    return when {
        loadState.refresh is LoadState.Error -> {
            val error = loadState.refresh as LoadState.Error
            convertPagingErrorToUiText(error.error, "Failed to load data")
        }

        loadState.append is LoadState.Error -> {
            val error = loadState.append as LoadState.Error
            convertPagingErrorToUiText(error.error, "Failed to load more data")
        }

        loadState.prepend is LoadState.Error -> {
            val error = loadState.prepend as LoadState.Error
            convertPagingErrorToUiText(error.error, "Failed to refresh data")
        }

        else -> null
    }
}

/**
 * Extension function to check if LazyPagingItems has any loading state
 */
fun <T : Any> LazyPagingItems<T>.isLoading(): Boolean {
    return loadState.refresh is LoadState.Loading ||
            loadState.append is LoadState.Loading ||
            loadState.prepend is LoadState.Loading
}

/**
 * Extension function to check if LazyPagingItems has any error state
 */
fun <T : Any> LazyPagingItems<T>.hasError(): Boolean {
    val refreshError = loadState.refresh is LoadState.Error
    val appendError = loadState.append is LoadState.Error
    val prependError = loadState.prepend is LoadState.Error
    val hasAnyError = refreshError || appendError || prependError
    return hasAnyError
}

/**
 * Extension function to check if LazyPagingItems finished loading without errors
 */
fun <T : Any> LazyPagingItems<T>.isNotLoadingAndNoError(): Boolean {
    return loadState.refresh is LoadState.NotLoading && !hasError()
}