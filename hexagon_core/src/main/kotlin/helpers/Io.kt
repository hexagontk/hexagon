package com.hexagonkt.helpers

import java.io.InputStream
import java.lang.ClassLoader.getSystemClassLoader
import java.net.URL

/** System class loader. */
val systemClassLoader: ClassLoader = getSystemClassLoader()

fun resourceAsStream(resource: String): InputStream? =
    systemClassLoader.getResourceAsStream(resource)

fun resource(resource: String): URL? = systemClassLoader.getResource(resource)

fun requireResource(resource: String): URL = resource(resource) ?: error("$resource not found")

fun readResource(resource: String): String? = resourceAsStream(resource)?.reader()?.readText()
