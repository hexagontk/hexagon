package com.hexagonkt.http.client

import com.hexagonkt.core.MultiMap
import com.hexagonkt.core.multiMapOf
import com.hexagonkt.core.media.TextMedia.CSV
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpCookie
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class HttpClientTest {

    @Test fun `Default settings are created as expected`() {
        assertEquals(HttpClientSettings(), HttpClient(VoidAdapter).settings)

        val noCookiesSettings = HttpClientSettings(useCookies = false)
        assertFalse(HttpClient(VoidAdapter, noCookiesSettings).settings.useCookies)

        val baseUrl = URL("http://example.org")
        assertEquals(baseUrl, HttpClient(VoidAdapter, baseUrl).settings.baseUrl)
        assertEquals(baseUrl, HttpClient(VoidAdapter, baseUrl, noCookiesSettings).settings.baseUrl)
        assertFalse(HttpClient(VoidAdapter, baseUrl, noCookiesSettings).settings.useCookies)

        val settingsBaseUrl = URL("http://server.com")
        val baseUrlSettings = HttpClientSettings(baseUrl = settingsBaseUrl)
        assertEquals(baseUrl, HttpClient(VoidAdapter, baseUrl, baseUrlSettings).settings.baseUrl)
        assertEquals(settingsBaseUrl, HttpClient(VoidAdapter, baseUrlSettings).settings.baseUrl)
    }

    @Test fun `Cookies map works ok`() {
        val client = HttpClient(VoidAdapter)
        client.cookies += HttpCookie("cookie", "value")
        assertNull(client.cookiesMap()["name"])
        assertEquals(HttpCookie("cookie", "value"), client.cookiesMap()["cookie"])
    }

    @Test fun `HTTP Client is closeable`() {
        val client = HttpClient(VoidAdapter)
        client.use {
            assertFalse(VoidAdapter.started)
            it.start()
            assertTrue(VoidAdapter.started)
        }
        assertFalse(VoidAdapter.started)
    }

    @Test fun `Client helper methods work properly`() {
        fun HttpClientResponse.checkClient(
            path: String,
            body: String = "",
            headers: MultiMap<String, String> = multiMapOf(),
            contentType: ContentType? = null,
        ) {
            assertEquals(headers + ("-path-" to path), this.headers)
            assertEquals(body, this.bodyString())
            assertEquals(contentType, this.contentType)
        }

        val client = HttpClient(VoidAdapter)
        val csv = ContentType(CSV)
        val csvClient = HttpClient(VoidAdapter, HttpClientSettings(contentType = csv))
        val headers = multiMapOf("h1" to "v1")
        val body = "body"

        client.get("/a").checkClient("/a")
        client.head("/a").checkClient("/a")
        client.post("/a").checkClient("/a")
        client.put("/a").checkClient("/a")
        client.delete("/a").checkClient("/a")
        client.trace("/a").checkClient("/a")
        client.options("/a").checkClient("/a")
        client.patch("/a").checkClient("/a")

        client.get("/a", headers).checkClient("/a", headers = headers)
        client.head("/a", headers).checkClient("/a", headers = headers)
        client.options("/a", headers = headers).checkClient("/a", headers = headers)
        client.get("/a", body = body).checkClient("/a", body)
        client.options("/a", body).checkClient("/a", body)
        client.post("/a", body).checkClient("/a", body)
        client.put("/a", body).checkClient("/a", body)
        client.delete("/a", body).checkClient("/a", body)
        client.trace("/a", body).checkClient("/a", body)
        client.patch("/a", body).checkClient("/a", body)
        client.options("/a", body, headers).checkClient("/a", body, headers)

        csvClient.get("/a").checkClient("/a", contentType = csv)
        csvClient.head("/a").checkClient("/a")
        csvClient.post("/a").checkClient("/a", contentType = csv)
        csvClient.put("/a").checkClient("/a", contentType = csv)
        csvClient.delete("/a").checkClient("/a", contentType = csv)
        csvClient.trace("/a").checkClient("/a", contentType = csv)
        csvClient.options("/a").checkClient("/a", contentType = csv)
        csvClient.patch("/a").checkClient("/a", contentType = csv)

        csvClient.get("/a", headers).checkClient("/a", headers = headers, contentType = csv)
        csvClient.head("/a", headers).checkClient("/a", headers = headers)
        csvClient.get("/a", body = body).checkClient("/a", body, contentType = csv)
        csvClient.options("/a", body).checkClient("/a", body, contentType = csv)
        csvClient.post("/a", body).checkClient("/a", body, contentType = csv)
        csvClient.put("/a", body).checkClient("/a", body, contentType = csv)
        csvClient.delete("/a", body).checkClient("/a", body, contentType = csv)
        csvClient.trace("/a", body).checkClient("/a", body, contentType = csv)
        csvClient.patch("/a", body).checkClient("/a", body, contentType = csv)
        csvClient.options("/a", body, headers).checkClient("/a", body, headers, contentType = csv)
        csvClient.options("/a", headers = headers)
            .checkClient("/a", headers = headers, contentType = csv)
    }
}
