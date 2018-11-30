package com.hexagonkt.helpers

import java.io.InputStream
import java.net.URL

/**
 * Absolute resource (start from root package without starting slash). Ie: foo/bar/res.txt instead
 * /foo/bar/res.txt
 */
class Resource(val path: String) {

    private companion object {

        /** Thread class loader. Used over System Class Loader because for JEE servers support. */
        private val threadClassLoader: ClassLoader = Thread.currentThread().contextClassLoader
    }

    fun stream(): InputStream? = threadClassLoader.getResourceAsStream(path)

    fun requireStream() = stream() ?: error("$path not found")

    fun url(): URL? = threadClassLoader.getResource(path)

    fun requireUrl(): URL = url() ?: error("$path not found")

    fun readText(): String? = stream()?.reader()?.readText()
}
