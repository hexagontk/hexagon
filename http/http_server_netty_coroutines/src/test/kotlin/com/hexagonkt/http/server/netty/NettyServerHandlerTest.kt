package com.hexagonkt.http.server.netty

import com.hexagonkt.http.model.*
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NettyServerHandlerTest {

    @Test fun `Statuses are mapped properly`() {
        val handler = NettyServerHandler(emptyMap(), null)

        fun check(hexagonStatus: HttpStatus, nettyStatus: HttpResponseStatus) {
            assertEquals(nettyStatus, handler.nettyStatus(hexagonStatus))
        }

        check(CONTINUE_100, CONTINUE)
        check(SWITCHING_PROTOCOLS_101, SWITCHING_PROTOCOLS)
        check(PROCESSING_102, PROCESSING)

        check(OK_200, OK)
        check(CREATED_201, CREATED)
        check(ACCEPTED_202, ACCEPTED)
        check(NON_AUTHORITATIVE_INFORMATION_203, NON_AUTHORITATIVE_INFORMATION)
        check(NO_CONTENT_204, NO_CONTENT)
        check(RESET_CONTENT_205, RESET_CONTENT)
        check(PARTIAL_CONTENT_206, PARTIAL_CONTENT)
        check(MULTI_STATUS_207, MULTI_STATUS)

        check(MULTIPLE_CHOICES_300, MULTIPLE_CHOICES)
        check(MOVED_PERMANENTLY_301, MOVED_PERMANENTLY)
        check(FOUND_302, FOUND)
        check(SEE_OTHER_303, SEE_OTHER)
        check(NOT_MODIFIED_304, NOT_MODIFIED)
        check(USE_PROXY_305, USE_PROXY)
        check(TEMPORARY_REDIRECT_307, TEMPORARY_REDIRECT)
        check(PERMANENT_REDIRECT_308, PERMANENT_REDIRECT)

        check(BAD_REQUEST_400, BAD_REQUEST)
        check(NOT_FOUND_404, NOT_FOUND)
        check(UNAUTHORIZED_401, UNAUTHORIZED)
        check(PAYMENT_REQUIRED_402, PAYMENT_REQUIRED)
        check(FORBIDDEN_403, FORBIDDEN)
        check(METHOD_NOT_ALLOWED_405, METHOD_NOT_ALLOWED)
        check(NOT_ACCEPTABLE_406, NOT_ACCEPTABLE)
        check(PROXY_AUTHENTICATION_REQUIRED_407, PROXY_AUTHENTICATION_REQUIRED)
        check(REQUEST_TIMEOUT_408, REQUEST_TIMEOUT)
        check(CONFLICT_409, CONFLICT)
        check(GONE_410, GONE)
        check(LENGTH_REQUIRED_411, LENGTH_REQUIRED)
        check(PRECONDITION_FAILED_412, PRECONDITION_FAILED)
        check(URI_TOO_LONG_414, REQUEST_URI_TOO_LONG)
        check(UNSUPPORTED_MEDIA_TYPE_415, UNSUPPORTED_MEDIA_TYPE)
        check(RANGE_NOT_SATISFIABLE_416, REQUESTED_RANGE_NOT_SATISFIABLE)
        check(EXPECTATION_FAILED_417, EXPECTATION_FAILED)
        check(MISDIRECTED_REQUEST_421, MISDIRECTED_REQUEST)
        check(UNPROCESSABLE_CONTENT_422, UNPROCESSABLE_ENTITY)
        check(LOCKED_423, LOCKED)
        check(FAILED_DEPENDENCY_424, FAILED_DEPENDENCY)
        check(UPGRADE_REQUIRED_426, UPGRADE_REQUIRED)
        check(PRECONDITION_REQUIRED_428, PRECONDITION_REQUIRED)
        check(TOO_MANY_REQUESTS_429, TOO_MANY_REQUESTS)
        check(REQUEST_HEADER_FIELDS_TOO_LARGE_431, REQUEST_HEADER_FIELDS_TOO_LARGE)

        check(INTERNAL_SERVER_ERROR_500, INTERNAL_SERVER_ERROR)
        check(NOT_IMPLEMENTED_501, NOT_IMPLEMENTED)
        check(BAD_GATEWAY_502, BAD_GATEWAY)
        check(SERVICE_UNAVAILABLE_503, SERVICE_UNAVAILABLE)
        check(GATEWAY_TIMEOUT_504, GATEWAY_TIMEOUT)
        check(HTTP_VERSION_NOT_SUPPORTED_505, HTTP_VERSION_NOT_SUPPORTED)
        check(VARIANT_ALSO_NEGOTIATES_506, VARIANT_ALSO_NEGOTIATES)
        check(INSUFFICIENT_STORAGE_507, INSUFFICIENT_STORAGE)
        check(NOT_EXTENDED_510, NOT_EXTENDED)
        check(NETWORK_AUTHENTICATION_REQUIRED_511, NETWORK_AUTHENTICATION_REQUIRED)

        val status = HttpStatus(599)
        check(status, HttpResponseStatus(599, status.toString()))
    }
}
