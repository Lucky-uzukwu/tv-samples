// ABOUTME: Repository interface for sports data operations
// ABOUTME: Defines methods for fetching games, sport types, and filtered game data

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

package com.google.wiltv.data.repositories

import com.google.wiltv.data.models.SportsResponse
import com.google.wiltv.data.models.SportTypesResponse
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError

interface SportsRepository {
    suspend fun getGamesForHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int,
    ): ApiResult<SportsResponse, DataError.Network>

    suspend fun getGamesBySportType(
        token: String,
        sportTypeId: Int,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<SportsResponse, DataError.Network>

    suspend fun getSportTypes(
        token: String
    ): ApiResult<SportTypesResponse, DataError.Network>

    suspend fun getAllLiveGames(
        token: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<SportsResponse, DataError.Network>
}