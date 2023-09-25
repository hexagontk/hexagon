package com.hexagonkt.http.model

import com.hexagonkt.http.model.HttpStatusType.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HttpStatusesTest {

    @Test fun `HTTP status codes can be retrieved or created`() {
        assertEquals(HttpStatus.codes[200], HttpStatus(200))
    }

    @Test fun `HTTP status codes can be retrieved as a map`() {
        assertEquals(HttpStatus.codes[200], HttpStatus[200])
    }

    @Test fun `HTTP status codes contains the proper type`() {
        assertEquals(INFORMATION, CONTINUE_100.type)
        assertEquals(INFORMATION, SWITCHING_PROTOCOLS_101.type)
        assertEquals(INFORMATION, PROCESSING_102.type)
        assertEquals(INFORMATION, EARLY_HINTS_103.type)

        assertEquals(SUCCESS, OK_200.type)
        assertEquals(SUCCESS, CREATED_201.type)
        assertEquals(SUCCESS, ACCEPTED_202.type)
        assertEquals(SUCCESS, NON_AUTHORITATIVE_INFORMATION_203.type)
        assertEquals(SUCCESS, NO_CONTENT_204.type)
        assertEquals(SUCCESS, RESET_CONTENT_205.type)
        assertEquals(SUCCESS, PARTIAL_CONTENT_206.type)
        assertEquals(SUCCESS, MULTI_STATUS_207.type)
        assertEquals(SUCCESS, ALREADY_REPORTED_208.type)
        assertEquals(SUCCESS, IM_USED_226.type)

        assertEquals(REDIRECTION, MULTIPLE_CHOICES_300.type)
        assertEquals(REDIRECTION, MOVED_PERMANENTLY_301.type)
        assertEquals(REDIRECTION, FOUND_302.type)
        assertEquals(REDIRECTION, SEE_OTHER_303.type)
        assertEquals(REDIRECTION, NOT_MODIFIED_304.type)
        assertEquals(REDIRECTION, USE_PROXY_305.type)
        assertEquals(REDIRECTION, TEMPORARY_REDIRECT_307.type)
        assertEquals(REDIRECTION, PERMANENT_REDIRECT_308.type)

        assertEquals(CLIENT_ERROR, BAD_REQUEST_400.type)
        assertEquals(CLIENT_ERROR, UNAUTHORIZED_401.type)
        assertEquals(CLIENT_ERROR, PAYMENT_REQUIRED_402.type)
        assertEquals(CLIENT_ERROR, FORBIDDEN_403.type)
        assertEquals(CLIENT_ERROR, NOT_FOUND_404.type)
        assertEquals(CLIENT_ERROR, METHOD_NOT_ALLOWED_405.type)
        assertEquals(CLIENT_ERROR, NOT_ACCEPTABLE_406.type)
        assertEquals(CLIENT_ERROR, PROXY_AUTHENTICATION_REQUIRED_407.type)
        assertEquals(CLIENT_ERROR, REQUEST_TIMEOUT_408.type)
        assertEquals(CLIENT_ERROR, CONFLICT_409.type)
        assertEquals(CLIENT_ERROR, GONE_410.type)
        assertEquals(CLIENT_ERROR, LENGTH_REQUIRED_411.type)
        assertEquals(CLIENT_ERROR, PRECONDITION_FAILED_412.type)
        assertEquals(CLIENT_ERROR, CONTENT_TOO_LARGE_413.type)
        assertEquals(CLIENT_ERROR, URI_TOO_LONG_414.type)
        assertEquals(CLIENT_ERROR, UNSUPPORTED_MEDIA_TYPE_415.type)
        assertEquals(CLIENT_ERROR, RANGE_NOT_SATISFIABLE_416.type)
        assertEquals(CLIENT_ERROR, EXPECTATION_FAILED_417.type)
        assertEquals(CLIENT_ERROR, I_AM_A_TEAPOT_418.type)
        assertEquals(CLIENT_ERROR, MISDIRECTED_REQUEST_421.type)
        assertEquals(CLIENT_ERROR, UNPROCESSABLE_CONTENT_422.type)
        assertEquals(CLIENT_ERROR, LOCKED_423.type)
        assertEquals(CLIENT_ERROR, FAILED_DEPENDENCY_424.type)
        assertEquals(CLIENT_ERROR, TOO_EARLY_425.type)
        assertEquals(CLIENT_ERROR, UPGRADE_REQUIRED_426.type)
        assertEquals(CLIENT_ERROR, PRECONDITION_REQUIRED_428.type)
        assertEquals(CLIENT_ERROR, TOO_MANY_REQUESTS_429.type)
        assertEquals(CLIENT_ERROR, REQUEST_HEADER_FIELDS_TOO_LARGE_431.type)
        assertEquals(CLIENT_ERROR, UNAVAILABLE_FOR_LEGAL_REASONS_451.type)

        assertEquals(SERVER_ERROR, INTERNAL_SERVER_ERROR_500.type)
        assertEquals(SERVER_ERROR, NOT_IMPLEMENTED_501.type)
        assertEquals(SERVER_ERROR, BAD_GATEWAY_502.type)
        assertEquals(SERVER_ERROR, SERVICE_UNAVAILABLE_503.type)
        assertEquals(SERVER_ERROR, GATEWAY_TIMEOUT_504.type)
        assertEquals(SERVER_ERROR, HTTP_VERSION_NOT_SUPPORTED_505.type)
        assertEquals(SERVER_ERROR, VARIANT_ALSO_NEGOTIATES_506.type)
        assertEquals(SERVER_ERROR, INSUFFICIENT_STORAGE_507.type)
        assertEquals(SERVER_ERROR, LOOP_DETECTED_508.type)
        assertEquals(SERVER_ERROR, NOT_EXTENDED_510.type)
        assertEquals(SERVER_ERROR, NETWORK_AUTHENTICATION_REQUIRED_511.type)
    }
}
