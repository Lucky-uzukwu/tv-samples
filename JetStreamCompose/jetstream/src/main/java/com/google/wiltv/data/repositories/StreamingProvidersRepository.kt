package com.google.wiltv.data.repositories

import com.google.wiltv.data.models.StreamingProvider
import kotlinx.coroutines.flow.Flow

interface StreamingProvidersRepository {
    fun getStreamingProviders(type: String): Flow<List<StreamingProvider>>
}
