package com.hexagonkt.http.model

import com.hexagonkt.http.model.HttpStatusType.*

/**
 * Supported HTTP responses status.
 */
enum class ServerErrorStatus(
    override val code: Int,
    override val type: HttpStatusType
) : HttpStatus {

    INTERNAL_SERVER_ERROR(500, SERVER_ERROR),
    NOT_IMPLEMENTED(501, SERVER_ERROR),
    BAD_GATEWAY(502, SERVER_ERROR),
    SERVICE_UNAVAILABLE(503, SERVER_ERROR),
    GATEWAY_TIMEOUT(504, SERVER_ERROR),
    HTTP_VERSION_NOT_SUPPORTED(505, SERVER_ERROR),
    VARIANT_ALSO_NEGOTIATES(506, SERVER_ERROR),
    INSUFFICIENT_STORAGE(507, SERVER_ERROR),
    LOOP_DETECTED(508, SERVER_ERROR),
    NOT_EXTENDED(510, SERVER_ERROR),
    NETWORK_AUTHENTICATION_REQUIRED(511, SERVER_ERROR),
}
