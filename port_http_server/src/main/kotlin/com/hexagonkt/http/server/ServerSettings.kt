package com.hexagonkt.http.server

import java.net.InetAddress

data class ServerSettings(
    val serverName: String = "<undefined>",
    val bindAddress: InetAddress = InetAddress.getLoopbackAddress(),
    val bindPort: Int = 2010,
    val contextPath: String = ""
)
