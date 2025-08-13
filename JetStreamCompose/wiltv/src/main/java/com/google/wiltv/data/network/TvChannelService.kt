package com.google.wiltv.data.network

import com.google.wiltv.data.models.Country
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.ViewDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TvChannelService {

    @GET("/tv_channels")
    suspend fun getTvChannels(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("page") page: Int? = 1,
        @Query("itemsPerPage") itemsPerPage: Int? = 15,
//        @Query("search") search: String? = null,
        @Query("isAdultContent") isAdultContent: Int? = null,
        @Query("isKidsContent") isKidsContent: Int? = null,
        @Query("genres[]") genreId: Int? = null,
        @Query("showInHeroSection") showInHeroSection: Int? = null,
    ): Response<TvChannelsResponse>
}

data class TvChannelsResponse(
    val member: List<TvChannel>,
    val totalItems: Int? = null,
    val viewDetails: ViewDetails? = null,
)

data class TvChannel(
    val id: Int,
    val name: String,
    val showInHeroSection: Boolean,
    val isAdultContent: Boolean,
    val isKidsContent: Boolean,
    val priority: Int?,
    val logoPath: String,
    val active: Boolean,
    val logoUrl: String,
    val playLink: String,
    val language: String?,
    val genres: List<Genre>,
    val countries: List<Country>
)