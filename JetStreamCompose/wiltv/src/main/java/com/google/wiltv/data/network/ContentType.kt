// ABOUTME: ContentType enum for specifying different content types in search API calls
// ABOUTME: Maps internal content types to API-specific values for movie, TV show, and channel searches

package com.google.wiltv.data.network

/**
 * Enum representing different content types for search operations
 * Each content type has an apiValue that corresponds to the API's expected format
 */
enum class ContentType(val apiValue: String) {
    MOVIE("App\\Models\\Movie"),
    TV_SHOW("App\\Models\\TvShow"),
    TV_CHANNEL("App\\Models\\TvChannel")
}