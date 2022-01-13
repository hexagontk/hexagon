package com.hexagonkt.http.model

import com.hexagonkt.http.model.HttpStatusType.*

/**
 * Supported HTTP responses status.
 */
enum class RedirectionStatus(
    override val code: Int,
    override val type: HttpStatusType
) : HttpStatus {

    MULTIPLE_CHOICES(300, REDIRECTION),
    MOVED_PERMANENTLY(301, REDIRECTION),
    FOUND(302, REDIRECTION),
    SEE_OTHER(303, REDIRECTION),
    NOT_MODIFIED(304, REDIRECTION),
    USE_PROXY(305, REDIRECTION),
    TEMPORARY_REDIRECT(307, REDIRECTION),
    PERMANENT_REDIRECT(308, REDIRECTION),
}
