// ABOUTME: Retrofit service interface for sports-related API endpoints
// ABOUTME: Handles games and sport types API calls with authentication headers

/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.wiltv.data.network

import com.google.wiltv.data.models.SportsResponse
import com.google.wiltv.data.models.SportTypesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SportsService {

    @GET("/games")
    suspend fun getGames(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("page") page: Int? = 1,
        @Query("itemsPerPage") itemsPerPage: Int? = 30,
        @Query("showInHeroSection") showInHeroSection: Int? = null,
        @Query("isLive") isLive: Int? = null,
        @Query("competition.sportType[]") sportTypeId: Int? = null,
        @Query("teamAId") teamAId: Int? = null,
        @Query("teamBId") teamBId: Int? = null,
        @Query("competitionId") competitionId: Int? = null
    ): Response<SportsResponse>

    @GET("/sport_types")
    suspend fun getSportTypes(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json"
    ): Response<SportTypesResponse>
}