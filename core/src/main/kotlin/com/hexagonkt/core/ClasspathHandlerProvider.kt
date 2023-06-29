package com.hexagonkt.core

import java.net.URLStreamHandler
import java.net.spi.URLStreamHandlerProvider

internal class ClasspathHandlerProvider : URLStreamHandlerProvider() {

    override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
        return ClasspathHandler.createURLStreamHandler(protocol)
    }
}
