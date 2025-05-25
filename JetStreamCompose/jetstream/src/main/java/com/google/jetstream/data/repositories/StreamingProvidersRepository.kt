package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.Genre
import com.google.jetstream.data.network.StreamingProvider
import kotlinx.coroutines.flow.Flow

interface StreamingProvidersRepository {
    fun getStreamingProviders(token: String): Flow<List<StreamingProvider>>
}
