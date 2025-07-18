package com.google.jetstream.data.network

import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.models.ViewDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface StreamingProviderService {

    @GET("/streaming_providers")
    suspend fun getStreamingProviders(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("page") page: Int? = 1,
        @Query("itemsPerPage") itemsPerPage: Int? = 15,
        @Query("type") type: String,
    ): Response<StreamingProviderResponse>
}

data class StreamingProviderResponse(
    val member: List<StreamingProvider>,
    val totalItems: Int? = null,
    val viewDetails: ViewDetails? = null,
//    val itemsPerPage: Int? = null
)


