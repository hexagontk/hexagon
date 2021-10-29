package com.hexagonkt.core

import java.net.URLStreamHandler
import java.net.spi.URLStreamHandlerProvider

/**
 * JDK 11 only
 */
internal class ClasspathHandlerProvider : URLStreamHandlerProvider() {

    override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
        return ClasspathHandler.createURLStreamHandler(protocol)
    }
}
