package com.hexagonkt

import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.spi.URLStreamHandlerProvider

internal class ClasspathHandlerProvider : URLStreamHandlerProvider() {

    companion object {
        private val provider = ClasspathHandlerProvider()

        fun registerHandler() {
            URL.setURLStreamHandlerFactory {
                provider.createURLStreamHandler(it)
            }
        }
    }

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
