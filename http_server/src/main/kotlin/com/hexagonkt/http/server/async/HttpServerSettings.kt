package com.hexagonkt.http.server.async

import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.model.HttpProtocol
import com.hexagonkt.http.model.HttpProtocol.HTTP
import java.net.InetAddress

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
 * @property vmInformation If true, show JVM information on start banner. If enabled, it forces
 *  the `java.management` module to be included.
 */
data class HttpServerSettings(
    val bindAddress: InetAddress = InetAddress.getLoopbackAddress(),
    val bindPort: Int = 2010,
    val contextPath: String = "",
    val protocol: HttpProtocol = HTTP,
    val sslSettings: SslSettings? = null,
    val banner: String? = null,
    val zip: Boolean = false,
    val vmInformation: Boolean = false,
)
