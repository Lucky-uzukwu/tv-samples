package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.Catalog
import com.google.wiltv.data.network.CatalogResponse
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError

class MockCatalogRepositoryImpl : CatalogRepository {
    override suspend fun getMovieCatalog(): ApiResult<CatalogResponse, DataError.Network> {

        return ApiResult.Success(
            CatalogResponse(
                listOf(
                    Catalog(
                        id = "1",
                        name = "Movie catalog"
                    )
                ),
                1
            )
        )
    }

    override suspend fun getTvShowCatalog(): ApiResult<CatalogResponse, DataError.Network> {
        return ApiResult.Success(
            CatalogResponse(
                listOf(
                    Catalog(
                        id = "1",
                        name = "Movie catalog"
                    )
                ),
                1
            )
        )
    }
}