package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.GenreResponse
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError

interface GenreRepository {
    suspend fun getMovieGenre(): ApiResult<GenreResponse, DataError.Network>
    suspend fun getTvShowsGenre(): ApiResult<GenreResponse, DataError.Network>
    suspend fun getTvChannelGenre(): ApiResult<GenreResponse, DataError.Network>
}
