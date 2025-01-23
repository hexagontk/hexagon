package com.hexagontk.helpers

import com.hexagontk.core.text.decodeBase64
import com.hexagontk.core.text.encodeToBase64
import java.nio.ByteBuffer
import java.util.*

/**
 * .
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
 * .
 *
 * @receiver .
 * @return .
 */
fun UUID.toBase64(): String =
    bytes().encodeToBase64()

/**
 * .
 *
 * @param text .
 * @return .
 */
fun uuid(text: String): UUID =
    if (text[8] == '-') UUID.fromString(text)
    else uuid(text.decodeBase64())

/**
 * .
 *
 * @param bytes .
 * @return .
 */
fun uuid(bytes: ByteArray): UUID =
    ByteBuffer.wrap(bytes).let { UUID(it.long, it.long) }
