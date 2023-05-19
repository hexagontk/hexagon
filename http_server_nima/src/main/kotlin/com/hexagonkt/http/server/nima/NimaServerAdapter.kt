package com.hexagonkt.http.server.nima

import com.hexagonkt.core.fieldsMapOf
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.toText
import com.hexagonkt.http.bodyToBytes
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.*
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerFeature
import com.hexagonkt.http.server.HttpServerFeature.WEB_SOCKETS
import com.hexagonkt.http.server.HttpServerFeature.ZIP
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.logging.jul.JulLoggingAdapter
import io.helidon.common.http.Http.*
import io.helidon.common.http.SetCookie
import io.helidon.nima.webserver.WebServer
import io.helidon.nima.webserver.http.ServerResponse

/**
 * Implements [HttpServerPort] using Helidon Nima.
 */
class NimaServerAdapter : HttpServerPort {

    private var nimaServer: WebServer? = null

    override fun runtimePort(): Int =
        nimaServer?.port() ?: error("")

    override fun started() =
        nimaServer?.isRunning ?: false

    override fun startUp(server: HttpServer) {
        LoggingManager.adapter = JulLoggingAdapter()
        val settings = server.settings

        val handlers: Map<Method, HttpHandler> =
            server.handler.addPrefix(settings.contextPath)
                .byMethod()
                .mapKeys { Method.create(it.key.toString()) }

        @Suppress("MoveLambdaOutsideParentheses") // TODO Delete after upgrading Nima
        nimaServer = WebServer
            .builder()
            .host(settings.bindAddress.hostName)
            .port(settings.bindPort)
            .routing {
                it.any({ nimaRequest, nimaResponse ->
                    val method = nimaRequest.prologue().method()
                    val request = NimaRequestAdapter(method, nimaRequest)
                    val response = handlers[method]?.process(request)?.response ?: HttpResponse()
                    setResponse(response, nimaResponse)
                })
            }
            .build()

        nimaServer?.start() ?: error("")
    }

    override fun shutDown() {
        nimaServer?.stop() ?: error("")
    }

    override fun supportedProtocols(): Set<HttpProtocol> =
        setOf(HTTP, HTTPS, HTTP2)

    override fun supportedFeatures(): Set<HttpServerFeature> =
        setOf(ZIP, WEB_SOCKETS)

    override fun options(): Map<String, *> =
        fieldsMapOf<NimaServerAdapter>()

    private fun setResponse(response: HttpResponsePort, nimaResponse: ServerResponse) {
        try {
            nimaResponse.status(Status.create(response.status.code))

            response.headers.values.forEach { h ->
                nimaResponse.header(Header.create(Header.create(h.name), h.values))
            }

            response.cookies.forEach { c ->
                val create = SetCookie.create(c.name, c.value)
                nimaResponse.headers().addCookie(create)
            }

            response.contentType?.let { ct ->
                nimaResponse.header(Header.create(Header.CONTENT_TYPE, ct.text))
            }

            nimaResponse.send(bodyToBytes(response.body))
        }
        catch (e: Exception) {
            nimaResponse.status(Status.INTERNAL_SERVER_ERROR_500)
            nimaResponse.send(bodyToBytes(e.toText()))
        }
    }
}
