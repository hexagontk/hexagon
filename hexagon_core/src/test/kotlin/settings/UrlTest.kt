package com.hexagonkt.settings

import org.junit.jupiter.api.Test
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory

private class ConfigurableStreamHandlerFactory(
    protocol: String,
    urlHandler: URLStreamHandler,
) : URLStreamHandlerFactory {

    private val protocolHandlers: MutableMap<String, URLStreamHandler> = HashMap()
//    private val defaultFactory = URL.D

    fun addHandler(protocol: String, urlHandler: URLStreamHandler) {
        protocolHandlers[protocol] = urlHandler
    }

    override fun createURLStreamHandler(protocol: String): URLStreamHandler {
        return protocolHandlers[protocol]!!
    }

    init {
        addHandler(protocol, urlHandler)
    }
}

private class Handler(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
) : URLStreamHandler() {

    override fun openConnection(u: URL): URLConnection? {
        val resourceUrl: URL? = classLoader.getResource(u.path)
        return resourceUrl?.openConnection()
    }
}

class UrlTest {

    @Test fun `Resource loading using URL`() {
//        URL.setURLStreamHandlerFactory(ConfigurableStreamHandlerFactory("classpath", Handler()))
        println(">>>>>>>>>>>>>>>>>>>>>>>> " + URL("classpath:application_test.yml").readText())
        println(">>>>>>>>>>>>>>>>>>>>>>>> " + URL("file:README.md").readText())
        println(">>>>>>>>>>>>>>>>>>>>>>>> " + URL("https://hexagonkt.com/index.html").readText())
    }
}
