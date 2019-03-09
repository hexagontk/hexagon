package com.hexagonkt.http.server.examples

import com.hexagonkt.helpers.require
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.testng.annotations.*
import java.net.HttpCookie

@Test abstract class CookiesTest(adapter: ServerPort) {

    // cookies
    val server: Server by lazy {
        Server(adapter) {
            post("/assertNoCookies") {
                if (!request.cookies.isEmpty())
                    halt(500)
            }

            post("/addCookie") {
                val name = parameters["cookieName"]?.first()
                val value = parameters["cookieValue"]?.first()
                response.addCookie(HttpCookie(name, value))
            }

            post("/assertHasCookie") {
                val cookieName = parameters.require("cookieName").first()
                val cookieValue = request.cookies[cookieName]?.value
                if (parameters["cookieValue"]?.first() != cookieValue)
                    halt(500)
            }

            post("/removeCookie") {
                response.removeCookie(parameters.require("cookieName").first())
            }
        }
    }
    // cookies

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @BeforeMethod fun clearCookies() {
        client.cookies.clear()
    }

    @Test(priority = 1) fun emptyCookies() {
        assert (client.post("/assertNoCookies").statusCode == 200)
    }

    @Test(priority = 2) fun createCookie() {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"

        client.post("/addCookie?$cookie")
        val result = client.post("/assertHasCookie?$cookie")
        assert (client.cookies.size == 1)
        assert (result.statusCode == 200)
    }

    @Test(priority = 3) fun removeCookie() {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"
        client.post("/addCookie?$cookie")
        assert (client.cookies.size == 1)
        assert (client.post("/assertHasCookie?$cookie").statusCode == 200)
        client.post("/removeCookie?$cookie")
        val result = client.post("/assertNoCookies")
        assert (result.statusCode == 200)
    }
}
