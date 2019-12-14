package com.hexagonkt.http.server

import com.hexagonkt.http.Protocol
import com.hexagonkt.http.Protocol.HTTP
import java.net.InetAddress

data class ServerSettings(
    val serverName: String = "<undefined>",
    val bindAddress: InetAddress = InetAddress.getLoopbackAddress(),
    val bindPort: Int = 2010,
    val contextPath: String = "",
    val protocol: Protocol = HTTP,
    val sslSettings: SslSettings? = null
)
