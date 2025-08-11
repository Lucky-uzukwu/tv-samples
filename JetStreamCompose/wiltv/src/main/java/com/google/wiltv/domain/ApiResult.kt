package com.google.wiltv.domain

typealias RootError = Error

sealed interface ApiResult<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D) : ApiResult<D, E>
    data class Error<out D, out E : RootError>(val error: E, val message: String? = null) :
        ApiResult<D, E>
}