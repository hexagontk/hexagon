package com.hexagonkt.http.model

import com.hexagonkt.http.model.HttpStatusType.*

/**
 * Supported HTTP responses status.
 */
enum class InformationStatus(
    override val code: Int,
    override val type: HttpStatusType
) : HttpStatus {

    CONTINUE(100, INFORMATION),
    SWITCHING_PROTOCOLS(101, INFORMATION),
    PROCESSING(102, INFORMATION),
    EARLY_HINTS(103, INFORMATION),
}
