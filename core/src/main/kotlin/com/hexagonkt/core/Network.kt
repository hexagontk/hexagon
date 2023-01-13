package com.hexagonkt.core

import com.hexagonkt.core.logging.Logger
import java.net.*

private val logger: Logger by lazy { Logger("com.hexagonkt.core.Helpers") }

/** Internet address used to bind services to all local network interfaces. */
val allInterfaces: InetAddress = inetAddress(0, 0, 0, 0)

/** Internet address used to bind services to the loopback interface. */
val loopbackInterface: InetAddress = inetAddress(127, 0, 0, 1)

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
        logger.debug { "Checked port: $port is already open" }
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

fun URL.check(vararg suffixes: String): URL {
    val extension = toString().substringBeforeLast('.')
    val file = extension.removeSuffix(".$extension")
    suffixes.forEach {
        val u = URL("$file$it.$extension")
        try {
            u.openConnection()
            return u
        }
        catch (_: Exception) {
            // continue
        }
    }

    return this
}
// TODO Find existing URL given a set of suffixes
// TODO Use the above to resolve a URL with locale information: foo_en_US.txt, foo_en.txt, foo.txt
