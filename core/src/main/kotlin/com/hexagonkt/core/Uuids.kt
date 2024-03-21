package com.hexagonkt.core

import com.hexagonkt.core.text.decodeBase64
import com.hexagonkt.core.text.encodeToBase64
import java.nio.ByteBuffer
import java.util.*

/**
 * [TODO](https://github.com/hexagontk/hexagon/issues/271).
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
 * [TODO](https://github.com/hexagontk/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun UUID.toBase64(): String =
    bytes().encodeToBase64()

/**
 * [TODO](https://github.com/hexagontk/hexagon/issues/271).
 *
 * @param text .
 * @return .
 */
fun uuid(text: String): UUID =
    if (text[8] == '-') UUID.fromString(text)
    else uuid(text.decodeBase64())

/**
 * [TODO](https://github.com/hexagontk/hexagon/issues/271).
 *
 * @param bytes .
 * @return .
 */
fun uuid(bytes: ByteArray): UUID =
    ByteBuffer.wrap(bytes).let { UUID(it.long, it.long) }
