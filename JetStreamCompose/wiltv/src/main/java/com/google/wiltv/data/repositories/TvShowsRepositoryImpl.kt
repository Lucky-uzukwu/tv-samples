package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvShowsResponse
import com.google.wiltv.data.network.TvShowsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvShowsRepositoryImpl @Inject constructor(
    private val tvShowService: TvShowsService,
    private val authRepository: AuthRepository,
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
            // TODO Handle HTTP error codes
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
                    // TODO Handle HTTP error codes
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
                    // TODO Handle HTTP error codes
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
            // TODO Handle HTTP error codes
        }

    }
}