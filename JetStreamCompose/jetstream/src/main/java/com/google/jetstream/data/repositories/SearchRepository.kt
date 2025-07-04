package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.SearchResponse
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun searchWithQueryAndType(
        token: String,
        query: String,
        type: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<SearchResponse>
}