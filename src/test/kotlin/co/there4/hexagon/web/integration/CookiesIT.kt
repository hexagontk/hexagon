package co.there4.hexagon.web.integration

import co.there4.hexagon.web.Exchange
import co.there4.hexagon.web.Server
import java.net.HttpCookie

@Suppress("unused") // Test methods are flagged as unused
class CookiesIT : ItTest () {
    override fun initialize(srv: Server) {
        srv.post("/assertNoCookies") {
            if (!request.cookies.isEmpty())
                halt(500)
        }

        srv.post("/setCookie") {
            val name = request ["cookieName"]
            val value = request ["cookieValue"]
            response.addCookie (HttpCookie (name, value))
        }

        srv.post("/assertHasCookie") {
            checkCookie(request ["cookieName"])
        }

        srv.post("/removeCookie") {
            val cookieName = request.parameter("cookieName")
            checkCookie(cookieName)
            response.removeCookie(cookieName)
        }
    }

    fun emptyCookies() {
        withClients {
            assert (post("/assertNoCookies").statusCode == 200)
        }
    }

    fun createCookie() {
        withClients {
            val cookieName = "testCookie"
            val cookieValue = "testCookieValue"
            val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"

            post("/setCookie?$cookie")
            val result = post("/assertHasCookie?$cookie")
            assert (this.cookies.size == 1)
            assert (result.statusCode == 200)
        }
    }

    fun removeCookie() {
        withClients {
            val cookieName = "testCookie"
            val cookieValue = "testCookieValue"
            val cookie = "cookieName=$cookieName&cookieValue=$cookieValue"
            post("/setCookie?$cookie")
            post("/removeCookie?$cookie")
            val result = post("/assertNoCookies")
            assert (result.statusCode == 200)
        }
    }

    private fun Exchange.checkCookie(cookieName: String?) {
        val cookieValue = request.cookies[cookieName]?.value
        if (request["cookieValue"] != cookieValue)
            halt(500)
    }
}
