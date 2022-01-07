package com.hexagonkt.http.test.examples

import com.hexagonkt.core.helpers.require
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.HttpCookie
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import kotlin.test.assertEquals

@TestMethodOrder(OrderAnnotation::class)
@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class CookiesTest(
    override val clientAdapter: () -> HttpClientPort,
    override val serverAdapter: () -> HttpServerPort
) : BaseTest() {

    // cookies
    private val handler: PathHandler = path {
        post("/assertNoCookies") {
            if (request.cookies.isNotEmpty()) internalServerError()
            else ok()
        }

        post("/addCookie") {
            val name = request.queryParameters.require("cookieName")
            val value = request.queryParameters.require("cookieValue")
            ok(cookies = response.cookies + HttpCookie(name, value))
        }

        post("/assertHasCookie") {
            val cookieName = request.queryParameters.require("cookieName")
            val cookieValue = request.cookiesMap()[cookieName]?.value
            if (request.queryParameters["cookieValue"] != cookieValue) internalServerError()
            else ok()
        }

        post("/removeCookie") {
            val cookie = request.cookiesMap().require(request.queryParameters.require("cookieName"))
            ok(cookies = response.cookies + cookie.delete())
        }
    }
    // cookies

    override val handlers: List<ServerHandler> = listOf(handler)

    @BeforeEach fun clearCookies() {
        client.cookies = emptyList()
    }

    @Test
    @Order(1)
    fun `Empty cookies assures there is no cookies`() = runBlocking {
        assertEquals(OK, client.post("/assertNoCookies").status)
    }

    @Test
    @Order(2)
    fun `Create cookie adds a new cookie to the request`() = runBlocking {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"

        client.post("/addCookie?$cookie")
        val result = client.post("/assertHasCookie?$cookie")
        assertEquals(1, client.cookies.size)
        assertEquals(OK, result.status)
    }

    @Test
    @Order(3)
    fun `Remove cookie deletes the given cookie`() = runBlocking {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"
        client.post("/addCookie?$cookie")
        assertEquals(1, client.cookies.size)
        assertEquals(OK, client.post("/assertHasCookie?$cookie").status)
        client.post("/removeCookie?$cookie")
        val result = client.post("/assertNoCookies")
        assertEquals(OK, result.status)
    }

    @Test
    @Order(4)
    fun `Remove not available cookie does not fail`() = runBlocking {
        val cookieName = "unknownCookie"
        client.post("/removeCookie?$cookieName")
        assert(client.cookies.isEmpty())
        val result = client.post("/assertNoCookies")
        assertEquals(OK, result.status)
    }

    @Test
    @Order(5)
    fun `Full cookie lifecycle`() = runBlocking {
        client.cookies = emptyList()
        assert(client.cookies.isEmpty())

        // clientCookies
        val cookieName = "sampleCookie"
        val cookieValue = "sampleCookieValue"

        // Set the cookie in the client
        client.cookies = client.cookies + HttpCookie(cookieName, cookieValue)

        // Assert that it is received in the server and change its value afterwards
        client.post("/assertHasCookie?cookieName=$cookieName")
        client.post("/addCookie?cookieName=$cookieName&cookieValue=${cookieValue}_changed")

        // Verify that the client cookie is updated
        assertEquals(cookieValue + "_changed", client.cookiesMap()[cookieName]?.value)
        // clientCookies
    }
}
