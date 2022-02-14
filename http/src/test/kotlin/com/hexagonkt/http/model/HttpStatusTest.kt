package com.hexagonkt.http.model

import com.hexagonkt.http.model.InformationStatus.*
import com.hexagonkt.http.model.SuccessStatus.*
import com.hexagonkt.http.model.RedirectionStatus.*
import com.hexagonkt.http.model.ClientErrorStatus.*
import com.hexagonkt.http.model.ServerErrorStatus.*
import com.hexagonkt.http.model.HttpStatusType.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HttpStatusTest {

    @Test fun `HTTP status codes can be retrieved or created`() {
        assertEquals(HttpStatus.codes[200], HttpStatus(200))
        assertEquals(CustomStatus(599), HttpStatus(599))
    }

    @Test fun `HTTP status codes can be retrieved as a map`() {
        assertEquals(HttpStatus.codes[200], HttpStatus[200])
    }

    @Test fun `HTTP status codes contains the proper type`() {
        assertEquals(INFORMATION, CONTINUE.type)
        assertEquals(INFORMATION, SWITCHING_PROTOCOLS.type)
        assertEquals(INFORMATION, PROCESSING.type)
        assertEquals(INFORMATION, EARLY_HINTS.type)

        assertEquals(SUCCESS, OK.type)
        assertEquals(SUCCESS, CREATED.type)
        assertEquals(SUCCESS, ACCEPTED.type)
        assertEquals(SUCCESS, NON_AUTHORITATIVE_INFORMATION.type)
        assertEquals(SUCCESS, NO_CONTENT.type)
        assertEquals(SUCCESS, RESET_CONTENT.type)
        assertEquals(SUCCESS, PARTIAL_CONTENT.type)
        assertEquals(SUCCESS, MULTI_STATUS.type)
        assertEquals(SUCCESS, ALREADY_REPORTED.type)
        assertEquals(SUCCESS, IM_USED.type)

        assertEquals(REDIRECTION, MULTIPLE_CHOICES.type)
        assertEquals(REDIRECTION, MOVED_PERMANENTLY.type)
        assertEquals(REDIRECTION, FOUND.type)
        assertEquals(REDIRECTION, SEE_OTHER.type)
        assertEquals(REDIRECTION, NOT_MODIFIED.type)
        assertEquals(REDIRECTION, USE_PROXY.type)
        assertEquals(REDIRECTION, TEMPORARY_REDIRECT.type)
        assertEquals(REDIRECTION, PERMANENT_REDIRECT.type)

        assertEquals(CLIENT_ERROR, BAD_REQUEST.type)
        assertEquals(CLIENT_ERROR, UNAUTHORIZED.type)
        assertEquals(CLIENT_ERROR, PAYMENT_REQUIRED.type)
        assertEquals(CLIENT_ERROR, FORBIDDEN.type)
        assertEquals(CLIENT_ERROR, NOT_FOUND.type)
        assertEquals(CLIENT_ERROR, METHOD_NOT_ALLOWED.type)
        assertEquals(CLIENT_ERROR, NOT_ACCEPTABLE.type)
        assertEquals(CLIENT_ERROR, PROXY_AUTHENTICATION_REQUIRED.type)
        assertEquals(CLIENT_ERROR, REQUEST_TIMEOUT.type)
        assertEquals(CLIENT_ERROR, CONFLICT.type)
        assertEquals(CLIENT_ERROR, GONE.type)
        assertEquals(CLIENT_ERROR, LENGTH_REQUIRED.type)
        assertEquals(CLIENT_ERROR, PRECONDITION_FAILED.type)
        assertEquals(CLIENT_ERROR, CONTENT_TOO_LARGE.type)
        assertEquals(CLIENT_ERROR, URI_TOO_LONG.type)
        assertEquals(CLIENT_ERROR, UNSUPPORTED_MEDIA_TYPE.type)
        assertEquals(CLIENT_ERROR, RANGE_NOT_SATISFIABLE.type)
        assertEquals(CLIENT_ERROR, EXPECTATION_FAILED.type)
        assertEquals(CLIENT_ERROR, I_AM_A_TEAPOT.type)
        assertEquals(CLIENT_ERROR, MISDIRECTED_REQUEST.type)
        assertEquals(CLIENT_ERROR, UNPROCESSABLE_CONTENT.type)
        assertEquals(CLIENT_ERROR, LOCKED.type)
        assertEquals(CLIENT_ERROR, FAILED_DEPENDENCY.type)
        assertEquals(CLIENT_ERROR, TOO_EARLY.type)
        assertEquals(CLIENT_ERROR, UPGRADE_REQUIRED.type)
        assertEquals(CLIENT_ERROR, PRECONDITION_REQUIRED.type)
        assertEquals(CLIENT_ERROR, TOO_MANY_REQUESTS.type)
        assertEquals(CLIENT_ERROR, REQUEST_HEADER_FIELDS_TOO_LARGE.type)
        assertEquals(CLIENT_ERROR, UNAVAILABLE_FOR_LEGAL_REASONS.type)

        assertEquals(SERVER_ERROR, INTERNAL_SERVER_ERROR.type)
        assertEquals(SERVER_ERROR, NOT_IMPLEMENTED.type)
        assertEquals(SERVER_ERROR, BAD_GATEWAY.type)
        assertEquals(SERVER_ERROR, SERVICE_UNAVAILABLE.type)
        assertEquals(SERVER_ERROR, GATEWAY_TIMEOUT.type)
        assertEquals(SERVER_ERROR, HTTP_VERSION_NOT_SUPPORTED.type)
        assertEquals(SERVER_ERROR, VARIANT_ALSO_NEGOTIATES.type)
        assertEquals(SERVER_ERROR, INSUFFICIENT_STORAGE.type)
        assertEquals(SERVER_ERROR, LOOP_DETECTED.type)
        assertEquals(SERVER_ERROR, NOT_EXTENDED.type)
        assertEquals(SERVER_ERROR, NETWORK_AUTHENTICATION_REQUIRED.type)
    }
}
