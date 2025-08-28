// ABOUTME: Unit tests for enhanced DataError to UiText conversion functionality  
// ABOUTME: Validates user-friendly error messages and StringResource integration

package com.google.wiltv.presentation

import com.google.wiltv.R
import com.google.wiltv.domain.DataError
import com.google.wiltv.presentation.asUiText
import org.junit.Test
import org.junit.Assert.*

/**
 * Validation tests for enhanced asUiText() error message conversion
 * Tests user-friendly error messages follow UiText patterns with StringResource support
 */
class AsUiTextTest {
    
    @Test
    fun validateRequestTimeoutErrorMessage() {
        // Test that REQUEST_TIMEOUT returns user-friendly message
        val error = DataError.Network.REQUEST_TIMEOUT
        val uiText = error.asUiText()
        
        // Should return StringResource with user-friendly timeout message
        assert(uiText is UiText.StringResource) {
            "REQUEST_TIMEOUT should return StringResource, got ${uiText::class.simpleName}"
        }
        
        val stringResource = uiText as UiText.StringResource
        assert(stringResource.id == R.string.error_connection_timeout) {
            "REQUEST_TIMEOUT should use error_connection_timeout string resource"
        }
    }
    
    @Test
    fun validateNoInternetErrorMessage() {
        // Test that NO_INTERNET returns user-friendly message
        val error = DataError.Network.NO_INTERNET
        val uiText = error.asUiText()
        
        assert(uiText is UiText.StringResource) {
            "NO_INTERNET should return StringResource, got ${uiText::class.simpleName}"
        }
        
        val stringResource = uiText as UiText.StringResource
        assert(stringResource.id == R.string.error_no_internet_detected) {
            "NO_INTERNET should use error_no_internet_detected string resource"
        }
    }
    
    @Test
    fun validateServerErrorMessage() {
        // Test that SERVER_ERROR returns enhanced user-friendly message
        val error = DataError.Network.SERVER_ERROR
        val uiText = error.asUiText()
        
        assert(uiText is UiText.StringResource) {
            "SERVER_ERROR should return StringResource, got ${uiText::class.simpleName}"
        }
        
        val stringResource = uiText as UiText.StringResource
        assert(stringResource.id == R.string.error_server_issues) {
            "SERVER_ERROR should use enhanced error_server_issues string resource"
        }
    }
    
    @Test
    fun validateDynamicStringFallback() {
        // Test that when custom message is provided, it uses DynamicString
        val error = DataError.Network.REQUEST_TIMEOUT
        val customMessage = "Custom timeout message"
        val uiText = error.asUiText(customMessage)
        
        assert(uiText is UiText.DynamicString) {
            "With custom message should return DynamicString, got ${uiText::class.simpleName}"
        }
        
        val dynamicString = uiText as UiText.DynamicString
        assert(dynamicString.value == customMessage) {
            "DynamicString should contain custom message"
        }
    }
    
    @Test
    fun validateBackwardCompatibilityForOtherErrors() {
        // Test that non-enhanced errors still work (backward compatibility)
        val testCases = listOf(
            DataError.Network.TOO_MANY_REQUESTS to R.string.youve_hit_your_rate_limit,
            DataError.Network.PAYLOAD_TOO_LARGE to R.string.file_too_large,
            DataError.Network.SERIALIZATION to R.string.error_serialization,
            DataError.Network.UNKNOWN to R.string.unknown_error,
            DataError.Local.DISK_FULL to R.string.error_disk_full
        )
        
        testCases.forEach { (error, expectedResId) ->
            val uiText = error.asUiText()
            
            assert(uiText is UiText.StringResource) {
                "$error should return StringResource for backward compatibility"
            }
            
            val stringResource = uiText as UiText.StringResource
            assert(stringResource.id == expectedResId) {
                "$error should use expected string resource $expectedResId"
            }
        }
    }
    
    @Test
    fun validateNullMessageHandling() {
        // Test that null message falls back to StringResource
        val error = DataError.Network.REQUEST_TIMEOUT
        val uiText = error.asUiText(null)
        
        assert(uiText is UiText.StringResource) {
            "Null message should fall back to StringResource"
        }
        
        val stringResource = uiText as UiText.StringResource
        assert(stringResource.id == R.string.error_connection_timeout) {
            "Null message fallback should use enhanced error message"
        }
    }
    
    @Test
    fun validateEmptyMessageHandling() {
        // Test handling of empty strings for certain error types
        val error = DataError.Network.BAD_REQUEST
        val uiText = error.asUiText("")
        
        assert(uiText is UiText.StringResource) {
            "Empty message should return StringResource for BAD_REQUEST"
        }
        
        val stringResource = uiText as UiText.StringResource
        assert(stringResource.id == R.string.bad_request) {
            "BAD_REQUEST with empty message should use bad_request string resource"
        }
    }
    
    @Test
    fun validateUnauthorizedErrorMessage() {
        // Test that UNAUTHORIZED returns session expired message per AC 1
        val error = DataError.Network.UNAUTHORIZED
        val uiText = error.asUiText("")
        
        assert(uiText is UiText.StringResource) {
            "UNAUTHORIZED should return StringResource, got ${uiText::class.simpleName}"
        }
        
        val stringResource = uiText as UiText.StringResource
        assert(stringResource.id == R.string.error_session_expired) {
            "UNAUTHORIZED should use error_session_expired string resource per AC 1"
        }
    }
    
    @Test
    fun validateNotFoundErrorMessage() {
        // Test that NOT_FOUND returns content unavailable message per AC 2
        val error = DataError.Network.NOT_FOUND
        val uiText = error.asUiText("")
        
        assert(uiText is UiText.StringResource) {
            "NOT_FOUND should return StringResource, got ${uiText::class.simpleName}"
        }
        
        val stringResource = uiText as UiText.StringResource
        assert(stringResource.id == R.string.error_content_unavailable) {
            "NOT_FOUND should use error_content_unavailable string resource per AC 2"
        }
    }
    
    @Test
    fun validateAllEnhancedErrorTypes() {
        // Comprehensive test for all enhanced error types per AC 1, 2, 3
        val enhancedErrors = mapOf(
            DataError.Network.REQUEST_TIMEOUT to R.string.error_connection_timeout,
            DataError.Network.NO_INTERNET to R.string.error_no_internet_detected,
            DataError.Network.SERVER_ERROR to R.string.error_server_issues,
            DataError.Network.UNAUTHORIZED to R.string.error_session_expired,
            DataError.Network.NOT_FOUND to R.string.error_content_unavailable
        )
        
        enhancedErrors.forEach { (error, expectedResId) ->
            val uiText = error.asUiText("")
            
            assert(uiText is UiText.StringResource) {
                "Enhanced error $error should return StringResource"
            }
            
            val stringResource = uiText as UiText.StringResource
            assert(stringResource.id == expectedResId) {
                "Enhanced error $error should use correct string resource"
            }
        }
    }
}