package com.hexagonkt.helpers

import java.nio.ByteBuffer
import java.util.*

private val encoder = Base64.getEncoder().withoutPadding()
private val decoder = Base64.getDecoder()

fun UUID.bytes(): ByteArray =
    ByteBuffer.wrap(ByteArray(16)).let {
        it.putLong(this.mostSignificantBits)
        it.putLong(this.leastSignificantBits)
        it.array()
    }

fun UUID.toBase64(): String =
    encoder.encodeToString(this.bytes())

fun uuid(text: String): UUID =
    if (text[8] == '-') UUID.fromString(text)
    else uuid(decoder.decode(text))

fun uuid(bytes: ByteArray) =
    ByteBuffer.wrap(bytes).let { UUID(it.long, it.long) }
