package com.google.jetstream.data.repositories

import co.touchlab.kermit.Logger
import com.google.jetstream.data.network.StreamingProvider
import com.google.jetstream.data.network.StreamingProviderService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingProvidersRepositoryImpl  @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository,
    private val streamingProviderService: StreamingProviderService
) : StreamingProvidersRepository {
    override fun getStreamingProviders(token: String): Flow<List<StreamingProvider>> = flow {
        Logger.i { "Fetching genres for Movie section with token: $token" }
        val user = userRepository.getUser() ?: return@flow
        val response = streamingProviderService.getStreamingProviders(
            authToken = "Bearer $token"
        )

        if (response.isSuccessful) {
            val streamingProviders = response.body()
            Logger.i { "API Response: $streamingProviders" }
            Logger.i { "Successfully fetched ${streamingProviders?.member?.size} genres for movie section." }
            if (streamingProviders != null) {
                emit(streamingProviders.member)
            }
        } else {
            // Handle HTTP error codes
            val errorBody =
                response.errorBody()?.string() // Get error message from server if available
            Logger.e { "API Error: ${response.code()} - ${response.message()}. Error body: $errorBody" }
            val loginResponse = user.password?.let {
                customerRepository.login(
                    deviceMacAddress = user.deviceMacAddress,
                    clientIp = user.clientIp,
                    deviceName = user.deviceName,
                    identifier = user.accessCode,
                    password = it
                )
            }
            when (loginResponse?.code()) {
                201 -> {
                    userRepository.saveUserToken(loginResponse.body()!!.token)
                    getStreamingProviders(loginResponse.body()!!.token)
                }

                else -> {
                    Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
                }
            }
        }

    }
}
