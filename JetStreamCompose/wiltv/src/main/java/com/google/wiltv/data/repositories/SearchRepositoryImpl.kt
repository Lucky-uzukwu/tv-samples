package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.MovieSearchResponse
import com.google.wiltv.data.network.SearchService
import com.google.wiltv.data.network.ShowSearchResponse
import com.google.wiltv.data.utils.ProfileContentHelper
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val searchService: SearchService,
    private val profileRepository: ProfileRepository
) : SearchRepository {
    override suspend fun searchMoviesByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<MovieSearchResponse, DataError.Network> {
        Logger.i { "Searching movies with query: $query" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        
        val response = searchService.searchMovie(
            authToken = "Bearer $token",
            search = query,
            itemsPerPage = itemsPerPage,
            page = page,
            isAdultContent = contentParams.isAdultContent,
            isKidsContent = contentParams.isKidsContent
        )
        return mapToResult(response)
    }

    override suspend fun searchTvShowsByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<ShowSearchResponse, DataError.Network> {
        Logger.i { "Searching TV shows with query: $query" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        
        val response = searchService.searchTvShows(
            authToken = "Bearer $token",
            search = query,
            itemsPerPage = itemsPerPage,
            page = page,
            isAdultContent = contentParams.isAdultContent,
            isKidsContent = contentParams.isKidsContent
        )
        return mapToResult(response)
    }


}