package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.Catalog
import com.google.wiltv.data.network.CatalogService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val catalogService: CatalogService,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : CatalogRepository {
    override fun getMovieCatalog(): Flow<List<Catalog>> = flow {
        val user = userRepository.getUser() ?: return@flow
        val response = catalogService.getCatalogs(
            authToken = "Bearer ${user.token}",
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
            // TODO Handle HTTP error codes
        }

    }

    override fun getTvShowCatalog(): Flow<List<Catalog>> = flow {
        val user = userRepository.getUser() ?: return@flow
        val response = catalogService.getCatalogs(
            authToken = "Bearer ${user.token}",
            type = "App\\Models\\TvShow"
        )

        if (response.isSuccessful) {
            val categories = response.body()
            Logger.i { "API Response: $categories" }
            Logger.i { "Successfully fetched ${categories?.member?.size} categories for tv show section." }
            if (categories != null) {
                emit(categories.member)
            }
        } else {
            // TODO Handle HTTP error codes
        }
    }
}