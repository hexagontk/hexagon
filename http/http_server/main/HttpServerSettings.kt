package com.hexagontk.http.server

import com.hexagontk.core.Platform
import com.hexagontk.http.SslSettings
import com.hexagontk.http.model.HttpProtocol
import com.hexagontk.http.model.HttpProtocol.HTTP
import java.net.InetAddress
import java.net.URI

/**
 * Holds server settings info.
 *
 * @property bindAddress Address for the server.
 * @property bindPort Port for the server.
 * @property protocol Server's protocol.
 * @property sslSettings SSL settings info for configuring the server.
 * @property zip Option to compress server responses.
 * @property bindUrl Base URI to connect to the server. It lacks the port (as it can be dynamic).
 */
class HttpServerSettings(
    val bindAddress: InetAddress = InetAddress.getLoopbackAddress(),
    val bindPort: Int = 2010,
    val protocol: HttpProtocol = HTTP,
    val sslSettings: SslSettings? = null,
    val zip: Boolean = false,
) {
    val bindUrl: URI by lazy {
        val scheme = if (protocol == HTTP) "http" else "https"
        val hostName =
            if (bindAddress.isAnyLocalAddress) Platform.ip else bindAddress.canonicalHostName

        URI("$scheme://$hostName")
    }

    fun with(
        bindAddress: InetAddress = this.bindAddress,
        bindPort: Int = this.bindPort,
        protocol: HttpProtocol = this.protocol,
        sslSettings: SslSettings? = this.sslSettings,
        zip: Boolean = this.zip,
    ): HttpServerSettings =
        HttpServerSettings(bindAddress, bindPort, protocol, sslSettings, zip)
}
