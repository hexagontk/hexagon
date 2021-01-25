package com.hexagonkt.http.server

import com.hexagonkt.http.Protocol
import com.hexagonkt.http.Protocol.HTTP
import com.hexagonkt.http.SslSettings
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
 * @property features List of features enabled for a server.
 * @property options Server options. Supported options change among adapters.
 */
data class ServerSettings(
    val bindAddress: InetAddress = InetAddress.getLoopbackAddress(),
    val bindPort: Int = 2010,
    val contextPath: String = "",
    val protocol: Protocol = HTTP,
    val sslSettings: SslSettings? = null,
    val banner: String? = null,
    val features: Set<ServerFeature> = emptySet(),
    val options: Map<String, Any> = emptyMap(),
)
