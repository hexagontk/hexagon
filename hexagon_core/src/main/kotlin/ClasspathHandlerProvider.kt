package com.hexagonkt

import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.spi.URLStreamHandlerProvider

internal class ClasspathHandlerProvider : URLStreamHandlerProvider() {

    private class Handler(
        private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
    ) : URLStreamHandler() {

        override fun openConnection(url: URL): URLConnection =
            classLoader.getResource(url.path)?.openConnection()
                ?: throw ResourceNotFoundException("$url cannot be open")
    }

    private val protocolHandlers: Map<String, URLStreamHandler> = mapOf("classpath" to Handler())

    override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
        return protocolHandlers[protocol]
    }
}
