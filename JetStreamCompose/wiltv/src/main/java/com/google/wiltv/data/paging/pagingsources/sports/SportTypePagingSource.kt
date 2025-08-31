// ABOUTME: Paging source for games filtered by sport type
// ABOUTME: Fetches competition games for a specific sport with pagination support

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

package com.google.wiltv.data.paging.pagingsources.sports

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.data.repositories.SportsRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import kotlinx.coroutines.flow.firstOrNull

class SportTypePagingSource(
    private val sportsRepository: SportsRepository,
    private val userRepository: UserRepository,
    private val sportTypeId: Int
) : PagingSource<Int, CompetitionGame>() {
    override fun getRefreshKey(state: PagingState<Int, CompetitionGame>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CompetitionGame> {
        return try {
            val token = userRepository.userToken.firstOrNull() ?: return LoadResult.Error(
                Exception("No token")
            )
            val currentPage = params.key ?: 1

            val gamesResult = sportsRepository.getGamesBySportType(
                token,
                sportTypeId,
                itemsPerPage = params.loadSize,
                page = currentPage
            )
            
            val games = when (gamesResult) {
                is ApiResult.Success -> gamesResult.data
                is ApiResult.Error -> return LoadResult.Error(
                    Exception("Failed to fetch games for sport type $sportTypeId: ${gamesResult.message ?: gamesResult.error}")
                )
            }

            LoadResult.Page(
                data = games.member,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (games.member.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}