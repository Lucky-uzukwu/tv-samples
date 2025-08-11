package com.google.wiltv.data.repositories

import com.google.gson.Gson
import com.google.wiltv.data.repositories.errors.Error
import com.google.wiltv.data.repositories.errors.ValidationError
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import retrofit2.Response


fun <T> mapToResult(response: Response<T>): ApiResult<T, DataError.Network> =
    if (response.isSuccessful) {
        mapSuccessResponse(response)
    } else {
        mapErrorResponse(response)
    }

fun <T> mapErrorResponse(response: Response<T>): ApiResult.Error<T, DataError.Network> {
    val gson = Gson()
    return when (response.code()) {
        400 -> {
            ApiResult.Error(DataError.Network.BAD_REQUEST, response.message())
        }

        403 -> {
            val errorBody = response.errorBody()?.string()
            val error = gson.fromJson(errorBody, Error::class.java)
            ApiResult.Error(DataError.Network.FORBIDDEN, error.detail)
        }

        404 -> {
            val errorBody = response.errorBody()?.string()
            val error = gson.fromJson(errorBody, Error::class.java)
            ApiResult.Error(DataError.Network.NOT_FOUND, error.title + ": " + "user not found")
        }

        422 -> {
            val errorBody = response.errorBody()?.string()
            val validationError = gson.fromJson(errorBody, ValidationError::class.java)
            ApiResult.Error(DataError.Network.VALIDATION_ERROR, validationError.detail)
        }

        500 -> ApiResult.Error(DataError.Network.SERVER_ERROR, response.message())
        else -> ApiResult.Error(DataError.Network.UNKNOWN, response.message())
    }
}


fun <T> mapSuccessResponse(response: Response<T>): ApiResult.Success<T, DataError.Network> =
    ApiResult.Success(response.body()!!)



