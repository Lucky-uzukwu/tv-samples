package com.google.jetstream.data.repositories

import com.google.jetstream.data.models.StreamingProvider
import kotlinx.coroutines.flow.Flow

interface StreamingProvidersRepository {
    fun getStreamingProviders(token: String): Flow<List<StreamingProvider>>
}
