package com.hexagonkt.helpers

import java.io.InputStream
import java.lang.ClassLoader.getSystemClassLoader
import java.net.URL

/**
 * Absolute resource (start from root package without starting slash). Ie: foo/bar/res.txt instead
 * /foo/bar/res.txt
 */
class Resource(val path: String) {

    private companion object {

        /** System class loader. */
        private val systemClassLoader: ClassLoader = getSystemClassLoader()
    }

    fun stream(): InputStream? = systemClassLoader.getResourceAsStream(path)

    fun requireStream() = stream() ?: error("$path not found")

    fun url(): URL? = systemClassLoader.getResource(path)

    fun requireUrl(): URL = url() ?: error("$path not found")

    fun readText(): String? = stream()?.reader()?.readText()
}
