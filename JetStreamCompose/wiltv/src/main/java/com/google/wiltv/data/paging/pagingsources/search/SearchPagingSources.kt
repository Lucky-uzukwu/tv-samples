package com.google.wiltv.data.paging.pagingsources.search

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.wiltv.data.models.SearchContent
import com.google.wiltv.data.network.ContentType
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
import kotlinx.coroutines.flow.Flow

private const val NETWORK_PAGE_SIZE = 30

class SearchPagingSources {

    fun searchUnified(
        query: String,
        searchRepository: SearchRepository,
        userRepository: UserRepository,
        contentTypes: List<ContentType>? = null
    ): Flow<PagingData<SearchContent>> {

        return Pager(
            PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                initialLoadSize = 40,
                enablePlaceholders = false
            )
        ) {
            UnifiedSearchPagingSource(
                searchRepository,
                userRepository,
                query,
                contentTypes
            )
        }.flow
    }
}