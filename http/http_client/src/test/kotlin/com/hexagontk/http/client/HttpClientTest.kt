package com.hexagontk.http.client

import com.hexagontk.core.media.TEXT_CSV
import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.core.urlOf
import com.hexagontk.http.handlers.FilterHandler
import com.hexagontk.http.handlers.HttpPredicate
import com.hexagontk.http.model.HttpResponsePort
import com.hexagontk.http.model.*
import com.hexagontk.http.patterns.LiteralPathPattern
import java.lang.StringBuilder
import java.util.concurrent.Flow
import java.util.concurrent.Flow.Subscription
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class HttpClientTest {

    @Test fun `Default settings are created as expected`() {
        assertEquals(HttpClientSettings(), HttpClient(VoidHttpClient).settings)

        val noCookiesSettings = HttpClientSettings(useCookies = false)
        assertFalse(HttpClient(VoidHttpClient, noCookiesSettings).settings.useCookies)

        val base = "http://example.org"
        val baseUrl = urlOf(base)
        assertEquals(
            baseUrl,
            HttpClient(VoidHttpClient, HttpClientSettings(baseUrl)).settings.baseUrl
        )
        assertEquals(
            baseUrl,
            HttpClient(VoidHttpClient, noCookiesSettings.copy(baseUrl = baseUrl)).settings.baseUrl
        )
        assertFalse(
            HttpClient(VoidHttpClient, noCookiesSettings.copy(baseUrl = baseUrl)).settings.useCookies
        )

        val settingsBaseUrl = urlOf("http://server.com")
        val baseUrlSettings = HttpClientSettings(baseUrl = settingsBaseUrl)
        assertEquals(
            baseUrl,
            HttpClient(VoidHttpClient, baseUrlSettings.copy(baseUrl = baseUrl)).settings.baseUrl
        )
        assertEquals(settingsBaseUrl, HttpClient(VoidHttpClient, baseUrlSettings).settings.baseUrl)
    }

    @Test fun `Cookies map works ok`() {
        val client = HttpClient(VoidHttpClient)
        client.cookies += Cookie("cookie", "value")
        assertNull(client.cookiesMap()["name"])
        assertEquals(Cookie("cookie", "value"), client.cookiesMap()["cookie"])
    }

    @Test fun `HTTP Client is closeable`() {
        val client = HttpClient(VoidHttpClient)
        client.use {
            assertFalse(VoidHttpClient.started)
            it.start()
            assertTrue(VoidHttpClient.started)
        }
        assertFalse(VoidHttpClient.started)
    }

    @Test fun `Client helper methods work properly`() {
        fun HttpResponsePort.checkClient(
            path: String,
            body: String = "",
            headers: Headers = Headers(),
            contentType: ContentType? = null,
        ) {
            assertEquals(headers + Header("-path-", path), this.headers)
            assertEquals(body, this.bodyString())
            assertEquals(contentType, this.contentType)
        }

        val client = HttpClient(VoidHttpClient)
        val csv = ContentType(TEXT_CSV)
        val csvClient = HttpClient(VoidHttpClient, HttpClientSettings(contentType = csv))
        val headers = Headers(Header("h1", "v1"))
        val body = "body"

        client.request {
            get("/a").checkClient("/a")
            head("/a").checkClient("/a")
            post("/a").checkClient("/a")
            put("/a").checkClient("/a")
            delete("/a").checkClient("/a")
            trace("/a").checkClient("/a")
            options("/a").checkClient("/a")
            patch("/a").checkClient("/a")

            get().checkClient("")
            head().checkClient("")
            post().checkClient("")
            put().checkClient("")
            delete().checkClient("")
            trace().checkClient("")
            options().checkClient("")
            patch().checkClient("")

            get("/a", headers).checkClient("/a", headers = headers)
            head("/a", headers).checkClient("/a", headers = headers)
            options("/a", headers = headers).checkClient("/a", headers = headers)
            get("/a", body = body).checkClient("/a", body)
            options("/a", body).checkClient("/a", body)
            post("/a", body).checkClient("/a", body)
            put("/a", body).checkClient("/a", body)
            delete("/a", body).checkClient("/a", body)
            trace("/a", body).checkClient("/a", body)
            patch("/a", body).checkClient("/a", body)
            options("/a", body, headers).checkClient("/a", body, headers)
        }

        csvClient.request {
            get("/a").checkClient("/a", contentType = csv)
            head("/a").checkClient("/a", contentType = csv)
            post("/a").checkClient("/a", contentType = csv)
            put("/a").checkClient("/a", contentType = csv)
            delete("/a").checkClient("/a", contentType = csv)
            trace("/a").checkClient("/a", contentType = csv)
            options("/a").checkClient("/a", contentType = csv)
            patch("/a").checkClient("/a", contentType = csv)

            get("/a", headers).checkClient("/a", headers = headers, contentType = csv)
            head("/a", headers).checkClient("/a", headers = headers, contentType = csv)
            get("/a", body = body).checkClient("/a", body, contentType = csv)
            options("/a", body).checkClient("/a", body, contentType = csv)
            post("/a", body).checkClient("/a", body, contentType = csv)
            put("/a", body).checkClient("/a", body, contentType = csv)
            delete("/a", body).checkClient("/a", body, contentType = csv)
            trace("/a", body).checkClient("/a", body, contentType = csv)
            patch("/a", body).checkClient("/a", body, contentType = csv)
            options("/a", body, headers).checkClient("/a", body, headers, contentType = csv)
            options("/a", headers = headers).checkClient("/a", headers = headers, contentType = csv)
        }
    }

    @Test fun `Shut down not started client fails`() {
        val client = HttpClient(VoidHttpClient)
        val message = assertFailsWith<IllegalStateException> { client.stop() }.message
        assertEquals("HTTP client *MUST BE STARTED* before shut-down", message)
    }

    @Test fun `HTTP clients fails to start if already started`() {
        val client = HttpClient(VoidHttpClient)
        client.start()
        assert(client.started())
        val message = assertFailsWith<IllegalStateException> { client.start() }.message
        assertEquals("HTTP client is already started", message)
        client.stop()
    }

    @Test fun `Handlers filter requests and responses`() {
        val handler = FilterHandler {
            val next = receive(body = "p_" + request.bodyString()).next()
            next.send(body = next.request.bodyString() + next.response.bodyString() + "_s")
        }
        val client = HttpClient(VoidHttpClient, handler = handler)

        val e = assertFailsWith<IllegalStateException> { client.get("http://localhost") }
        assertEquals("HTTP client *MUST BE STARTED* before sending requests", e.message)

        client.request {
            assertEquals("p_p__s", client.post("/test").bodyString())
            assertEquals("p_bodyp_body_s", client.post("/test", "body").bodyString())
        }
    }

    @Test fun `Request is sent even if no handler`() {
        val handler = FilterHandler("/test") { error("Failure") }
        val client = HttpClient(VoidHttpClient, handler = handler)

        val e1 = assertFailsWith<IllegalStateException> { client.get("http://localhost") }
        assertEquals("HTTP client *MUST BE STARTED* before sending requests", e1.message)

        client.start()
        client.request {
            val e2 = assertFailsWith<IllegalStateException> { client.put("/test", "body") }
            assertEquals("Failure", e2.message)
            assertEquals("/good", client.put("/good", "body").headers["-path-"]?.string())
        }
    }

    @Test fun `Request is sent with accept and authorization headers`() {
        val handler = FilterHandler {
            assertEquals(TEXT_PLAIN, request.accept.first().mediaType)
            assertEquals("basic", request.authorization?.type)
            assertEquals("abc", request.authorization?.value)
            next()
        }
        val client = HttpClient(VoidHttpClient, handler = handler)

        client.request {
            val accept = listOf(ContentType(TEXT_PLAIN))
            val authorization = Authorization("basic", "abc")
            client.send(HttpRequest(accept = accept, authorization = authorization))
        }
    }

    @Test fun `SSE requests work properly`() {
        val client = HttpClient(VoidHttpClient)

        val e = assertFailsWith<IllegalStateException> { client.sse("http://localhost") }
        assertEquals("HTTP client *MUST BE STARTED* before sending requests", e.message)

        client.request {
            val publisher = sse("http://example.org")
            publisher.subscribe(object : Flow.Subscriber<ServerEvent> {
                override fun onComplete() {}
                override fun onError(throwable: Throwable) {}

                override fun onNext(item: ServerEvent) {
                    assertEquals(ServerEvent("event", "data", "id", 1), item)
                }

                override fun onSubscribe(subscription: Subscription) {
                    subscription.request(Long.MAX_VALUE)
                }
            })
            VoidHttpClient.eventPublisher.submit(ServerEvent("event", "data", "id", 1))
        }
    }

    @Test fun `WebSockets requests work properly`() {
        val client = HttpClient(VoidHttpClient)

        val e = assertFailsWith<IllegalStateException> { client.ws("http://localhost") }
        assertEquals("HTTP client *MUST BE STARTED* before connecting to WS", e.message)

        client.request {
            val data = StringBuilder()
            val session = ws("http://example.org", onText = { data.append(it) })
            assertEquals("", data.toString())
            session.send("text")
            assertEquals("text", data.toString())
        }
    }
}
