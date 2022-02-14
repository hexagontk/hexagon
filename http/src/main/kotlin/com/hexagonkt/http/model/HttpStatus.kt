package com.hexagonkt.http.model

/**
 * Supported HTTP responses status.
 */
interface HttpStatus {
    val code: Int
    val type: HttpStatusType

    companion object {
        val codes: Map<Int, HttpStatus> =
            listOf(
                InformationStatus.values(),
                SuccessStatus.values(),
                RedirectionStatus.values(),
                ClientErrorStatus.values(),
                ServerErrorStatus.values(),
            )
            .flatMap { it.toList() }
            .associateBy { it.code }

        operator fun get(code: Int): HttpStatus? =
            codes[code]

        operator fun invoke(code: Int): HttpStatus =
            codes[code] ?: CustomStatus(code)
    }
}
