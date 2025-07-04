package com.google.jetstream.data.repositories

import co.touchlab.kermit.Logger
import com.google.jetstream.data.network.SearchResponse
import com.google.jetstream.data.network.SearchService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository,
    private val searchService: SearchService
) : SearchRepository {
    override fun searchWithQueryAndType(
        token: String,
        query: String,
        type: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<SearchResponse> = flow {
        val user = userRepository.getUser() ?: return@flow

        // TODO: Switch to new API structure , when Priesly pushes it
        val response = searchService.search(
            authToken = "Bearer $token",
            query = query,
            type = type,
            itemsPerPage = itemsPerPage,
            page = page
        )

        if (response.isSuccessful) {
            val searchResponse = response.body()
            Logger.i { "Successfully fetched ${searchResponse?.member?.size} movies for catalog section out of ${searchResponse?.totalItems}." }
            if (searchResponse != null) {
                emit(searchResponse)
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
                    searchWithQueryAndType(token, query, type, itemsPerPage, page)
                }
            }

            Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
        }
    }

}