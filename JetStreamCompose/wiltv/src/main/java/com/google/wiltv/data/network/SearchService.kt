package com.google.wiltv.data.network

import com.google.wiltv.data.models.UnifiedSearchResponse
import com.google.wiltv.data.models.SearchTemplate
import com.google.wiltv.data.models.PartialCollectionView
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SearchService {

    @GET("/search")
    suspend fun search(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("search") search: String,
        @Query("types[]") types: List<String>? = null,
        @Query("genres[]") genres: List<String>? = null,
        @Query("catalogs[]") catalogs: List<String>? = null,
        @Query("year[]") year: List<String>? = null,
        @Query("streamingProviders[]") streamingProviders: List<String>? = null,
        @Query("sportTypes[]") sportTypes: List<String>? = null,
        @Query("teamA.name[]") teamAName: List<String>? = null,
        @Query("teamB.name[]") teamBName: List<String>? = null,
        @Query("competition[]") competition: List<String>? = null,
        @Query("isAdultContent") isAdultContent: Int? = null,
        @Query("isKidsContent") isKidsContent: Int? = null,
        @Query("page") page: Int = 1,
        @Query("itemsPerPage") itemsPerPage: Int = 10,
    ): Response<UnifiedSearchResponse>

    @GET("/search/auto_complete")
    suspend fun getAutocomplete(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("search") query: String,
        @Query("type") type: String? = null
    ): Response<AutocompleteResponse>
}



data class AutocompleteResponse(
    val member: List<String>,
    val totalItems: Int? = null,
    val search: SearchTemplate? = null,
    val view: PartialCollectionView? = null
)

