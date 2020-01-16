package com.hexagonkt.http.server

import com.hexagonkt.helpers.CodedException
import com.hexagonkt.serialization.ContentType
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.serialize
import java.nio.charset.Charset

/**
 * HTTP request context. It holds client supplied data and methods to change the response.
 *
 * TODO Move Request, Response and Session abstract methods here and pass the call to them
 */
class Call(val request: Request, val response: Response, val session: Session) {

    /** Call attributes (for the current request). Same as HttpServletRequest.setAttribute(). */
    @Suppress("RemoveExplicitTypeArguments") // Without types fails inside IntelliJ
    val attributes: MutableMap<String, Any> by lazy { LinkedHashMap<String, Any>() }

    val requestType: String get() =
        request.requestType()

    val requestFormat: SerializationFormat get() =
        request.requestFormat()

    val responseType: String get() =
        response.contentType ?:
        request.accept.firstOrNull()?.let { if (it == "*/*") null else it } ?:
        requestType

    val responseFormat: SerializationFormat get() =
        SerializationManager.formatOf(responseType)

    // Request shortcuts
    val pathParameters: Map<String, String> by lazy { request.pathParameters }
    val queryParameters: Map<String, List<String>> by lazy { request.queryParameters }
    val formParameters: Map<String, List<String>> by lazy { request.formParameters }
    val parameters: Map<String, List<String>> by lazy { request.parameters }

    fun ok(content: Any = "", contentType: String? = null) = send(200, content, contentType)

    fun ok(
        content: Any,
        serializationFormat: SerializationFormat = responseFormat,
        charset: Charset? = null) =
            send(200, content, serializationFormat, charset)

    fun send(code: Int, content: Any = "", contentType: String? = null) {
        response.status = code
        response.body = content

        if (contentType != null)
            response.contentType = contentType
    }

    fun send(code: Int, content: Any, serializationFormat: SerializationFormat, charset: Charset?) {
        send(code, content, ContentType(serializationFormat, charset))
    }

    // TODO Handle charset: transform content to the proper encoding
    fun send(code: Int, content: Any, contentType: ContentType) =
        send(code, content.serialize(contentType.format), contentType.toString())

    fun halt(content: Any): Nothing =
        halt(500, content)

    fun halt(code: Int = 500, content: Any = ""): Nothing {
        throw CodedException(code, content.toString())
    }

    fun redirect(url: String) {
        response.redirect(url)
    }
}
