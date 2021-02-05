package com.hexagonkt

import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

object ClasspathHandler : URLStreamHandler() {

    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
    private val protocolHandlers: Map<String, URLStreamHandler> = mapOf("classpath" to this)

    override fun openConnection(url: URL): URLConnection =
        classLoader.getResource(url.path)?.openConnection()
            ?: throw ResourceNotFoundException("$url cannot be open")

    fun registerHandler() {
        URL.setURLStreamHandlerFactory {
            createURLStreamHandler(it)
        }
    }

    fun createURLStreamHandler(protocol: String): URLStreamHandler? {
        return protocolHandlers[protocol]
    }
}
