package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.network.GenreService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenreRepositoryImpl @Inject constructor(
    private val genreService: GenreService,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : GenreRepository {
    override fun getMovieGenre(): Flow<List<Genre>> = flow {
        val user = userRepository.getUser() ?: return@flow
        val response = genreService.getGenres(
            authToken = "Bearer ${user.token}",
            isMovieGenre = 1
        )

        if (response.isSuccessful) {
            val genres = response.body()
            Logger.i { "API Response: $genres" }
            Logger.i { "Successfully fetched ${genres?.member?.size} genres for movie section." }
            if (genres != null) {
                emit(genres.member)
            }
        } else {
            // TODO Handle HTTP error codes
        }

    }

    override fun getTvShowsGenre(): Flow<List<Genre>> = flow {
        val user = userRepository.getUser() ?: return@flow
        val response = genreService.getGenres(
            authToken = "Bearer ${user.token}",
            isTvShowGenre = 1
        )

        if (response.isSuccessful) {
            val genres = response.body()
            Logger.i { "API Response: $genres" }
            Logger.i { "Successfully fetched ${genres?.member?.size} genres for tv show section." }
            if (genres != null) {
                emit(genres.member)
            }
        } else {
            // TODO Handle HTTP error codes
        }

    }
}