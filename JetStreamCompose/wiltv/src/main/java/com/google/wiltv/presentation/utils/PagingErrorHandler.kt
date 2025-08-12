// ABOUTME: Utility for monitoring PagingData LoadState and converting errors to UiText
// ABOUTME: Bridges gap between PagingSource errors and ViewModel error states for ErrorScreen display

package com.google.wiltv.presentation.utils

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.google.wiltv.presentation.UiText
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Extension function to monitor LazyPagingItems LoadState and extract error messages
 * Returns UiText that can be used in ErrorScreen composables
 */
fun <T : Any> LazyPagingItems<T>.getErrorState(): UiText? {
    return when {
        loadState.refresh is LoadState.Error -> {
            val error = loadState.refresh as LoadState.Error
            UiText.DynamicString(error.error.message ?: "Failed to load data")
        }
        loadState.append is LoadState.Error -> {
            val error = loadState.append as LoadState.Error
            UiText.DynamicString(error.error.message ?: "Failed to load more data")
        }
        loadState.prepend is LoadState.Error -> {
            val error = loadState.prepend as LoadState.Error
            UiText.DynamicString(error.error.message ?: "Failed to refresh data")
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
    
    if (hasAnyError) {
        Logger.e { "🔍 hasError() detected error - refresh: $refreshError, append: $appendError, prepend: $prependError" }
        Logger.e { "🔍 LoadStates - refresh: ${loadState.refresh}, append: ${loadState.append}, prepend: ${loadState.prepend}" }
    } else {
        Logger.v { "🔍 hasError() - no errors detected - refresh: ${loadState.refresh}, append: ${loadState.append}, prepend: ${loadState.prepend}" }
    }
    
    return hasAnyError
}

/**
 * Extension function to check if LazyPagingItems finished loading without errors
 */
fun <T : Any> LazyPagingItems<T>.isNotLoadingAndNoError(): Boolean {
    return loadState.refresh is LoadState.NotLoading && !hasError()
}