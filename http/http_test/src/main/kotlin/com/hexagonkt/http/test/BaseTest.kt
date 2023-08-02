package com.hexagonkt.http.test

import com.hexagonkt.core.encodeToBase64
import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.logging.slf4j.jul.Slf4jJulLoggingAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
abstract class BaseTest {

    protected abstract val clientAdapter: () -> HttpClientPort
    protected abstract val serverAdapter: () -> HttpServerPort
    protected abstract val serverSettings: HttpServerSettings
    protected abstract val handler: HttpHandler

    protected val server: HttpServer by lazy {
        HttpServer(serverAdapter(), handler, serverSettings)
    }

    protected val client: HttpClient by lazy {
        val settings = HttpClientSettings(urlOf("http://localhost:${server.runtimePort}"))
        HttpClient(clientAdapter(), settings)
    }

    @BeforeAll fun startUp() {
        LoggingManager.adapter = Slf4jJulLoggingAdapter()
        LoggingManager.setLoggerLevel("com.hexagonkt", DEBUG)
        server.start()
        client.start()
    }

    @AfterAll fun shutDown() {
        client.stop()
        server.stop()
        LoggingManager.setLoggerLevel("com.hexagonkt", OFF)
    }

    protected fun assertResponseContains(
        response: HttpResponsePort?, status: HttpStatus, vararg content: String) {

        assertEquals(status, response?.status)
        val payload = response?.body?.let { b -> b as String }
        content.forEach { assert(payload?.contains(it) ?: false) }
    }

    protected fun assertResponseContains(
        response: HttpResponsePort?, vararg content: String
    ) {
        assertResponseContains(response, OK_200, *content)
    }

    // TODO Move to `http` module to share basic and digest auth among client and server
    protected fun basicAuth(user: String, password: String? = null): String =
        "Basic " + "$user:$password".encodeToBase64()

    protected fun assertResponseEquals(
        response: HttpResponsePort?, status: HttpStatus = OK_200, content: String
    ) {
        assertEquals(status, response?.status)
        assertEquals(content, response?.body?.let { it as String }?.trim())
    }
}
