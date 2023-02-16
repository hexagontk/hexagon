package com.hexagonkt.http.model

import com.hexagonkt.core.assertEnabled
import com.hexagonkt.http.model.HttpStatusType.*
import kotlin.IllegalArgumentException

data class HttpStatus(
    val code: Int,
    val type: HttpStatusType = when (code) {
        in 100..199 -> INFORMATION
        in 200..299 -> SUCCESS
        in 300..399 -> REDIRECTION
        in 400..499 -> CLIENT_ERROR
        in 500..599 -> SERVER_ERROR
        else -> throw IllegalArgumentException(INVALID_CODE_ERROR_MESSAGE + code)
    }
) {

    companion object {
        internal const val INVALID_CODE_ERROR_MESSAGE: String =
            "Error code is not in any HTTP status range (100..599): "

        val codes: Map<Int, HttpStatus> by lazy {
            HTTP_STATUSES.associateBy { it.code }
        }

        operator fun get(code: Int): HttpStatus? =
            codes[code]

        operator fun invoke(code: Int): HttpStatus =
            codes[code] ?: HttpStatus(code)
    }

    init {
        if (assertEnabled)
            require(code in 100..599) { INVALID_CODE_ERROR_MESSAGE + code }
    }
}
