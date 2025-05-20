package com.google.jetstream.data.repositories

import co.touchlab.kermit.Logger
import com.google.jetstream.data.entities.MovieListNew
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.network.CatalogService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val catalogService: CatalogService,
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository
) : CatalogRepository {
    override fun getMovieCatalog(token: String): Flow<List<Catalog>> = flow {
        Logger.i { "Fetching categories for Movie section with token: $token" }
        val user = userRepository.getUser() ?: return@flow
        val response = catalogService.getCatalogs(
            authToken = "Bearer $token",
            type = "App\\Models\\Movie"
        )

        if (response.isSuccessful) {
            val categories = response.body()
            Logger.i { "API Response: $categories" }
            Logger.i { "Successfully fetched ${categories?.member?.size} categories for movie section." }
            if (categories != null) {
                emit(categories.member)
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
                }
            }

            Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
        }

    }
}