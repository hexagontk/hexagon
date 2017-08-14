package co.there4.hexagon.server.undertow

import io.undertow.server.HttpServerExchange
import co.there4.hexagon.server.EngineResponse
import co.there4.hexagon.server.get
import io.undertow.server.handlers.CookieImpl
import io.undertow.util.HttpString
import java.io.OutputStream
import java.net.HttpCookie

class UndertowResponse(private val exchange: HttpServerExchange) : EngineResponse {
    override var contentType: String?
        get() = exchange.responseHeaders.getFirst("content-type")
        set(value) {
            exchange.responseHeaders.put(HttpString("content-type"), value)
        }

    override val outputStream: OutputStream
        get() = throw UnsupportedOperationException()

    override fun getMimeType(file: String): String? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addHeader(name: String, value: String) {
        exchange.responseHeaders.put(HttpString(name), value)
    }

    override fun addCookie(cookie: HttpCookie) {
        exchange.responseCookies.put(cookie.name, CookieImpl(cookie.name, cookie.value))
    }

    override fun removeCookie(name: String) {
        val cookie = exchange.requestCookies[name]
        if (cookie != null) {
            cookie.value = ""
            cookie.path = "/"
            cookie.maxAge = 0
            exchange.responseCookies[name] = cookie
        }
    }

    override var body: Any = ""
        get() = field
        set(value) { field = value }
    override var status: Int
        get() = exchange.statusCode
        set(value) { exchange.statusCode = value }

    override fun redirect(url: String) {
        exchange.statusCode = 302
        exchange.responseHeaders.put(HttpString("Location"), url)
    }
}
