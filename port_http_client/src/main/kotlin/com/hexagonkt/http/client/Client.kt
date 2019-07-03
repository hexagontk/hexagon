package com.hexagonkt.http.client

import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.serialize
import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.*
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.ssl.SslContextBuilder
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

        val bodyValue = when (body) {
            null -> null
            is File -> Base64.getEncoder().encodeToString(body.readBytes())
            is String -> body.toString() // TODO Add test!!!
            else ->
                if (contentType == null) body.toString()
                else body.serialize(formatOf(contentType))
        }

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

    fun get(url: String, callHeaders: Map<String, List<String>> = LinkedHashMap()) =
        send(GET, url, null, callHeaders = callHeaders)

    fun head(url: String, callHeaders: Map<String, List<String>> = LinkedHashMap()) =
        send(HEAD, url, null, callHeaders = callHeaders)

    fun post(url: String, body: Any? = null, contentType: String? = this.contentType) =
        send(POST, url, body, contentType)

    fun put(url: String, body: Any? = null, contentType: String? = this.contentType) =
        send(PUT, url, body, contentType)

    fun delete(url: String, body: Any? = null, contentType: String? = this.contentType) =
        send(DELETE, url, body, contentType)

    fun trace(url: String, body: Any? = null, contentType: String? = this.contentType) =
        send(TRACE, url, body, contentType)

    fun options(url: String, body: Any? = null, contentType: String? = this.contentType) =
        send(OPTIONS, url, body, contentType)

    fun patch(url: String, body: Any? = null, contentType: String? = this.contentType) =
        send(PATCH, url, body, contentType)

    private fun createRequest(
        method: Method, url: String, contentType: String? = null, parts: List<Part>) =
            (endpoint + url).let {
                val request = when (method) {
                    GET -> client.prepareGet (it)
                    HEAD -> client.prepareHead (it)
                    POST -> client.preparePost (it)
                    PUT -> client.preparePut (it)
                    DELETE -> client.prepareDelete (it)
                    TRACE -> client.prepareTrace (it)
                    OPTIONS -> client.prepareOptions (it)
                    PATCH -> client.preparePatch (it)
                }

                request.setCharset(Charset.defaultCharset()) // TODO Problem if encoding is set?

                parts.forEach { part -> request.addBodyPart(part) }

                if (contentType != null)
                    request.addHeader("Content-Type", contentType)

                if (authorization != null)
                    request.addHeader("Authorization", "Basic $authorization")

                request
            }
}
