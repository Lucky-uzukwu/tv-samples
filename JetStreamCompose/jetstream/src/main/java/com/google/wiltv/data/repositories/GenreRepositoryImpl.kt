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
            // Handle HTTP error codes
            val errorBody =
                response.errorBody()?.string() // Get error message from server if available
            Logger.e { "API Error for getMovieGenre: ${response.code()} - ${response.message()}. Error body: $errorBody" }
            val loginResponse = user.password?.let {
                authRepository.login(
                    deviceMacAddress = user.deviceMacAddress,
                    clientIp = user.clientIp,
                    deviceName = user.deviceName,
                    identifier = user.identifier,
                    password = it
                )
            }
            when (loginResponse?.code()) {
                201 -> {
                    userRepository.saveUserToken(loginResponse.body()!!.token)
                    getMovieGenre()
                }

                else -> {
                    Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
                }
            }
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
            // Handle HTTP error codes
            val errorBody =
                response.errorBody()?.string() // Get error message from server if available
            Logger.e { "API Error for getTvShowsGenre: ${response.code()} - ${response.message()}. Error body: $errorBody" }
            val loginResponse = user.password?.let {
                authRepository.login(
                    deviceMacAddress = user.deviceMacAddress,
                    clientIp = user.clientIp,
                    deviceName = user.deviceName,
                    identifier = user.identifier,
                    password = it
                )
            }
            when (loginResponse?.code()) {
                201 -> {
                    userRepository.saveUserToken(loginResponse.body()!!.token)
                    getTvShowsGenre()
                }

                else -> {
                    Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
                }
            }
        }

    }
}