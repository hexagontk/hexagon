package com.hexagonkt.http.model

import com.hexagonkt.http.model.HttpStatusType.*

/**
 * Supported HTTP responses status.
 */
enum class SuccessStatus(
    override val code: Int,
    override val type: HttpStatusType
) : HttpStatus {

    OK(200, SUCCESS),
    CREATED(201, SUCCESS),
    ACCEPTED(202, SUCCESS),
    NON_AUTHORITATIVE_INFORMATION(203, SUCCESS),
    NO_CONTENT(204, SUCCESS),
    RESET_CONTENT(205, SUCCESS),
    PARTIAL_CONTENT(206, SUCCESS),
    MULTI_STATUS(207, SUCCESS),
    ALREADY_REPORTED(208, SUCCESS),
    IM_USED(226, SUCCESS),
}
