package com.hexagonkt.http.client.ahc

import com.hexagonkt.helpers.ensureSize
import com.hexagonkt.helpers.stream
import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.serialize
import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.*
import com.hexagonkt.http.client.*
import io.netty.handler.codec.http.cookie.DefaultCookie
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder.forClient as sslContextBuilderClient
import org.asynchttpclient.BoundRequestBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE as InsecureTrustManager
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder
import org.asynchttpclient.request.body.multipart.InputStreamPart
import org.asynchttpclient.request.body.multipart.StringPart
import org.asynchttpclient.request.body.multipart.Part as AhcPart
import java.io.File
import java.net.HttpCookie
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.KeyStore.PrivateKeyEntry
import java.security.cert.X509Certificate
import java.util.*
import java.util.Base64.Encoder

/**
 * Client to use other REST services.
 */
class AhcAdapter : ClientPort {

    private val base64encoder: Encoder by lazy { Base64.getEncoder() }

    private var authorization: String? = null

    private lateinit var ssl: ClientSettings

    // TODO Cache this as this will be done in each request
    private val ahcClient: DefaultAsyncHttpClient get() =
        DefaultAsyncHttpClient(
            Builder()
                .setConnectTimeout(5000)
                .setSslContext(sslContext(ssl))
                .build()
        )

    private fun sslContext(settings: ClientSettings): SslContext = sslContextBuilderClient().let {
        when {
            settings.insecure ->
                it.trustManager(InsecureTrustManager).build()

            settings.sslSettings != null -> {
                val sslSettings = settings.sslSettings
                val keyStore = sslSettings!!.keyStore
                val trustStore = sslSettings.trustStore

                var sslContextBuilder = it

                if (keyStore != null) {
                    val password = sslSettings.keyStorePassword
                    val store = keyStore(keyStore, password)
                    val passwordProtection = PasswordProtection(password.toCharArray())
                    val key = store
                        .aliases()
                        .toList()
                        .filter { alias -> store.isKeyEntry(alias) }
                        .mapNotNull { alias ->
                            store.getEntry(alias, passwordProtection) as PrivateKeyEntry
                        }
                        .ensureSize(1..1)
                        .first()

                    val certificateChain = key.certificateChain
                        .toList()
                        .mapNotNull { certificate -> certificate as X509Certificate }
                        .toTypedArray()

                    sslContextBuilder = sslContextBuilder
                        .keyManager(key.privateKey, *certificateChain)
                }

                if (trustStore != null) {
                    val store = keyStore(trustStore, sslSettings.trustStorePassword)
                    val certs = store
                        .aliases()
                        .toList()
                        .mapNotNull { alias -> store.getCertificate(alias) as X509Certificate }
                        .toTypedArray()

                    sslContextBuilder = sslContextBuilder.trustManager(*certs)
                }

                sslContextBuilder.build()
            }

            else ->
                it.build()
        }
    }

    private fun keyStore(uri: URI, password: String): KeyStore {
        val keyStore = KeyStore.getInstance("pkcs12")
        keyStore.load(uri.stream(), password.toCharArray())
        return keyStore
    }

    override fun send(client: Client, request: Request): Response {

        val settings: ClientSettings = client.settings
        val ahcRequest = createRequest(client, request)
        val bodyValue = createBodyValue(request.body, request.contentType)

        if (bodyValue != null)
            ahcRequest.setBody(bodyValue)

        (settings.headers + request.headers).forEach { ahcRequest.addHeader(it.key, it.value) }

        if (settings.useCookies)
            client.cookies.forEach {
                ahcRequest.addCookie(DefaultCookie(it.value.name, it.value.value))
            }

        val response = ahcRequest.execute().get()

        if (settings.useCookies) {
            response.cookies.forEach {
                if (it.value() == "")
                    client.cookies.remove(it.name())
                else
                    client.cookies[it.name()] = HttpCookie(it.name(), it.value())
            }
        }

        // TODO Make header lookup case insensitive (and add tests)
        val returnHeaders: MutableMap<String, List<String>> = HashMap(
            response.headers.names()
                .map { it to response.headers.getAll(it) }
                .toMap()
        )

        return response.let {
            Response(
                status = it.statusCode,
                body = it.responseBody,
                headers = returnHeaders,
                contentType = it.contentType,
                inputStream = it.responseBodyAsStream
            )
        }
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

    private fun createRequest(
        cl: Client, request: Request): BoundRequestBuilder {

        val method: Method = request.method
        val path: String = cl.endpoint + request.path.path
        val settings: ClientSettings = cl.settings
        val contentType: String? = request.contentType
        val parts: List<AhcPart> = request.parts.values.toList().map {
            if (it.submittedFileName == null)
                StringPart(it.name, it.inputStream.reader().readText())
            else
                InputStreamPart(it.name, it.inputStream, it.submittedFileName)
        }

        ssl = cl.settings
        val req = when (method) {
            GET -> ahcClient.prepareGet(path)
            HEAD -> ahcClient.prepareHead(path)
            POST -> ahcClient.preparePost(path)
            PUT -> ahcClient.preparePut(path)
            DELETE -> ahcClient.prepareDelete(path)
            TRACE -> ahcClient.prepareTrace(path)
            OPTIONS -> ahcClient.prepareOptions(path)
            PATCH -> ahcClient.preparePatch(path)
        }

        req.setCharset(Charset.defaultCharset()) // TODO Problem if encoding is set?

        parts.forEach { part -> req.addBodyPart(part) }

        if (contentType != null)
            req.addHeader("Content-Type", contentType)

        authorization =
            if (settings.user != null)
                base64encoder.encodeToString("${settings.user}:${settings.password}"
                    .toByteArray(UTF_8))
            else
                null
        if (authorization != null)
            req.addHeader("Authorization", "Basic $authorization")

        return req
    }
}
