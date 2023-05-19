package com.hexagonkt.http.server.nima

import com.hexagonkt.core.media.MediaType
import com.hexagonkt.core.media.MediaTypeGroup
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.Headers
import com.hexagonkt.http.model.Headers as HxHttpHeaders
import io.helidon.common.http.Http
import io.helidon.nima.webserver.http.ServerRequest
import java.security.cert.X509Certificate

class NimaRequestAdapter(
    methodName: Http.Method,
    req: ServerRequest,
) : HttpRequestPort {

    override val certificateChain: List<X509Certificate> by lazy {
        TODO()
    }

    override val accept: List<ContentType> by lazy {
//        nettyHeaders.getAll(ACCEPT).flatMap { it.split(",") }.map { parseContentType(it) }
        TODO()
    }

    override val contentLength: Long by lazy {
//        nettyHeaders[CONTENT_LENGTH]?.toLong() ?: 0L
        TODO()
    }

    override val queryParameters: QueryParameters by lazy {
        QueryParameters(
            req.query().names().map {
                QueryParameter(it, req.query().all(it))
            }
        )
    }

    override val parts: List<HttpPart> by lazy {
//        HttpPostRequestDecoder(req).bodyHttpDatas.map {
//            when (it) {
//                is FileUpload -> HttpPart(
//                    name = it.name,
//                    body = ByteBufUtil.getBytes(it.content()),
//                    submittedFileName = it.filename,
//                    contentType = it.contentType?.let { ct -> parseContentType(ct) },
//                )
//                is Attribute -> HttpPart(it.name, it.value)
//                else -> error("Unknown part type: ${it.javaClass}")
//            }
//        }
        TODO()
    }

    override val formParameters: FormParameters by lazy {
        val fields = parts
            .filter { it.submittedFileName == null }
            .groupBy { it.name }
            .mapValues { it.value.map { v -> v.bodyString() } }
            .map { (k, v) -> FormParameter(k, v) }

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

    override val cookies: List<com.hexagonkt.http.model.Cookie> by lazy {
//        val cookieHeader: String = nettyHeaders.get(COOKIE)
//            ?: return@lazy emptyList<com.hexagonkt.http.model.Cookie>()
//
//        val cookies: Set<Cookie> = ServerCookieDecoder.STRICT.decode(cookieHeader)
//
//        cookies.map {
//            Cookie(
//                name = it.name(),
//                value = it.value(),
//                maxAge = if (it.maxAge() == Long.MIN_VALUE) -1 else it.maxAge(),
//                secure = it.isSecure,
//            )
//        }
        TODO()
    }

    override val body: Any by lazy {
//        val content =
//            if (req is ByteBufHolder) req.content()
//            else Unpooled.buffer(0)
//
//        if (content.isReadable) ByteBufUtil.getBytes(content)
//        else byteArrayOf()
        TODO()
    }

    override val headers: HxHttpHeaders by lazy {
//        HxHttpHeaders(
//            nettyHeaders.names()
//                .toList()
//                .map { it.lowercase() }
//                .map { Header(it, nettyHeaders.getAll(it)) }
//        )
        TODO()
    }

    override val contentType: ContentType? by lazy {
        req.headers().contentType().map {
            val mt = it.mediaType()
            val t = mt.type()
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
