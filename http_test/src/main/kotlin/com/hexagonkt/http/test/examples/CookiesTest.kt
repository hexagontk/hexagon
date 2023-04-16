package com.hexagonkt.http.test.examples

import com.hexagonkt.core.require
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.Cookie
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.handlers.PathHandler
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.handlers.path
import com.hexagonkt.http.test.BaseTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import kotlin.test.assertEquals

@TestMethodOrder(OrderAnnotation::class)
@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class CookiesTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    // cookies
    private val path: PathHandler = path {
        post("/assertNoCookies") {
            if (request.cookies.isNotEmpty()) internalServerError()
            else ok()
        }

        post("/addCookie") {
            val name = queryParameters.require("cookieName").value ?: return@post badRequest("No cookie name")
            val value = queryParameters.require("cookieValue").value ?: return@post badRequest("No cookie value")
            ok(cookies = response.cookies + Cookie(name, value))
        }

        post("/assertHasCookie") {
            val cookieName = queryParameters.require("cookieName").value ?: return@post badRequest("No cookie name")
            val cookieValue = request.cookiesMap()[cookieName]?.value ?: return@post badRequest("No cookie value")
            if (queryParameters["cookieValue"]?.value != cookieValue) internalServerError()
            else ok()
        }

        post("/removeCookie") {
            val cookie = request.cookiesMap()[queryParameters.require("cookieName").value]
            if (cookie == null) ok()
            else ok(cookies = response.cookies + cookie.delete())
        }
    }
    // cookies

    override val handler: HttpHandler = path

    @BeforeEach fun clearCookies() {
        client.cookies = emptyList()
    }

    @Test
    @Order(1)
    fun `Empty cookies assures there is no cookies`() {
        assertEquals(OK_200, client.post("/assertNoCookies").status)
    }

    @Test
    @Order(2)
    fun `Create cookie adds a new cookie to the request`() {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"

        client.post("/addCookie?$cookie")
        val result = client.post("/assertHasCookie?$cookie")
        assertEquals(1, client.cookies.size)
        assertEquals(OK_200, result.status)
    }

    @Test
    @Order(3)
    fun `Remove cookie deletes the given cookie`() {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"
        client.post("/addCookie?$cookie")
        assertEquals(1, client.cookies.size)
        assertEquals(OK_200, client.post("/assertHasCookie?$cookie").status)
        client.post("/removeCookie?$cookie")
        val result = client.post("/assertNoCookies")
        assertEquals(OK_200, result.status)
    }

    @Test
    @Order(4)
    fun `Remove not available cookie does not fail`() {
        val cookieName = "unknownCookie"
        client.post("/removeCookie?cookieName=$cookieName")
        assert(client.cookies.isEmpty())
        val result = client.post("/assertNoCookies")
        assertEquals(OK_200, result.status)
    }

    @Test
    @Order(5)
    fun `Full cookie lifecycle`() {
        client.cookies = emptyList()
        assert(client.cookies.isEmpty())

        // clientCookies
        val cookieName = "sampleCookie"
        val cookieValue = "sampleCookieValue"

        // Set the cookie in the client
        client.cookies = client.cookies + Cookie(cookieName, cookieValue)

        // Assert that it is received in the server and change its value afterwards
        client.post("/assertHasCookie?cookieName=$cookieName")
        client.post("/addCookie?cookieName=$cookieName&cookieValue=${cookieValue}_changed")

        // Verify that the client cookie is updated
        assertEquals(cookieValue + "_changed", client.cookiesMap()[cookieName]?.value)

        // The cookie is persisted along calls
        client.post("/assertHasCookie?cookieName=$cookieName")
        assertEquals(cookieValue + "_changed", client.cookiesMap()[cookieName]?.value)
        // clientCookies
    }
}
