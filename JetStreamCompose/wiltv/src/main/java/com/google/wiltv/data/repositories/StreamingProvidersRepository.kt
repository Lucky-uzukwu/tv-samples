package com.google.wiltv.data.repositories

import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError

interface StreamingProvidersRepository {
    suspend fun getStreamingProviders(type: String): ApiResult<List<StreamingProvider>, DataError.Network>
}
