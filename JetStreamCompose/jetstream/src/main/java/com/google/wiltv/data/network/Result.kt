package com.google.wiltv.data.network

import retrofit2.HttpException
import java.io.IOException

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
}


suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.Success(apiCall())
    } catch (e: HttpException) {
        Result.Error(
            message = e.response()?.errorBody()?.string() ?: "HTTP ${e.code()} error",
            code = e.code()
        )
    } catch (e: IOException) {
        Result.Error("Network error: ${e.localizedMessage}")
    } catch (e: Exception) {
        Result.Error("Unexpected error: ${e.localizedMessage}")
    }
}
