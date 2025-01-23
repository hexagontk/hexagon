package com.hexagontk.http.test

import com.hexagontk.core.urlOf
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.model.HttpResponsePort
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.HttpHandler
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.logging.LogManager
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
abstract class BaseTest {

    protected abstract val clientAdapter: () -> HttpClientPort
    protected abstract val serverAdapter: () -> HttpServerPort
    protected abstract val serverSettings: HttpServerSettings
    protected abstract val handler: HttpHandler

    protected fun server(): HttpServer =
        HttpServer(serverAdapter(), handler, serverSettings.with(bindPort = 0)).apply { start() }

    protected fun client(server: HttpServer): HttpClient =
        HttpClient(clientAdapter(), HttpClientSettings(server.binding)).apply { start() }

    @BeforeAll fun startUp() {
        val configuration = urlOf("classpath:logging.properties")
        LogManager.getLogManager().readConfiguration(configuration.openStream())
    }

    protected fun assertResponseContains(
        response: HttpResponsePort?, status: Int, vararg content: String) {

        assertEquals(status, response?.status)
        val payload = response?.body?.let { b -> b as String }
        content.forEach { assert(payload?.contains(it) == true) }
    }

    protected fun assertResponseContains(
        response: HttpResponsePort?, vararg content: String
    ) {
        assertResponseContains(response, OK_200, *content)
    }

    protected fun assertResponseEquals(
        response: HttpResponsePort?, status: Int = OK_200, content: String
    ) {
        assertEquals(status, response?.status)
        assertEquals(content, response?.body?.let { it as String }?.trim())
    }
}
