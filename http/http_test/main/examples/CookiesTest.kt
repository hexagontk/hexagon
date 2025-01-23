package com.hexagontk.http.test.examples

import com.hexagontk.core.require
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.model.Cookie
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.model.CookieSameSite.*
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import java.time.Instant
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
            val name = queryParameters.require("cookieName").text
            val value = queryParameters.require("cookieValue").text
            val maxAge = queryParameters["maxAge"]?.text
            val secure = queryParameters["secure"]?.text
            val cookiePath = queryParameters["path"]?.text
            val httpOnly = queryParameters["httpOnly"]?.text
            val domain = queryParameters["domain"]?.text
            val sameSite = queryParameters["sameSite"]?.text
            val expires = queryParameters["expires"]?.text

            ok(
                cookies = response.cookies + Cookie(
                    name,
                    value,
                    maxAge?.toLong() ?: -1,
                    secure?.toBooleanStrict() ?: false,
                    cookiePath ?: "/",
                    httpOnly?.toBooleanStrict() ?: true,
                    domain ?: "",
                    sameSite?.let(::valueOf),
                    expires?.let(Instant::parse),
                )
            )
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

    private val server: HttpServer by lazy { server() }
    private val client: HttpClient by lazy { client(server) }

    @BeforeEach fun clearCookies() {
        client.cookies = emptyList()
    }

    @AfterAll fun stop() {
        client.stop()
        server.stop()
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
        client.cookies += Cookie(cookieName, cookieValue)

        // Assert that it is received in the server and change its value afterward
        client.post("/assertHasCookie?cookieName=$cookieName")
        client.post("/addCookie?cookieName=$cookieName&cookieValue=${cookieValue}_changed")

        // Verify that the client cookie is updated
        assertEquals(cookieValue + "_changed", client.cookiesMap()[cookieName]?.value)

        // The cookie is persisted along calls
        client.post("/assertHasCookie?cookieName=$cookieName")
        assertEquals(cookieValue + "_changed", client.cookiesMap()[cookieName]?.value)
        // clientCookies
    }

    @Test
    @Order(6)
    fun `Cookies contain correct values`() {
        client.cookies = emptyList()
        assert(client.cookies.isEmpty())

        val c = mapOf(
            "cookieName" to "fullCookie",
            "cookieValue" to "val",
            "maxAge" to 50,
            "path" to "/cook",
            "httpOnly" to true,
        ).entries.joinToString("&") { (k, v) -> "$k=$v" }

        client.post("/addCookie?cookieName=cookieName&cookieValue=val")
        client.post("/addCookie?$c")

        // Verify that the client cookie is updated
        val cm = client.cookiesMap()
        assertEquals("val", cm["cookieName"]?.value)
        assertEquals("val", cm["fullCookie"]?.value)
        assert(cm.require("fullCookie").maxAge in 45..50)
        assertEquals("/cook", cm["fullCookie"]?.path)
        assertTrue(cm.require("fullCookie").httpOnly)

        // The cookie is persisted along calls
        client.post("/assertHasCookie?cookieName=cookieName")
        client.post("/assertHasCookie?cookieName=fullCookie")

        assertEquals("val", cm["cookieName"]?.value)
    }
}
