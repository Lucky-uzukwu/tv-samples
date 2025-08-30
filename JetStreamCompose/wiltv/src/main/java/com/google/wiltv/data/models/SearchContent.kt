// ABOUTME: Sealed interface for unified search results containing movies, TV shows, and channels
// ABOUTME: Provides type-safe handling of mixed search results from the unified search API

package com.google.wiltv.data.models

import com.google.wiltv.data.network.TvChannel

sealed interface SearchContent {
    data class MovieContent(val movie: MovieNew) : SearchContent
    data class TvShowContent(val tvShow: TvShow) : SearchContent
    data class TvChannelContent(val tvChannel: TvChannel) : SearchContent
}