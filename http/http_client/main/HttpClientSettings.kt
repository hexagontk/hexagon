package com.hexagontk.http.client

import com.hexagontk.http.SslSettings
import com.hexagontk.http.model.*
import java.net.URI

// TODO Add proxy configuration and timeouts
class HttpClientSettings(
    val baseUri: URI? = null,
    val contentType: ContentType? = null,
    val accept: List<ContentType> = emptyList(),
    val useCookies: Boolean = true,
    val headers: Headers = Headers(),
    val insecure: Boolean = false,
    val sslSettings: SslSettings? = null,
    val authorization: Authorization? = null,
    val followRedirects: Boolean = false,
) {
    fun with(
        baseUri: URI? = this.baseUri,
        contentType: ContentType? = this.contentType,
        accept: List<ContentType> = this.accept,
        useCookies: Boolean = this.useCookies,
        headers: Headers = this.headers,
        insecure: Boolean = this.insecure,
        sslSettings: SslSettings? = this.sslSettings,
        authorization: Authorization? = this.authorization,
        followRedirects: Boolean = this.followRedirects,
    ): HttpClientSettings =
        HttpClientSettings(
            baseUri,
            contentType,
            accept,
            useCookies,
            headers,
            insecure,
            sslSettings,
            authorization,
            followRedirects
        )
}
