package com.hexagonkt.http.test

import com.hexagonkt.core.helpers.encodeToBase64
import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.logging.slf4j.jul.Slf4jJulLoggingAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URL
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
abstract class BaseTest {

    protected abstract val clientAdapter: () -> HttpClientPort
    protected abstract val serverAdapter: () -> HttpServerPort
    protected abstract val handlers: List<ServerHandler>

    protected val server: HttpServer by lazy {
        HttpServer(serverAdapter(), handlers)
    }

    protected val client: HttpClient by lazy {
        HttpClient(clientAdapter(), URL("http://localhost:${server.runtimePort}"))
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
        response: HttpClientResponse?, status: HttpStatus, vararg content: String) {

        assertEquals(status, response?.status)
        val payload = response?.body?.let { b -> b as String }
        content.forEach { assert(payload?.contains(it) ?: false) }
    }

    protected fun assertResponseContains(response: HttpClientResponse?, vararg content: String) {
        assertResponseContains(response, OK, *content)
    }

    // TODO Move to `http` module to share basic and digest auth among client and server
    protected fun basicAuth(user: String, password: String? = null): String =
        "Basic " + "$user:$password".encodeToBase64()

    protected fun assertResponseEquals(
        response: HttpClientResponse?, status: HttpStatus = OK, content: String) {

        assertEquals(status, response?.status)
        assertEquals(content, response?.body?.let { it as String }?.trim())
    }
}
