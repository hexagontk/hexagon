package com.hexagonkt.http.test.examples

import com.hexagonkt.core.require
import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.formatQueryString
import com.hexagonkt.http.model.*
import com.hexagonkt.http.server.*
import com.hexagonkt.http.handlers.HttpCallbackType
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.handlers.path
import com.hexagonkt.http.test.BaseTest
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager
import org.junit.jupiter.api.*

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class ClientCookiesTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    private val serializationFormats: List<SerializationFormat>,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    private var callback: HttpCallbackType = { this }

    override val handler: HttpHandler = path {
        post("*") { callback() }
        get("*") { callback() }
        head("*") { callback() }
        put("*") { callback() }
        delete("*") { callback() }
        trace("*") { callback() }
        options("*") { callback() }
        patch("*") { callback() }
    }

    @BeforeAll fun setUpSerializationFormats() {
        SerializationManager.formats = serializationFormats.toSet()
    }

    @BeforeEach fun resetHandler() {
        callback = {
            val contentType = ContentType(APPLICATION_JSON, charset = Charsets.UTF_8)
            val bodyString = request.bodyString()
            val bodyHeader =
                if (bodyString.endsWith("\n") || bodyString.contains("{")) "json"
                else bodyString

            ok(
                body = bodyString,
                headers = response.headers
                    + Header("body", bodyHeader)
                    + Header("ct", request.contentType?.text ?: "")
                    + Header("query-parameters", formatQueryString(queryParameters)),
                contentType = contentType,
            )
        }
    }

    @Test fun `Cookies are sent correctly`() {
        callback = {
            val cookiesMap = request.cookiesMap()
            assertEquals(Cookie("c1", "v1"), cookiesMap["c1"])
            assertEquals(Cookie("c2", "v2", -1), cookiesMap["c2"])
            assertNull(cookiesMap["c3"]) // Secure headers only sent through HTTPS
            ok(cookies = listOf(
                Cookie("c4", "v4", 60),
                Cookie("c5", "v5"),
                Cookie("c6", "v6", secure = true),
            ))
        }

        client.cookies = emptyList()
        val response = client.send(
            HttpRequest(
                cookies = listOf(
                    Cookie("c1", "v1"),
                    Cookie("c2", "v2", 1),
                    Cookie("c3", "v3", secure = true),
                )
            )
        )

        listOf(response.cookiesMap(), client.cookiesMap()).forEach {
            val c4 = it.require("c4")
            assertEquals("v4", c4.value)
            assertTrue(c4.maxAge in 59..60)
            assertEquals(Cookie("c5", "v5"), it["c5"]?.copy(domain = null))
            assertNull(it["c6"])
        }
    }
}
