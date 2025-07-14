package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.Catalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockCatalogRepositoryImpl : CatalogRepository {
    override fun getMovieCatalog(): Flow<List<Catalog>> = flow {

        emit(
            listOf(
                Catalog(
                    id = "1",
                    name = "Movie catalog"
                )
            )
        )
    }

    override fun getTvShowCatalog(): Flow<List<Catalog>> = flow {
        emit(
            listOf(
                Catalog(
                    id = "1",
                    name = "Show catalog"
                )
            )
        )
    }
}