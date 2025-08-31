// ABOUTME: Implementation of SportsRepository interface for sports data operations
// ABOUTME: Handles API calls for games and sport types with authentication and error handling

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

import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.SportsResponse
import com.google.wiltv.data.models.SportTypesResponse
import com.google.wiltv.data.network.SportsService
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SportsRepositoryImpl @Inject constructor(
    private val sportsService: SportsService,
    private val userRepository: UserRepository
) : SportsRepository {

    override suspend fun getGamesForHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): ApiResult<SportsResponse, DataError.Network> {
        Logger.i { "Fetching games for hero section with token: $token" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        
        val response = sportsService.getGames(
            authToken = "Bearer $token",
            showInHeroSection = 1,
            page = page,
            itemsPerPage = itemsPerPage
        )
        return mapToResult(response)
    }

    override suspend fun getGamesBySportType(
        token: String,
        sportTypeId: Int,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<SportsResponse, DataError.Network> {
        Logger.i { "Fetching games for sport type $sportTypeId with token: $token" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        
        val response = sportsService.getGames(
            authToken = "Bearer $token",
            sportTypeId = sportTypeId,
            itemsPerPage = itemsPerPage,
            page = page
        )
        return mapToResult(response)
    }

    override suspend fun getSportTypes(
        token: String
    ): ApiResult<SportTypesResponse, DataError.Network> {
        Logger.i { "Fetching sport types with token: $token" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        
        val response = sportsService.getSportTypes(
            authToken = "Bearer $token"
        )
        return mapToResult(response)
    }

    override suspend fun getAllLiveGames(
        token: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<SportsResponse, DataError.Network> {
        Logger.i { "Fetching live games with token: $token" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        
        val response = sportsService.getGames(
            authToken = "Bearer $token",
            isLive = 1,
            itemsPerPage = itemsPerPage,
            page = page
        )
        return mapToResult(response)
    }
}