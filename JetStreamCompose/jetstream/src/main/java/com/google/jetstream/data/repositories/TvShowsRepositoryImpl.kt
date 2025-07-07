package com.google.jetstream.data.repositories

import co.touchlab.kermit.Logger
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.network.TvShowsResponse
import com.google.jetstream.data.network.TvShowsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvShowsRepositoryImpl @Inject constructor(
    private val tvShowService: TvShowsService,
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository
) : TvShowsRepository {
    override fun getTvShowsToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): Flow<TvShowsResponse> = flow {
        val user = userRepository.getUser() ?: return@flow
        val response = tvShowService.getTvShows(
            authToken = "Bearer $token",
            showInHeroSection = 1,
            page = page,
            itemsPerPage = itemsPerPage
        )

        if (response.isSuccessful) {
            val tvShows = response.body()
            Logger.i { "API Response: $tvShows" }
            if (tvShows != null) {
                emit(tvShows)
            }
        } else {
            // Handle HTTP error codes
            val errorBody =
                response.errorBody()?.string() // Get error message from server if available
            Logger.e { "API Error for getTvShowsToShowInHeroSection : ${response.code()} - ${response.message()}. Error body: $errorBody" }
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
                    getTvShowsToShowInHeroSection(token, page, itemsPerPage)
                }
            }

            Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
        }
    }

    override fun getTvShowsToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<TvShowsResponse> {
        var retries = 0
        return flow {
            val user = userRepository.getUser() ?: return@flow
            while (retries < 3) {
                val response = tvShowService.getTvShows(
                    authToken = "Bearer $token",
                    catalogId = catalogId,
                    itemsPerPage = itemsPerPage,
                    page = page
                )

                if (response.isSuccessful) {
                    val tvShowResponse = response.body()
                    Logger.i { "Successfully fetched ${tvShowResponse?.member?.size} tv shows for catalog $catalogId." }
                    if (tvShowResponse != null) {
                        emit(tvShowResponse)
                        return@flow // Success, exit flow
                    }
                } else {
                    // Handle HTTP error codes
                    val errorBody =
                        response.errorBody()?.string() // Get error message from server if available
                    Logger.e { "API Error when getting tvshows in catalog section for $catalogId: ${response.code()} - ${response.message()}. Error body: $errorBody" }
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
                            retries++ // Increment retries and continue loop
                        }
                        else -> Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
                    }
                }
            }
            Logger.e { "Failed to fetch tv shows for catalog $catalogId after $retries retries." }
        }
    }

    override fun getTvShowsToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): Flow<TvShowsResponse> {
        var retries = 0
        return flow {
            val user = userRepository.getUser() ?: return@flow
            while (retries < 3) {
                val response = tvShowService.getTvShows(
                    authToken = "Bearer $token",
                    genreId = genreId,
                    itemsPerPage = itemsPerPage,
                    page = page
                )

                if (response.isSuccessful) {
                    val tvShowsResponse = response.body()
                    Logger.i { "Successfully fetched ${tvShowsResponse?.member?.size} tv shows for genre $genreId." }
                    if (tvShowsResponse != null) {
                        emit(tvShowsResponse)
                        return@flow // Success, exit flow
                    }
                } else {
                    // Handle HTTP error codes
                    val errorBody =
                        response.errorBody()?.string() // Get error message from server if available
                    Logger.e { "API Error for getTvShowsToShowInGenreSection: ${response.code()} - ${response.message()}. Error body: $errorBody" }
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
                            retries++ // Increment retries and continue loop
                        }

                        else -> Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
                    }
                }
            }
            Logger.e { "Failed to fetch tv shows for genre $genreId after $retries retries." }
        }
    }

    override fun getTvShowsDetails(
        token: String,
        tvShowId: String
    ): Flow<TvShow> = flow {
        val user = userRepository.getUser() ?: return@flow
        val response = tvShowService.getTvShowById(
            authToken = "Bearer $token",
            tvShowId = tvShowId
        )

        if (response.isSuccessful) {
            val tvShowData = response.body()
            Logger.i { "Successfully fetched ${tvShowData?.id}." }
            if (tvShowData != null) {
                emit(tvShowData)
            }
        } else {
            // Handle HTTP error codes
            val errorBody =
                response.errorBody()?.string() // Get error message from server if available
            Logger.e { "API Error for getTvShowsDetails: ${response.code()} - ${response.message()}. Error body: $errorBody" }
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
                    getTvShowsDetails(token, tvShowId)
                }

                else -> {
                    Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
                    // todo navigate to login

                }
            }

            Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
        }

    }
}