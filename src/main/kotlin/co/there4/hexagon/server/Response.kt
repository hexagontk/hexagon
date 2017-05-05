package co.there4.hexagon.server

import co.there4.hexagon.server.engine.IResponse
import java.io.OutputStream
import java.net.HttpCookie

/**
 * Check 'Request' comment. And also note that lists should be updated by engines after callback
 * is processed, this data is like a buffer that needs to be dumped to the real response.
 *
 * HTTP response context.
 */
class Response (private val response: IResponse) {
    val outputStream: OutputStream by lazy { response.outputStream }

    var body: Any
        get() = response.body
        set(value) { response.body = value }

    var status: Int
        get() = response.status
        set(value) { response.status = value }

    var contentType: String?
        get() = response.contentType
        set(value) { response.contentType = value }

    fun getMimeType (file: String): String? = response.getMimeType(file)
    fun addHeader (name: String, value: String) = response.addHeader(name, value)

    fun addCookie (cookie: HttpCookie) = response.addCookie(cookie)
    fun removeCookie (name: String) = response.removeCookie(name)

    fun redirect (url: String) = response.redirect(url)
}
