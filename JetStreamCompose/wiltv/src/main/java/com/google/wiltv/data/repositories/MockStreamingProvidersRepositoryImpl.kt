package com.google.wiltv.data.repositories

import com.google.wiltv.data.models.StreamingProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockStreamingProvidersRepositoryImpl : StreamingProvidersRepository {
    override fun getStreamingProviders(type: String): Flow<List<StreamingProvider>> = flow {

        emit(
            listOf(
                StreamingProvider(
                    id = 1,
                    name = "Apple TV Plus",
                    logoPath = "images/streaming/provider/1bd81243-5ed6-4ea4-b7bf-ad40ee281517.jpg",
                    logoUrl = "TODO()"
                ),
                StreamingProvider(
                    id = 1,
                    name = "Apple TV Plus Amazon Channel",
                    logoPath = "images/streaming/provider/3fc35932-2d8c-4491-bab9-82a79e449f1d.jpg",
                    logoUrl = "TODO()"
                )
            )
        )

    }
}