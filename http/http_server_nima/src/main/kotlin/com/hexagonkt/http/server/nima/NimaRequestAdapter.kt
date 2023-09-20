package com.hexagonkt.http.server.nima

import com.hexagonkt.core.media.MediaType
import com.hexagonkt.core.media.MediaTypeGroup
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.Headers
import io.helidon.http.Http
import io.helidon.http.media.multipart.MultiPart
import io.helidon.webserver.http.ServerRequest
import java.security.cert.X509Certificate
import kotlin.jvm.optionals.getOrNull

class NimaRequestAdapter(
    methodName: Http.Method,
    req: ServerRequest,
) : HttpRequestPort {

    override val certificateChain: List<X509Certificate> by lazy {
        req.remotePeer().tlsCertificates().getOrNull()
            ?.toList()
            ?.map { it as X509Certificate }
            ?: emptyList()
    }

    override val accept: List<ContentType> by lazy {
        req.headers().acceptedTypes().map {
            val mt = it.mediaType()
            val t = mt.type().uppercase()
            val st = mt.subtype()
            ContentType(MediaType(MediaTypeGroup.valueOf(t), st))
        }
    }

    override val contentLength: Long by lazy {
        req.headers().get(Http.HeaderNames.CONTENT_LENGTH).get().toLong()
    }

    override val queryParameters: QueryParameters by lazy {
        QueryParameters(
            req.query().names().map {
                QueryParameter(it, req.query().all(it))
            }
        )
    }

    override val parts: List<HttpPart> by lazy {
        try {
            val multiPart = req.content().`as`(MultiPart::class.java)
            var parts = emptyList<HttpPart>()
            // TODO Fails when parsing multiple parts !?
            multiPart.forEach { p ->
                val name = p.name()
                val bytes = p.inputStream().readAllBytes()
                val fileName = p.fileName().getOrNull()
                    ?.let { HttpPart(name, bytes, it) }
                    ?: HttpPart(name, String(bytes))

                parts = parts + fileName
            }
            parts
        }
        catch (e: Exception) {
            emptyList()
        }
    }

    override val formParameters: FormParameters by lazy {
        val fields = parts
            .filter { it.submittedFileName == null }
            .map { FormParameter(it.name, it.bodyString()) }

        FormParameters(fields)
    }

    override val method: HttpMethod by lazy {
        HttpMethod.valueOf(methodName.text())
    }

    override val protocol: HttpProtocol by lazy {
        HttpProtocol.valueOf(req.prologue().protocol())
    }

    override val host: String by lazy {
        req.remotePeer().host()
    }

    override val port: Int by lazy {
        req.remotePeer().port()
    }

    override val path: String by lazy {
        req.path().path()
    }

    override val cookies: List<Cookie> by lazy {
        req.headers().cookies().toMap().map { (k, v) -> Cookie(k, v.first()) }
    }

    override val body: Any by lazy {
        req.content().inputStream().readAllBytes()
    }

    override val headers: Headers by lazy {
        Headers(req.headers().map { Header(it.name(), it.allValues()) })
    }

    override val contentType: ContentType? by lazy {
        req.headers().contentType().map {
            val mt = it.mediaType()
            val t = mt.type().uppercase()
            val st = mt.subtype()
            ContentType(MediaType(MediaTypeGroup.valueOf(t), st))
        }
        .orElse(null)
    }

    override val authorization: Authorization? by lazy { authorization() }

    override fun with(
        body: Any,
        headers: Headers,
        contentType: ContentType?,
        method: HttpMethod,
        protocol: HttpProtocol,
        host: String,
        port: Int,
        path: String,
        queryParameters: QueryParameters,
        parts: List<HttpPart>,
        formParameters: FormParameters,
        cookies: List<Cookie>,
        accept: List<ContentType>,
        authorization: Authorization?,
        certificateChain: List<X509Certificate>
    ): HttpRequestPort =
        throw UnsupportedOperationException()
}
