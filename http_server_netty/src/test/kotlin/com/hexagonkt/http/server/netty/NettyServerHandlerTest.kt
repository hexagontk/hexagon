package com.hexagonkt.http.server.netty

import com.hexagonkt.http.model.*
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NettyServerHandlerTest {

    @Test fun `Statuses are mapped properly`() {
        val handler = NettyServerHandler(emptyMap(), null)

        fun check(hexagonStatus: HttpStatus, nettyStatus: HttpResponseStatus) {
            assertEquals(nettyStatus, handler.nettyStatus(hexagonStatus))
        }

        check(InformationStatus.CONTINUE, CONTINUE)
        check(InformationStatus.SWITCHING_PROTOCOLS, SWITCHING_PROTOCOLS)
        check(InformationStatus.PROCESSING, PROCESSING)

        check(SuccessStatus.OK, OK)
        check(SuccessStatus.CREATED, CREATED)
        check(SuccessStatus.ACCEPTED, ACCEPTED)
        check(SuccessStatus.NON_AUTHORITATIVE_INFORMATION, NON_AUTHORITATIVE_INFORMATION)
        check(SuccessStatus.NO_CONTENT, NO_CONTENT)
        check(SuccessStatus.RESET_CONTENT, RESET_CONTENT)
        check(SuccessStatus.PARTIAL_CONTENT, PARTIAL_CONTENT)
        check(SuccessStatus.MULTI_STATUS, MULTI_STATUS)

        check(RedirectionStatus.MULTIPLE_CHOICES, MULTIPLE_CHOICES)
        check(RedirectionStatus.MOVED_PERMANENTLY, MOVED_PERMANENTLY)
        check(RedirectionStatus.FOUND, FOUND)
        check(RedirectionStatus.SEE_OTHER, SEE_OTHER)
        check(RedirectionStatus.NOT_MODIFIED, NOT_MODIFIED)
        check(RedirectionStatus.USE_PROXY, USE_PROXY)
        check(RedirectionStatus.TEMPORARY_REDIRECT, TEMPORARY_REDIRECT)
        check(RedirectionStatus.PERMANENT_REDIRECT, PERMANENT_REDIRECT)

        check(ClientErrorStatus.BAD_REQUEST, BAD_REQUEST)
        check(ClientErrorStatus.NOT_FOUND, NOT_FOUND)
        check(ClientErrorStatus.UNAUTHORIZED, UNAUTHORIZED)
        check(ClientErrorStatus.PAYMENT_REQUIRED, PAYMENT_REQUIRED)
        check(ClientErrorStatus.FORBIDDEN, FORBIDDEN)
        check(ClientErrorStatus.METHOD_NOT_ALLOWED, METHOD_NOT_ALLOWED)
        check(ClientErrorStatus.NOT_ACCEPTABLE, NOT_ACCEPTABLE)
        check(ClientErrorStatus.PROXY_AUTHENTICATION_REQUIRED, PROXY_AUTHENTICATION_REQUIRED)
        check(ClientErrorStatus.REQUEST_TIMEOUT, REQUEST_TIMEOUT)
        check(ClientErrorStatus.CONFLICT, CONFLICT)
        check(ClientErrorStatus.GONE, GONE)
        check(ClientErrorStatus.LENGTH_REQUIRED, LENGTH_REQUIRED)
        check(ClientErrorStatus.PRECONDITION_FAILED, PRECONDITION_FAILED)
        check(ClientErrorStatus.URI_TOO_LONG, REQUEST_URI_TOO_LONG)
        check(ClientErrorStatus.UNSUPPORTED_MEDIA_TYPE, UNSUPPORTED_MEDIA_TYPE)
        check(ClientErrorStatus.RANGE_NOT_SATISFIABLE, REQUESTED_RANGE_NOT_SATISFIABLE)
        check(ClientErrorStatus.EXPECTATION_FAILED, EXPECTATION_FAILED)
        check(ClientErrorStatus.MISDIRECTED_REQUEST, MISDIRECTED_REQUEST)
        check(ClientErrorStatus.UNPROCESSABLE_CONTENT, UNPROCESSABLE_ENTITY)
        check(ClientErrorStatus.LOCKED, LOCKED)
        check(ClientErrorStatus.FAILED_DEPENDENCY, FAILED_DEPENDENCY)
        check(ClientErrorStatus.UPGRADE_REQUIRED, UPGRADE_REQUIRED)
        check(ClientErrorStatus.PRECONDITION_REQUIRED, PRECONDITION_REQUIRED)
        check(ClientErrorStatus.TOO_MANY_REQUESTS, TOO_MANY_REQUESTS)
        check(ClientErrorStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, REQUEST_HEADER_FIELDS_TOO_LARGE)

        check(ServerErrorStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
        check(ServerErrorStatus.NOT_IMPLEMENTED, NOT_IMPLEMENTED)
        check(ServerErrorStatus.BAD_GATEWAY, BAD_GATEWAY)
        check(ServerErrorStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE)
        check(ServerErrorStatus.GATEWAY_TIMEOUT, GATEWAY_TIMEOUT)
        check(ServerErrorStatus.HTTP_VERSION_NOT_SUPPORTED, HTTP_VERSION_NOT_SUPPORTED)
        check(ServerErrorStatus.VARIANT_ALSO_NEGOTIATES, VARIANT_ALSO_NEGOTIATES)
        check(ServerErrorStatus.INSUFFICIENT_STORAGE, INSUFFICIENT_STORAGE)
        check(ServerErrorStatus.NOT_EXTENDED, NOT_EXTENDED)
        check(ServerErrorStatus.NETWORK_AUTHENTICATION_REQUIRED, NETWORK_AUTHENTICATION_REQUIRED)

        val status = CustomStatus(599)
        check(status, HttpResponseStatus(599, status.toString()))
    }
}
