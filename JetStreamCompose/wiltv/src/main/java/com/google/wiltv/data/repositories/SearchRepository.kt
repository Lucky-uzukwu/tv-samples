package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.ContentType
import com.google.wiltv.data.models.UnifiedSearchResponse
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError

interface SearchRepository {

    suspend fun searchContent(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int,
        contentTypes: List<ContentType>? = null,
        genreId: Int? = null
    ): ApiResult<UnifiedSearchResponse, DataError.Network>

    suspend fun getSearchSuggestions(
        token: String,
        query: String,
        contentType: ContentType? = null
    ): ApiResult<List<String>, DataError.Network>
}