package com.hexagonkt.http.server.coroutines

import com.hexagonkt.core.Jvm
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.HTTP
import java.net.InetAddress
import java.net.URL

/**
 * Holds server settings info.
 *
 * @property bindAddress Address for the server.
 * @property bindPort Port for the server.
 * @property contextPath Context Path for the server's incoming requests.
 * @property protocol Server's protocol.
 * @property sslSettings SSL settings info for configuring the server.
 * @property banner Server banner message.
 * @property zip Option to compress server responses.
 * @property bindUrl Base URL to connect to the server. It lacks the port (as it can be dynamic).
 */
data class HttpServerSettings(
    val bindAddress: InetAddress = InetAddress.getLoopbackAddress(),
    val bindPort: Int = 2010,
    val contextPath: String = "",
    val protocol: HttpProtocol = HTTP,
    val sslSettings: SslSettings? = null,
    val banner: String? = HttpServer.banner,
    val zip: Boolean = false,
) {
    val bindUrl: URL by lazy {
        val hostName = if (bindAddress.isAnyLocalAddress) Jvm.ip else bindAddress.canonicalHostName
        val scheme = if (protocol == HTTP) "http" else "https"
        urlOf("$scheme://$hostName")
    }
}
