package com.hexagonkt.core.security

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun hmac(algorithm: String, data: ByteArray, key: ByteArray): ByteArray {
    val mac = Mac.getInstance(algorithm)
    mac.init(SecretKeySpec(key, algorithm))
    return mac.doFinal(data)
}

fun chainHmac(algorithm: String, vararg fields: String): ByteArray =
    fields
        .map(String::toByteArray)
        .reduce { data, field -> hmac(algorithm, field, data) }

fun hash(algorithm: String, data: ByteArray): ByteArray {
    val messageDigest = MessageDigest.getInstance(algorithm)
    messageDigest.update(data)
    return messageDigest.digest()
}

fun hash(algorithm: String, data: String): ByteArray =
    hash(algorithm, data.toByteArray())

fun sign(algorithm: String, data: String, key: ByteArray): ByteArray {
    val mac = Mac.getInstance(algorithm)
    mac.init(SecretKeySpec(key, algorithm))
    return mac.doFinal(data.toByteArray())
}
