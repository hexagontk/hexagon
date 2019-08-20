package com.hexagonkt.http.client

import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.serialize
import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.*
import com.hexagonkt.serialization.SerializationFormat
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.ssl.SslContextBuilder
import org.asynchttpclient.BoundRequestBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE as InsecureTrustManager
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder
import org.asynchttpclient.Response
import org.asynchttpclient.request.body.multipart.Part
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.Base64.Encoder
import kotlin.collections.LinkedHashMap

/**
 * Client to use other REST services.
 */
class Client(
    val endpoint: String = "",
    val contentType: String? = null,
    val useCookies: Boolean = true,
    val headers: Map<String, List<String>> = LinkedHashMap(),
    user: String? = null,
    password: String? = null,
    insecure: Boolean = false) {

    constructor(
        endpoint: String = "",
        format: SerializationFormat,
        useCookies: Boolean = true,
        headers: Map<String, List<String>> = LinkedHashMap(),
        user: String? = null,
        password: String? = null,
        insecure: Boolean = false):
            this(endpoint, format.contentType, useCookies, headers, user, password, insecure)

    private val base64encoder: Encoder = Base64.getEncoder()

    private val authorization: String? =
        if (user != null) base64encoder.encodeToString("$user:$password".toByteArray(UTF_8))
        else null

    private val client = DefaultAsyncHttpClient(Builder()
        .setConnectTimeout(5000)
        .setSslContext(
            if (insecure) SslContextBuilder.forClient().trustManager(InsecureTrustManager).build()
            else SslContextBuilder.forClient().build()
        )
        .build()
    )

    val cookies: MutableMap<String, Cookie> = mutableMapOf()

    /**
     * Synchronous execution.
     */
    fun send(
        method: Method,
        url: String = "",
        body: Any? = null,
        contentType: String? = this.contentType,
        callHeaders: Map<String, List<String>> = LinkedHashMap(),
        parts: List<Part> = emptyList()): Response {

        val request = createRequest(method, url, contentType, parts)
        val bodyValue = createBodyValue(body, contentType)

        if (bodyValue != null)
            request.setBody(bodyValue)

        (headers + callHeaders).forEach { request.addHeader(it.key, it.value) }

        if (useCookies)
            cookies.forEach { request.addCookie(it.value) }

        val response = request.execute().get()

        if (useCookies) {
            response.cookies.forEach {
                if (it.value() == "")
                    cookies.remove(it.name())
                else
                    cookies[it.name()] = it
            }
        }

        return response
    }

    private fun createBodyValue(body: Any?, contentType: String?): String? =
        when (body) {
            null -> null
            is File -> Base64.getEncoder().encodeToString(body.readBytes())
            is String -> body.toString() // TODO Add test!!!
            else ->
                if (contentType == null) body.toString()
                else body.serialize(formatOf(contentType))
        }

    fun get(
        url: String,
        callHeaders: Map<String, List<String>> = emptyMap(),
        callback: Response.() -> Unit = {}): Response =
            send(GET, url, null, callHeaders = callHeaders).apply(callback)

    fun head(
        url: String,
        callHeaders: Map<String, List<String>> = emptyMap(),
        callback: Response.() -> Unit = {}): Response =
            send(HEAD, url, null, callHeaders = callHeaders).apply(callback)

    fun post(
        url: String,
        body: Any? = null,
        contentType: String? = this.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(POST, url, body, contentType).apply(callback)

    fun put(
        url: String,
        body: Any? = null,
        contentType: String? = this.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(PUT, url, body, contentType).apply(callback)

    fun delete(
        url: String,
        body: Any? = null,
        contentType: String? = this.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(DELETE, url, body, contentType).apply(callback)

    fun trace(
        url: String,
        body: Any? = null,
        contentType: String? = this.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(TRACE, url, body, contentType).apply(callback)

    fun options(
        url: String,
        body: Any? = null,
        contentType: String? = this.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(OPTIONS, url, body, contentType).apply(callback)

    fun patch(
        url: String,
        body: Any? = null,
        contentType: String? = this.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(PATCH, url, body, contentType).apply(callback)

    private fun createRequest(
        method: Method,
        url: String,
        contentType: String? = null,
        parts: List<Part>): BoundRequestBuilder {

        val path = endpoint + url
        val request = when (method) {
            GET -> client.prepareGet(path)
            HEAD -> client.prepareHead(path)
            POST -> client.preparePost(path)
            PUT -> client.preparePut(path)
            DELETE -> client.prepareDelete(path)
            TRACE -> client.prepareTrace(path)
            OPTIONS -> client.prepareOptions(path)
            PATCH -> client.preparePatch(path)
        }

        request.setCharset(Charset.defaultCharset()) // TODO Problem if encoding is set?

        parts.forEach { part -> request.addBodyPart(part) }

        if (contentType != null)
            request.addHeader("Content-Type", contentType)

        if (authorization != null)
            request.addHeader("Authorization", "Basic $authorization")

        return request
    }
}
