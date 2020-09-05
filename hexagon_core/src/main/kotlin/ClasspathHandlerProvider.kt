package com.hexagonkt

import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.spi.URLStreamHandlerProvider

internal class ClasspathHandlerProvider : URLStreamHandlerProvider() {

    private class Handler(
        private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
    ) : URLStreamHandler() {

        override fun openConnection(u: URL): URLConnection {

            val resourceUrl: URL = classLoader.getResource(u.path)
                ?: error("${u.path} cannot be open")

            return resourceUrl.openConnection()
        }
    }

    private val protocolHandlers: Map<String, URLStreamHandler> = mapOf("classpath" to Handler())

    override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
        return protocolHandlers[protocol]
    }
}
