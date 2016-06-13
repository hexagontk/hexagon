package co.there4.hexagon.web.integration

import co.there4.hexagon.web.Client
import co.there4.hexagon.web.Exchange
import co.there4.hexagon.web.Server
import java.net.HttpCookie

/**
 * TODO This test smells... it should fail
 */
@Suppress("unused") // Test methods are flagged as unused
class CookiesIT : ItTest () {
    override fun initialize(server: Server) {
        server.post("/assertNoCookies") {
            if (!request.cookies.isEmpty())
                halt(500)
        }

        server.post("/setCookie") {
            val name = request ["cookieName"]
            val value = request ["cookieValue"]
            response.addCookie (HttpCookie (name, value))
        }

        server.post("/assertHasCookie") {
            checkCookie(request ["cookieName"])
        }

        server.post("/removeCookie") {
            val cookieName = request ["cookieName"]
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
            var result = post("/assertHasCookie?$cookie")
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

    private fun Client.get(url: String, sessionid: String) =
        client.prepareGet(endpoint + url).addHeader("Cookie", sessionid).execute().get()

    private fun Client.put(url: String, sessionid: String) =
        client.preparePut(endpoint + url).addHeader("Cookie", sessionid).execute().get()

    private fun Client.delete(url: String, sessionid: String) =
        client.prepareDelete(endpoint + url).addHeader("Cookie", sessionid).execute().get()

    private fun Client.post(url: String, sessionid: String) =
        client.preparePost(endpoint + url).addHeader("Cookie", sessionid).execute().get()

    private fun Exchange.checkCookie(cookieName: String?) {
        val cookieValue = request.cookies[cookieName]?.value
        if (request["cookieValue"] != cookieValue)
            halt(500)
    }
}
