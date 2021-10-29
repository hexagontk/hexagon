package com.hexagonkt.http.server

import com.hexagonkt.core.helpers.CodedException
import com.hexagonkt.core.serialization.ContentType
import com.hexagonkt.core.serialization.SerializationFormat
import com.hexagonkt.core.serialization.SerializationManager
import com.hexagonkt.core.serialization.serialize
import java.nio.charset.Charset

/**
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
class Call(val request: Request, val response: Response, val session: Session) {

    /** Call attributes (for the current request). Same as HttpServletRequest.setAttribute(). */
    val attributes: MutableMap<String, Any> by lazy { LinkedHashMap() }

    val requestType: String get() =
        request.requestType()

    val requestFormat: SerializationFormat get() =
        request.requestFormat()

    val responseType: String get() =
        response.contentType ?:
        request.accept?.let { if (it == "*/*") null else it } ?:
        requestType

    val responseFormat: SerializationFormat get() =
        SerializationManager.formatOf(responseType)

    // Request shortcuts
    val pathParameters: Map<String, String> by lazy { request.pathParameters }
    val queryParametersValues: Map<String, List<String>> by lazy { request.queryParametersValues }
    val formParametersValues: Map<String, List<String>> by lazy { request.formParametersValues }
    val queryParameters: Map<String, String> by lazy { request.queryParameters }
    val formParameters: Map<String, String> by lazy { request.formParameters }

    /**
     * Sends success response with given content type.
     *
     * @param content Content of the response.
     * @param contentType Content type of the response.
     */
    fun ok(content: Any = "", contentType: String? = null) = send(200, content, contentType)

    /**
     * Sends success response serialized using given [SerializationFormat] and [Charset].
     *
     * @param content Content of the response.
     * @param serializationFormat Serialization format for serializing the response.
     * @param charset Character Set to be used for the content type.
     */
    fun ok(
        content: Any,
        serializationFormat: SerializationFormat = responseFormat,
        charset: Charset? = null) =
            send(200, content, serializationFormat, charset)

    /**
     * Sends response to the client.
     *
     * @param code Status code of the response.
     * @param content Content of the response.
     * @param contentType Content type of the response.
     */
    fun send(code: Int, content: Any = "", contentType: String? = null) {
        response.status = code
        response.body = content

        if (contentType != null)
            response.contentType = contentType
    }

    /**
     * Sends response to the client after serializing using given [SerializationFormat]
     * and [Charset].
     *
     * @param code Status code of the response.
     * @param content Content of the response.
     * @param serializationFormat Serialization format for serializing the response.
     * @param charset Character Set to be used for the content type.
     */
    fun send(code: Int, content: Any, serializationFormat: SerializationFormat, charset: Charset?) {
        send(code, content, ContentType(serializationFormat, charset))
    }

    /**
     * Sends response to the client after serializing using given [ContentType] instance.
     *
     * @param code Status code of the response.
     * @param content Content of the response.
     * @param contentType Content type of the response.
     */
    // TODO Handle charset: transform content to the proper encoding
    fun send(code: Int, content: Any, contentType: ContentType) =
        send(code, content.serialize(contentType.format), contentType.toString())

    /**
     * Sends error response.
     *
     * @param content Message for error response.
     */
    fun halt(content: Any): Nothing =
        halt(500, content)

    /**
     * Sends error response.
     *
     * @param code Status code for error response.
     * @param content Message for error response.
     */
    fun halt(code: Int = 500, content: Any = ""): Nothing {
        throw CodedException(code, content.toString())
    }

    /**
     * Sends a redirect response to the client using the
     * specified redirect URL.
     *
     * @param url Redirect URL.
     */
    fun redirect(url: String) {
        response.redirect(url)
    }
}
