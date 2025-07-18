package com.google.jetstream.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CatalogService {

    @GET("/catalogs")
    suspend fun getCatalogs(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("type") type: String? = null,
    ): Response<CatalogResponse>
}

data class CatalogResponse(
    val member: List<Catalog>,
    val totalItems: Int? = null,
)

data class Catalog(
    val id: String,
    val name: String,
)