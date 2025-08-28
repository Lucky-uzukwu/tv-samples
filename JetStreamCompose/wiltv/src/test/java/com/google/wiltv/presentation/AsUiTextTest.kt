// ABOUTME: Unit tests for enhanced DataError to UiText conversion functionality  
// ABOUTME: Validates user-friendly error messages and StringResource integration

package com.google.wiltv.presentation

import com.google.wiltv.R
import com.google.wiltv.domain.DataError
import com.google.wiltv.presentation.asUiText

/**
 * Validation tests for enhanced asUiText() error message conversion
 * Tests user-friendly error messages follow UiText patterns with StringResource support
 */
class AsUiTextTest {
    
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
    
    fun validateServerErrorMessage() {
        // Test that SERVER_ERROR returns user-friendly message
        val error = DataError.Network.SERVER_ERROR
        val uiText = error.asUiText()
        
        assert(uiText is UiText.StringResource) {
            "SERVER_ERROR should return StringResource, got ${uiText::class.simpleName}"
        }
        
        val stringResource = uiText as UiText.StringResource
        assert(stringResource.id == R.string.error_service_unavailable) {
            "SERVER_ERROR should use error_service_unavailable string resource"
        }
    }
    
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
    
    fun validateAllEnhancedErrorTypes() {
        // Comprehensive test for all three enhanced error types
        val enhancedErrors = mapOf(
            DataError.Network.REQUEST_TIMEOUT to R.string.error_connection_timeout,
            DataError.Network.NO_INTERNET to R.string.error_no_internet_detected,
            DataError.Network.SERVER_ERROR to R.string.error_service_unavailable
        )
        
        enhancedErrors.forEach { (error, expectedResId) ->
            val uiText = error.asUiText()
            
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