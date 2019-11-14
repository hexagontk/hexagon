package com.hexagonkt.http.server

import com.hexagonkt.http.server.HttpProtocol.HTTP
import java.net.InetAddress
import java.net.URI

data class ServerSettings(
    val serverName: String = "<undefined>",
    val bindAddress: InetAddress = InetAddress.getLoopbackAddress(),
    val bindPort: Int = 2010,
    val contextPath: String = "",
    val protocol: HttpProtocol = HTTP,
    val keyStore: URI? = null,
    val trustStore: URI? = null
)
