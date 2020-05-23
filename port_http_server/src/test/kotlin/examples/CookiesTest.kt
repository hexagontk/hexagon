package com.hexagonkt.http.server.examples

import com.hexagonkt.helpers.require
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.HttpCookie

@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
abstract class CookiesTest(adapter: ServerPort) {

    // cookies
    val server: Server = Server(adapter) {
        post("/assertNoCookies") {
            if (request.cookies.isNotEmpty())
                halt(500)
        }

        post("/addCookie") {
            val name = queryParameters["cookieName"]
            val value = queryParameters["cookieValue"]
            response.addCookie(HttpCookie(name, value))
        }

        post("/assertHasCookie") {
            val cookieName = queryParameters.require("cookieName")
            val cookieValue = request.cookies[cookieName]?.value
            if (queryParameters["cookieValue"] != cookieValue)
                halt(500)
        }

        post("/removeCookie") {
            response.removeCookie(queryParameters.require("cookieName"))
        }
    }
    // cookies

    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

    @BeforeAll fun initialize() {
        server.start()
    }

    @AfterAll fun shutdown() {
        server.stop()
    }

    @BeforeEach fun clearCookies() {
        client.cookies.clear()
    }

    @Test
    @Order(1)
    fun `Empty cookies assures there is no cookies`() {
        assert(client.post("/assertNoCookies").status == 200)
    }

    @Test
    @Order(2)
    fun `Create cookie adds a new cookie to the request`() {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"

        client.post("/addCookie?$cookie")
        val result = client.post("/assertHasCookie?$cookie")
        assert(client.cookies.size == 1)
        assert(result.status == 200)
    }

    @Test
    @Order(3)
    fun `Remove cookie deletes the given cookie`() {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"
        client.post("/addCookie?$cookie")
        assert(client.cookies.size == 1)
        assert(client.post("/assertHasCookie?$cookie").status == 200)
        client.post("/removeCookie?$cookie")
        val result = client.post("/assertNoCookies")
        assert(result.status == 200)
    }

    @Test
    @Order(4)
    fun `Remove not available cookie does not fail`() {
        val cookieName = "unknownCookie"
        client.post("/removeCookie?$cookieName")
        assert(client.cookies.isEmpty())
        val result = client.post("/assertNoCookies")
        assert(result.status == 200)
    }
}
