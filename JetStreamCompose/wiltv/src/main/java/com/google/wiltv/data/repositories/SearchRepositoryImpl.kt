package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.ContentType
import com.google.wiltv.data.network.SearchService
import com.google.wiltv.data.models.UnifiedSearchResponse
import com.google.wiltv.data.utils.ProfileContentHelper
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val searchService: SearchService,
    private val profileRepository: ProfileRepository
) : SearchRepository {
    override suspend fun searchContent(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int,
        contentTypes: List<ContentType>?,
        genreId: Int?
    ): ApiResult<UnifiedSearchResponse, DataError.Network> {
        Logger.i { "Searching content with query: $query, types: ${contentTypes?.map { it.apiValue }}, genre: $genreId" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        
        val response = searchService.search(
            authToken = "Bearer $token",
            search = query,
            types = contentTypes?.map { it.apiValue },
            genres = genreId,
            itemsPerPage = itemsPerPage,
            page = page,
            isAdultContent = contentParams.isAdultContent,
            isKidsContent = contentParams.isKidsContent
        )
        return mapToResult(response)
    }

    override suspend fun getSearchSuggestions(
        token: String,
        query: String,
        contentType: ContentType?
    ): ApiResult<List<String>, DataError.Network> {
        Logger.i { "Getting search suggestions for query: $query, type: ${contentType?.apiValue}" }
        
        return try {
            val response = searchService.getAutocomplete(
                authToken = "Bearer $token",
                query = query,
                type = contentType?.apiValue
            )
            
            if (response.isSuccessful && response.body() != null) {
                val suggestions = response.body()!!.member
                Logger.i { "Received ${suggestions.size} suggestions" }
                ApiResult.Success(suggestions)
            } else {
                Logger.w { "Autocomplete request failed: ${response.code()} ${response.message()}" }
                ApiResult.Error(DataError.Network.SERVER_ERROR)
            }
        } catch (e: Exception) {
            Logger.e(e) { "Error fetching search suggestions" }
            ApiResult.Error(DataError.Network.NO_INTERNET)
        }
    }


}