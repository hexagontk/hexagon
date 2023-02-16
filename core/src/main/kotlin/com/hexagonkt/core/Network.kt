package com.hexagonkt.core

import java.io.File
import java.net.*
import java.util.*

/** Internet address used to bind services to all local network interfaces. */
val ALL_INTERFACES: InetAddress = inetAddress(0, 0, 0, 0)

/** Internet address used to bind services to the loopback interface. */
val LOOPBACK_INTERFACE: InetAddress = inetAddress(127, 0, 0, 1)

/**
 * Syntactic sugar to create an Internet address.
 *
 * @param bytes Bytes used in the address.
 * @return The Internet address corresponding with the supplied bytes.
 */
fun inetAddress(vararg bytes: Byte): InetAddress =
    InetAddress.getByAddress(bytes)

/**
 * Return a random free port (not used by any other local process).
 *
 * @return Random free port number.
 */
fun freePort(): Int =
    ServerSocket(0).use { it.localPort }

/**
 * Check if a port is already opened.
 *
 * @param port Port number to check.
 * @return True if the port is open, false otherwise.
 */
fun isPortOpened(port: Int): Boolean =
    try {
        Socket("localhost", port).use { it.isConnected }
    }
    catch (e: Exception) {
        false
    }

fun URL.responseCode(): Int =
    try {
        (openConnection() as HttpURLConnection).responseCode
    }
    catch (e: java.lang.Exception) {
        400
    }

fun URL.responseSuccessful(): Boolean =
    responseCode() in 200 until 300

fun URL.responseFound(): Boolean =
    responseCode().let { it in 200 until 500 && it != 404 }

// TODO Review the next functions, not all cases are covered
fun URL.exists(): Boolean =
    when (protocol) {
        "http" -> responseSuccessful()
        "https" -> responseSuccessful()
        "file" -> File(file).let { it.exists() && !it.isDirectory }
        "classpath" -> try { openConnection(); true } catch (_: Exception) { false }
        else -> false
    }

fun URL.firstVariant(vararg suffixes: String): URL {
    val extension = file.substringAfter('.')
    val fileName = file.removeSuffix(".$extension")

    suffixes.forEach {
        val u = URL("$protocol:$fileName$it.$extension")
        if (u.exists())
            return u
    }

    return this
}

fun URL.localized(locale: Locale): URL {
    val language = locale.language
    val country = locale.country
    return firstVariant("_${language}_$country", "_${language}")
}
