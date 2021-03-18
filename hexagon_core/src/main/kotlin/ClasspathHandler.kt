package com.hexagonkt

import com.hexagonkt.logging.Logger
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

object ClasspathHandler : URLStreamHandler() {

    private val logger: Logger by lazy { Logger(this::class) }
    private val classLoader: ClassLoader by lazy { Thread.currentThread().contextClassLoader }
    private val protocolHandlers: Map<String, URLStreamHandler> by lazy {
        mapOf("classpath" to this)
    }

    override fun openConnection(url: URL): URLConnection =
        classLoader.getResource(url.path)?.openConnection()
            ?: throw ResourceNotFoundException("$url cannot be open")

    fun registerHandler() {
        try {
            URL.setURLStreamHandlerFactory {
                createURLStreamHandler(it)
            }
        }
        catch (e: Error) {
            logger.debug { "Classpath URL handler already registered" }
        }
    }

    fun createURLStreamHandler(protocol: String): URLStreamHandler? {
        return protocolHandlers[protocol]
    }
}
