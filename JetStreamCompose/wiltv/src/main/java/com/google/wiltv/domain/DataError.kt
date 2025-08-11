package com.google.wiltv.domain

sealed interface DataError : Error {
    enum class Network : DataError {
        BAD_REQUEST,

        NOT_FOUND,
        FORBIDDEN,
        VALIDATION_ERROR,
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN,
        LOCAL_USER_NOT_FOUND,
    }

    enum class Local : DataError {
        DISK_FULL,

    }
}