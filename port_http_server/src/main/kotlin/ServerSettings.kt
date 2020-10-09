package com.hexagonkt.http.server

import com.hexagonkt.http.Protocol
import com.hexagonkt.http.Protocol.HTTP
import com.hexagonkt.http.SslSettings
import java.net.InetAddress

data class ServerSettings(
    val bindAddress: InetAddress = InetAddress.getLoopbackAddress(),
    val bindPort: Int = 2010,
    val contextPath: String = "",
    val protocol: Protocol = HTTP,
    val sslSettings: SslSettings? = null,
    val banner: String? = null
)
