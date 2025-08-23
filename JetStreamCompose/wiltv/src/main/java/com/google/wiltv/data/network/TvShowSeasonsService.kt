package com.google.wiltv.data.network

import com.google.wiltv.data.models.TvShowSeasonsResponse
import com.google.wiltv.data.models.TvShowEpisodesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TvShowSeasonsService {

    @GET("/tv_shows/{tvShowId}/seasons")
    suspend fun getTvShowSeasons(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Path("tvShowId") tvShowId: Int
    ): Response<TvShowSeasonsResponse>

    @GET("/tv_shows/{tvShowId}/seasons/{seasonId}/episodes")
    suspend fun getTvShowSeasonEpisodes(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Path("tvShowId") tvShowId: Int,
        @Path("seasonId") seasonId: Int,
        @Query("page") page: Int? = 1,
        @Query("itemsPerPage") itemsPerPage: Int? = 30
    ): Response<TvShowEpisodesResponse>
}