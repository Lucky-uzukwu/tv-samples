package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.network.StreamingProviderService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingProvidersRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val streamingProviderService: StreamingProviderService
) : StreamingProvidersRepository {
    override fun getStreamingProviders(type: String): Flow<List<StreamingProvider>> = flow {
        val user = userRepository.getUser() ?: return@flow
        val response = streamingProviderService.getStreamingProviders(
            authToken = "Bearer ${user.token}",
            type = type
        )

        if (response.isSuccessful) {
            val streamingProviders = response.body()
            Logger.i { "API Response: $streamingProviders" }
            Logger.i { "Successfully fetched ${streamingProviders?.member?.size} genres for movie section." }
            if (streamingProviders != null) {
                emit(streamingProviders.member)
            }
        } else {
            // TODO Handle HTTP error codes
        }

    }
}
