package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.core.toText
import com.hexagonkt.handlers.Context
import com.hexagonkt.handlers.EventContext
import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpProtocol.HTTP
import com.hexagonkt.http.model.HttpStatusType.SERVER_ERROR
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerRequestPort
import com.hexagonkt.http.server.model.HttpServerResponse
import java.security.cert.X509Certificate

sealed interface HttpHandler : Handler<HttpServerCall> {
    val serverPredicate: HttpServerPredicate

    fun addPrefix(prefix: String): HttpHandler

    fun byMethod(): Map<HttpMethod, HttpHandler> =
        serverPredicate.methods.associateWith { filter(it) }

    fun filter(method: HttpMethod): HttpHandler =
        when (this) {
            is PathHandler ->
                copy(
                    serverPredicate = serverPredicate.clearMethods(),
                    handlers = handlers
                        .filter {
                            val methods = it.serverPredicate.methods
                            method in methods || methods.isEmpty()
                        }
                        .map { it.filter(method) }
                )

            is OnHandler ->
                copy(serverPredicate = serverPredicate.clearMethods())

            is FilterHandler ->
                copy(serverPredicate = serverPredicate.clearMethods())

            is AfterHandler ->
                copy(serverPredicate = serverPredicate.clearMethods())
        }

    fun processContext(request: HttpServerRequestPort): Context<HttpServerCall> =
        process(EventContext(HttpServerCall(request = request), predicate)).let {
            val event = it.event
            val response = event.response
            val exception = it.exception

            if (exception != null && response.status.type != SERVER_ERROR)
                it.with(
                    event = event.copy(
                        response = response.copy(
                            body = exception.toText(),
                            contentType = ContentType(TEXT_PLAIN),
                            status = INTERNAL_SERVER_ERROR_500,
                        )
                    )
                )
            else it
        }

    fun process(request: HttpServerRequestPort): HttpServerResponse =
        EventContext(HttpServerCall(request = request), predicate).let { context ->
            if (serverPredicate(context)) process(context).event.response
            else context.event.response
        }

    fun process(
        method: HttpMethod = GET,
        protocol: HttpProtocol = HTTP,
        host: String = "localhost",
        port: Int = 80,
        path: String = "",
        queryParameters: QueryParameters = QueryParameters(),
        headers: Headers = Headers(),
        body: Any = "",
        parts: List<HttpPart> = emptyList(),
        formParameters: FormParameters = FormParameters(),
        cookies: List<Cookie> = emptyList(),
        contentType: ContentType? = null,
        certificateChain: List<X509Certificate> = emptyList(),
        accept: List<ContentType> = emptyList(),
        contentLength: Long = -1L,
    ): HttpServerResponse =
        process(
            HttpServerRequest(
                method,
                protocol,
                host,
                port,
                path,
                queryParameters,
                headers,
                body,
                parts,
                formParameters,
                cookies,
                contentType,
                certificateChain,
                accept,
                contentLength,
            )
        )
}
