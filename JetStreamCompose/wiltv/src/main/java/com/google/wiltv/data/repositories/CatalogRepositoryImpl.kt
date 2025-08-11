package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.CatalogResponse
import com.google.wiltv.data.network.CatalogService
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val catalogService: CatalogService,
    private val userRepository: UserRepository
) : CatalogRepository {
    override suspend fun getMovieCatalog(): ApiResult<CatalogResponse, DataError.Network> {
        Logger.i { "Attempting to fetch movie catalog" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val response = catalogService.getCatalogs(
            authToken = "Bearer ${user.token}",
            type = "App\\Models\\Movie"
        )
        return mapToResult(response)
    }

    override suspend fun getTvShowCatalog(): ApiResult<CatalogResponse, DataError.Network> {
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val response = catalogService.getCatalogs(
            authToken = "Bearer ${user.token}",
            type = "App\\Models\\TvShow"
        )
        return mapToResult(response)
    }
}