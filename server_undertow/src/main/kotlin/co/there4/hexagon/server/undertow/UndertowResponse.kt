package co.there4.hexagon.server.undertow

import io.undertow.server.HttpServerExchange
import co.there4.hexagon.server.EngineResponse
import io.undertow.server.handlers.CookieImpl
import io.undertow.util.HttpString
import java.io.OutputStream
import java.net.HttpCookie

class UndertowResponse(exchange: HttpServerExchange) : EngineResponse {
    override var contentType: String?
        get() = contentType
        set(value) {
            contentType = value
        }

    override val outputStream: OutputStream
        get() = throw UnsupportedOperationException()

    override fun getMimeType(file: String): String? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addHeader(name: String, value: String) {
        e.responseHeaders.put(HttpString(name), value)
    }

    override fun addCookie(cookie: HttpCookie) {
        e.responseCookies.put(cookie.name, CookieImpl(cookie.name, cookie.value))
    }

    override fun removeCookie(name: String) {
        val cookie = e.requestCookies[name]
        if (cookie != null) {
            cookie.value = ""
            cookie.path = "/"
            cookie.maxAge = 0
            e.responseCookies[name] = cookie
        }
    }

    val e: HttpServerExchange = exchange

    override var body: Any = ""
        get() = field
        set(value) { field = value }
    override var status: Int
        get() = e.statusCode
        set(value) { e.statusCode = value }

    override fun redirect(url: String) {
        throw UnsupportedOperationException()
    }
}
