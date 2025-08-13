package com.google.wiltv.data.network

import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.models.ViewDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SearchService {

    @GET("/search")
    suspend fun searchMovie(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("search") search: String = "5 IN [‚id of sp‘]",
//        @Query("types[]") type: String = "App\\Models\\Movie",
//        @Query("search") search: String = "5 IN [‚id of sp‘]",
//        @Query("types") types: List<String>,
//        @Query("genres") genres: List<String>,
//        @Query("catalogs") catalogs: List<String>,
//        @Query("year") year: List<String>,
//        @Query("streamingProviders") streamingProviders: List<String>,
//        @Query("sportTypes") sportTypes: List<String>,
//        @Query("teamA.name") teamAName: List<String>,
//        @Query("teamB.name") teamBName: List<String>,
//        @Query("competition") competition: List<String>,
        @Query("isAdultContent") isAdultContent: Int? = null,
        @Query("isKidsContent") isKidsContent: Int? = null,
        @Query("page") page: Int = 1,
        @Query("itemsPerPage") itemsPerPage: Int = 10,
    ): Response<MovieSearchResponse>

    @GET("/search")
    suspend fun searchTvShows(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("search") search: String = "5 IN [‚id of sp‘]",
        @Query("isAdultContent") isAdultContent: Int? = null,
        @Query("isKidsContent") isKidsContent: Int? = null,
//        @Query("types[]") type: String = "App\\Models\\TvShow",
//        @Query("search") search: String = "5 IN [‚id of sp‘]",
//        @Query("types") types: List<String>,
//        @Query("genres") genres: List<String>,
//        @Query("catalogs") catalogs: List<String>,
//        @Query("year") year: List<String>,
//        @Query("streamingProviders") streamingProviders: List<String>,
//        @Query("sportTypes") sportTypes: List<String>,
//        @Query("teamA.name") teamAName: List<String>,
//        @Query("teamB.name") teamBName: List<String>,
//        @Query("competition") competition: List<String>,
//        @Query("isKidsContent") isKidsContent: Boolean = false,
//        @Query("isAdultContent") isAdultContent: Boolean = false,
        @Query("page") page: Int = 1,
        @Query("itemsPerPage") itemsPerPage: Int = 10,
    ): Response<ShowSearchResponse>
}


data class MovieSearchResponse(
    val member: List<MovieNew>,
    val totalItems: Int? = null,
    val viewDetails: ViewDetails? = null,
)

data class ShowSearchResponse(
    val member: List<TvShow>,
    val totalItems: Int? = null,
    val viewDetails: ViewDetails? = null,
)
