package com.hexagonkt.http.server.handlers

import com.hexagonkt.handlers.Context
import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpProtocol.HTTP
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerRequestPort
import com.hexagonkt.http.server.model.HttpServerResponse
import java.security.cert.X509Certificate

sealed interface HttpHandler : Handler<HttpServerCall> {
    val serverPredicate: HttpServerPredicate

    fun addPrefix(prefix: String): HttpHandler

    fun process(request: HttpServerRequestPort): HttpServerResponse =
        Context(HttpServerCall(request = request), predicate).let { context ->
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
