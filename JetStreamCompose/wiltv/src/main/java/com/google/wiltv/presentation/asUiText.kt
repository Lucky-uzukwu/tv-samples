package com.google.wiltv.presentation

import com.google.wiltv.R
import com.google.wiltv.domain.DataError
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.presentation.UiText.*

fun DataError.asUiText(message: String? = null): UiText {
    return when (this) {
        DataError.Network.REQUEST_TIMEOUT -> message?.let { DynamicString(it) } ?: StringResource(
            R.string.the_request_timed_out
        )

        DataError.Network.TOO_MANY_REQUESTS -> message?.let { DynamicString(it) } ?: StringResource(
            R.string.youve_hit_your_rate_limit
        )

        DataError.Network.NO_INTERNET -> message?.let { DynamicString(it) } ?: StringResource(
            R.string.no_internet
        )

        DataError.Network.PAYLOAD_TOO_LARGE -> message?.let { DynamicString(it) } ?: StringResource(
            R.string.file_too_large
        )

        DataError.Network.SERVER_ERROR -> message?.let { DynamicString(it) } ?: StringResource(
            R.string.server_error
        )

        DataError.Network.SERIALIZATION -> message?.let { DynamicString(it) } ?: StringResource(
            R.string.error_serialization
        )

        DataError.Network.UNKNOWN -> message?.let { DynamicString(it) } ?: StringResource(
            R.string.unknown_error
        )

        DataError.Local.DISK_FULL -> message?.let { DynamicString(it) } ?: StringResource(
            R.string.error_disk_full
        )

        DataError.Network.BAD_REQUEST -> if (message == "") {
            StringResource(R.string.bad_request)
        } else {
            DynamicString(message!!)
        }

        DataError.Network.VALIDATION_ERROR -> if (message == "") {
            StringResource(R.string.validation_error)
        } else {
            DynamicString(message!!)
        }

        DataError.Network.NOT_FOUND -> if (message == "") {
            StringResource(R.string.not_found)
        } else {
            DynamicString(message!!)
        }

        DataError.Network.FORBIDDEN -> if (message == "") {
            StringResource(R.string.bad_request)
        } else {
            DynamicString(message!!)
        }
    }
}

fun ApiResult.Error<*, DataError>.asErrorUiText(): UiText {
    return error.asUiText()
}