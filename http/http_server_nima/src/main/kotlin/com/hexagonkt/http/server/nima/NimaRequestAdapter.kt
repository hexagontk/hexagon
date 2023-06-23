package com.hexagonkt.http.server.nima

import com.hexagonkt.core.media.MediaType
import com.hexagonkt.core.media.MediaTypeGroup
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.Headers
import io.helidon.common.http.Http.Header as NimaHeader
import io.helidon.common.http.Http
import io.helidon.nima.http.media.multipart.MultiPart
import io.helidon.nima.webserver.http.ServerRequest
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
        req.headers().get(NimaHeader.CONTENT_LENGTH).value().toLong()
    }

    override val queryParameters: QueryParameters by lazy {
        QueryParameters(
            req.query().names().map {
                QueryParameter(it, req.query().all(it))
            }
        )
    }

    override val parts: List<HttpPart> by lazy {
        requestParts.filterIsInstance(HttpPart::class.java)
    }

    override val formParameters: FormParameters by lazy {
        FormParameters(requestParts.filterIsInstance(FormParameter::class.java))
    }

    private val requestParts: List<Any> by lazy {
        try {
            val multiPart = req.content().`as`(MultiPart::class.java).iterator()
            var parts = emptyList<Any>()
            multiPart.forEachRemaining { p ->
                val cd = p.partHeaders().get(Http.Header.create("content-disposition")).value()
                if (cd.startsWith("form-data")) {
                    parts = parts + FormParameter(p.name(), p.inputStream().reader().readText())
                } else {
                    val x = p.fileName()
                    val b = p.inputStream().readAllBytes()
                    val h = p.partHeaders()
                    parts = parts + HttpPart(p.name(), b, submittedFileName = p.fileName().get())
                }
            }
            parts
        }
        catch (e: Exception) {
            emptyList()
        }
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
        val c = req.headers().cookies().toMap()

        c.map { (k, v) ->
            Cookie(
                name = k,
                value = v.first(),
//                maxAge = if (it.maxAge() == Long.MIN_VALUE) -1 else it.maxAge(),
//                secure = it.isSecure,
            )
        }
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
