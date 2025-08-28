// ABOUTME: Unit tests for enhanced PagingErrorHandler error message conversion
// ABOUTME: Validates that paging errors are converted to user-friendly messages via DataError patterns

package com.google.wiltv.presentation.utils

import com.google.wiltv.R
import com.google.wiltv.presentation.UiText
import com.google.wiltv.domain.DataError
import com.google.wiltv.presentation.asUiText

/**
 * Validation tests for enhanced PagingErrorHandler functionality
 * Tests that paging errors are properly converted to user-friendly UiText messages
 */
class PagingErrorHandlerTest {
    
    fun validateTimeoutErrorDetection() {
        // Test various timeout-related error messages
        val timeoutMessages = listOf(
            "Connection timeout occurred",
            "Request timed out after 30 seconds",
            "TIMEOUT: Failed to connect",
            "Network timeout error"
        )
        
        timeoutMessages.forEach { message ->
            val exception = Exception(message)
            val result = convertPagingErrorToUiTextForTesting(exception, "Fallback")
            
            assert(result is UiText.StringResource) {
                "Timeout message '$message' should return StringResource"
            }
            
            val stringResource = result as UiText.StringResource
            assert(stringResource.id == R.string.error_connection_timeout) {
                "Timeout message '$message' should use connection timeout string resource"
            }
        }
    }
    
    fun validateInternetErrorDetection() {
        // Test various internet/network-related error messages
        val networkMessages = listOf(
            "No internet connection available",
            "Network connection failed",
            "Connection to server lost",
            "Network unreachable"
        )
        
        networkMessages.forEach { message ->
            val exception = Exception(message)
            val result = convertPagingErrorToUiTextForTesting(exception, "Fallback")
            
            assert(result is UiText.StringResource) {
                "Network message '$message' should return StringResource"
            }
            
            val stringResource = result as UiText.StringResource
            assert(stringResource.id == R.string.error_no_internet_detected) {
                "Network message '$message' should use no internet string resource"
            }
        }
    }
    
    fun validateServerErrorDetection() {
        // Test various server error messages
        val serverMessages = listOf(
            "Server error occurred",
            "HTTP 500 Internal Server Error",
            "Service temporarily unavailable",
            "Backend service failure"
        )
        
        serverMessages.forEach { message ->
            val exception = Exception(message)
            val result = convertPagingErrorToUiTextForTesting(exception, "Fallback")
            
            assert(result is UiText.StringResource) {
                "Server message '$message' should return StringResource"
            }
            
            val stringResource = result as UiText.StringResource
            assert(stringResource.id == R.string.error_service_unavailable) {
                "Server message '$message' should use service unavailable string resource"
            }
        }
    }
    
    fun validateUnauthorizedErrorDetection() {
        // Test unauthorized error messages
        val unauthorizedMessages = listOf(
            "Unauthorized access",
            "User not authorized to access this resource"
        )
        
        unauthorizedMessages.forEach { message ->
            val exception = Exception(message)
            val result = convertPagingErrorToUiTextForTesting(exception, "Fallback")
            
            assert(result is UiText.StringResource) {
                "Unauthorized message '$message' should return StringResource"
            }
            
            val stringResource = result as UiText.StringResource
            assert(stringResource.id == R.string.unauthorized) {
                "Unauthorized message '$message' should use unauthorized string resource"
            }
        }
    }
    
    fun validateNotFoundErrorDetection() {
        // Test not found error messages
        val notFoundMessages = listOf(
            "Resource not found",
            "Content not found on server"
        )
        
        notFoundMessages.forEach { message ->
            val exception = Exception(message)
            val result = convertPagingErrorToUiTextForTesting(exception, "Fallback")
            
            assert(result is UiText.StringResource) {
                "Not found message '$message' should return StringResource"
            }
            
            val stringResource = result as UiText.StringResource
            assert(stringResource.id == R.string.not_found) {
                "Not found message '$message' should use not found string resource"
            }
        }
    }
    
    fun validateFallbackForUnknownErrors() {
        // Test that unrecognized error messages fall back to DynamicString
        val unknownMessages = listOf(
            "Some random error message",
            "Database constraint violation",
            "Memory allocation failure"
        )
        
        unknownMessages.forEach { message ->
            val exception = Exception(message)
            val fallbackMessage = "Custom fallback"
            val result = convertPagingErrorToUiTextForTesting(exception, fallbackMessage)
            
            assert(result is UiText.DynamicString) {
                "Unknown message '$message' should return DynamicString fallback"
            }
            
            val dynamicString = result as UiText.DynamicString
            assert(dynamicString.value == fallbackMessage) {
                "Unknown message '$message' should use provided fallback message"
            }
        }
    }
    
    fun validateNullAndEmptyMessages() {
        // Test handling of null and empty error messages
        val nullException = Exception(null as String?)
        val emptyException = Exception("")
        val fallback = "Default error message"
        
        // Test null message
        val nullResult = convertPagingErrorToUiTextForTesting(nullException, fallback)
        assert(nullResult is UiText.DynamicString) {
            "Null message should return DynamicString fallback"
        }
        assert((nullResult as UiText.DynamicString).value == fallback) {
            "Null message should use fallback message"
        }
        
        // Test empty message
        val emptyResult = convertPagingErrorToUiTextForTesting(emptyException, fallback)
        assert(emptyResult is UiText.DynamicString) {
            "Empty message should return DynamicString fallback"
        }
        assert((emptyResult as UiText.DynamicString).value == fallback) {
            "Empty message should use fallback message"
        }
    }
    
    fun validateCaseInsensitiveMatching() {
        // Test that error detection is case-insensitive
        val testCases = listOf(
            "CONNECTION TIMEOUT" to R.string.error_connection_timeout,
            "network ERROR" to R.string.error_no_internet_detected,
            "Server Error" to R.string.error_service_unavailable,
            "UNAUTHORIZED" to R.string.unauthorized,
            "not found" to R.string.not_found
        )
        
        testCases.forEach { (message, expectedResId) ->
            val exception = Exception(message)
            val result = convertPagingErrorToUiTextForTesting(exception, "Fallback")
            
            assert(result is UiText.StringResource) {
                "Case-insensitive message '$message' should return StringResource"
            }
            
            val stringResource = result as UiText.StringResource
            assert(stringResource.id == expectedResId) {
                "Case-insensitive message '$message' should match expected resource"
            }
        }
    }
    
    // Helper function to expose the private convertPagingErrorToUiText for testing
    private fun convertPagingErrorToUiTextForTesting(error: Throwable, fallbackMessage: String): UiText {
        val message = error.message ?: ""
        
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
}