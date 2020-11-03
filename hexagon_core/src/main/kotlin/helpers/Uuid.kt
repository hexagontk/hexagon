package com.hexagonkt.helpers

import java.nio.ByteBuffer
import java.util.*

private val encoder = Base64.getEncoder().withoutPadding()
private val decoder = Base64.getDecoder()

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun UUID.bytes(): ByteArray =
    ByteBuffer.wrap(ByteArray(16)).let {
        it.putLong(this.mostSignificantBits)
        it.putLong(this.leastSignificantBits)
        it.array()
    }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun UUID.toBase64(): String =
    encoder.encodeToString(this.bytes())

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param text .
 * @return .
 */
fun uuid(text: String): UUID =
    if (text[8] == '-') UUID.fromString(text)
    else uuid(decoder.decode(text))

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param bytes .
 * @return .
 */
fun uuid(bytes: ByteArray): UUID =
    ByteBuffer.wrap(bytes).let { UUID(it.long, it.long) }
