package com.hexagonkt.server

import com.hexagonkt.http.client.Client
import java.net.HttpCookie

@Suppress("unused", "MemberVisibilityCanPrivate") // Test methods are flagged as unused
internal class CookiesModule : TestModule() {
    override fun initialize(): Router = router {
        post("/assertNoCookies") {
            if (!request.cookies.isEmpty())
                halt(500)
        }

        post("/setCookie") {
            val name = request ["cookieName"]
            val value = request ["cookieValue"]
            response.addCookie (HttpCookie (name, value))
        }

        post("/assertHasCookie") {
            checkCookie(request ["cookieName"])
        }

        post("/removeCookie") {
            val cookieName = request.parameter("cookieName")
            checkCookie(cookieName)
            response.removeCookie(cookieName)
        }
    }

    fun emptyCookies(client: Client) {
        assert (client.post("/assertNoCookies").statusCode == 200)
    }

    fun createCookie(client: Client) {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"

        client.post("/setCookie?$cookie")
        val result = client.post("/assertHasCookie?$cookie")
        assert (client.cookies.size == 1)
        assert (result.statusCode == 200)
    }

    fun removeCookie(client: Client) {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"
        client.post("/setCookie?$cookie")
        assert (client.cookies.size == 1)
        assert (client.post("/assertHasCookie?$cookie").statusCode == 200)
        client.post("/removeCookie?$cookie")
        val result = client.post("/assertNoCookies")
        assert (result.statusCode == 200)
    }

    private fun Call.checkCookie(cookieName: String?) {
        val cookieValue = request.cookies[cookieName]?.value
        if (request["cookieValue"] != cookieValue)
            halt(500)
    }

    override fun validate(client: Client) {
        client.cookies.clear()
        emptyCookies(client)
        client.cookies.clear()
        createCookie(client)
        client.cookies.clear()
        removeCookie(client)
    }
}
