package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.network.StreamingProviderService
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingProvidersRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val streamingProviderService: StreamingProviderService
) : StreamingProvidersRepository {
    override suspend fun getStreamingProviders(type: String): ApiResult<List<StreamingProvider>, DataError.Network> {
        val user = userRepository.getUser() ?: return ApiResult.Error(
            error = DataError.Network.UNAUTHORIZED,
            message = "User not found"
        )
        
        Logger.d { "🎥 StreamingProvidersRepository: Fetching streaming providers for type: $type" }
        
        val result = mapToResult(streamingProviderService.getStreamingProviders(
            authToken = "Bearer ${user.token}",
            type = type
        ))
        
        return when (result) {
            is ApiResult.Success -> {
                Logger.d { "✅ StreamingProvidersRepository: Successfully fetched ${result.data.member.size} streaming providers" }
                ApiResult.Success(result.data.member)
            }
            is ApiResult.Error -> {
                Logger.e { "❌ StreamingProvidersRepository: Failed to fetch streaming providers - ${result.message ?: result.error}" }
                ApiResult.Error(
                    error = result.error,
                    message = result.message
                )
            }
        }
    }
}
