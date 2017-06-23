package co.there4.hexagon.server.integration

import co.there4.hexagon.server.Call
import co.there4.hexagon.server.Router
import co.there4.hexagon.server.ServerEngine
import co.there4.hexagon.server.post
import java.net.HttpCookie

@Suppress("unused") // Test methods are flagged as unused
class CookiesIT(serverEngine: ServerEngine) : ItModule(serverEngine) {
    override fun Router.initialize() {
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

    fun emptyCookies() {
        assert (client.post("/assertNoCookies").statusCode == 200)
    }

    fun createCookie() {
        val cookieName = "testCookie"
        val cookieValue = "testCookieValue"
        val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"

        client.post("/setCookie?$cookie")
        val result = client.post("/assertHasCookie?$cookie")
        assert (client.cookies.size == 1)
        assert (result.statusCode == 200)
    }

    fun removeCookie() {
            val cookieName = "testCookie"
            val cookieValue = "testCookieValue"
            val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"
            post("/setCookie?$cookie")
            post("/removeCookie?$cookie")
            val result = client.post("/assertNoCookies")
            assert (result.statusCode == 200)
    }

    private fun Call.checkCookie(cookieName: String?) {
        val cookieValue = request.cookies[cookieName]?.value
        if (request["cookieValue"] != cookieValue)
            halt(500)
    }

    override fun validate() {
        emptyCookies()
        createCookie()
        removeCookie()
    }
}
