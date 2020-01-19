package com.hexagonkt.http.client

import com.hexagonkt.helpers.Resource
import com.hexagonkt.helpers.ensureSize
import com.hexagonkt.helpers.stream
import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.serialize
import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.*
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import org.asynchttpclient.BoundRequestBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE as InsecureTrustManager
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder
import org.asynchttpclient.Response
import org.asynchttpclient.request.body.multipart.Part
import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.KeyStore.PrivateKeyEntry
import java.security.cert.X509Certificate
import java.util.*
import java.util.Base64.Encoder
import kotlin.collections.LinkedHashMap

/**
 * Client to use other REST services.
 */
class Client(val endpoint: String = "", val settings: ClientSettings = ClientSettings()) {

    private val base64encoder: Encoder = Base64.getEncoder()

    private val authorization: String? =
        if (settings.user != null)
            base64encoder.encodeToString("${settings.user}:${settings.password}".toByteArray(UTF_8))
        else
            null

    private val client = DefaultAsyncHttpClient(Builder()
        .setConnectTimeout(5000)
        .setSslContext(sslContext())
        .build()
    )

    private fun sslContext(): SslContext = SslContextBuilder.forClient().let {
        when {
            settings.insecure -> it.trustManager(InsecureTrustManager).build()

            settings.sslSettings != null -> {
                val sslSettings = settings.sslSettings
                val keyStore = sslSettings.keyStore
                val trustStore = sslSettings.trustStore

                var sslContextBuilder = it

                if (keyStore != null) {
                    val store = keyStore(keyStore)
                    val password = authority(keyStore)
                    val passwordProtection = PasswordProtection(password.toCharArray())
                    val key = store
                        .aliases()
                        .toList()
                        .filter { alias -> store.isKeyEntry(alias) }
                        .mapNotNull { alias ->
                            store.getEntry(alias, passwordProtection) as? PrivateKeyEntry
                        }
                        .ensureSize(1..1)
                        .first()

                    val certificateChain = key.certificateChain
                        .toList()
                        .mapNotNull { certificate -> certificate as? X509Certificate }
                        .toTypedArray()

                    sslContextBuilder = sslContextBuilder
                        .keyManager(key.privateKey, *certificateChain)
                }

                if (trustStore != null) {
                    val store = keyStore(trustStore)
                    val certs = store
                        .aliases()
                        .toList()
                        .mapNotNull { alias -> store.getCertificate(alias) as? X509Certificate }
                        .toTypedArray()

                    sslContextBuilder = sslContextBuilder.trustManager(*certs)
                }

                sslContextBuilder.build()
            }

            else -> it.build()
        }
    }

    private fun keyStore(uri: URI): KeyStore {
        val keyStore = KeyStore.getInstance("pkcs12")
        val password = authority(uri)
        keyStore.load(uri.stream(), password.toCharArray())
        return keyStore
    }

    val cookies: MutableMap<String, Cookie> = mutableMapOf()

    /**
     * Synchronous execution.
     */
    fun send(
        method: Method,
        url: String = "",
        body: Any? = null,
        contentType: String? = settings.contentType,
        callHeaders: Map<String, List<String>> = LinkedHashMap(),
        parts: List<Part> = emptyList()): Response {

        val request = createRequest(method, url, contentType, parts)
        val bodyValue = createBodyValue(body, contentType)

        if (bodyValue != null)
            request.setBody(bodyValue)

        (settings.headers + callHeaders).forEach { request.addHeader(it.key, it.value) }

        if (settings.useCookies)
            cookies.forEach { request.addCookie(it.value) }

        val response = request.execute().get()

        if (settings.useCookies) {
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
            is String -> body.toString()
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
        contentType: String? = settings.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(POST, url, body, contentType).apply(callback)

    fun put(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(PUT, url, body, contentType).apply(callback)

    fun delete(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(DELETE, url, body, contentType).apply(callback)

    fun trace(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(TRACE, url, body, contentType).apply(callback)

    fun options(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType,
        callHeaders: Map<String, List<String>> = emptyMap(),
        callback: Response.() -> Unit = {}): Response =
            send(OPTIONS, url, body, contentType, callHeaders).apply(callback)

    fun patch(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType,
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

    internal fun authority(uri: URI): String =
        uri.authority ?: ""
}
