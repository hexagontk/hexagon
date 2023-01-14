package com.hexagonkt.core.security

import kotlin.test.Test

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertContentEquals

internal class CryptoTest {

    private val signatureKeyPrefix: String = "AWS4"
    private val hmacAlgorithm: String = "HmacSHA256"
    private val awsRequest: String = "aws4_request"
    private val dateFormat: String = "yyyyMMdd"
    private val dateFormatter = DateTimeFormatter.ofPattern(dateFormat)

    @Test fun `Test chained HMACs`() {
        val key = "key"
        val date = LocalDateTime.now()
        val formattedDate = date.format(dateFormatter)
        val region = "region"
        val service = "service"

        val signatureKey = signatureKey(key, date, region, service)
        val hmac =
            chainHmac("HmacSHA256", "AWS4$key", formattedDate, region, service, "aws4_request")

        assert(hmac.contentEquals(signatureKey))
    }

    @Test fun `'sign' work as a helper function for 'hmac'`() {
        val algorithm = "HmacSHA256"
        val data = "the data"
        val key = "a key".toByteArray()
        assertContentEquals(sign(algorithm, data, key), hmac(algorithm, data.toByteArray(), key))
    }

    @Test fun `Hashes work properly`() {
        val algorithm = "SHA-256"
        val data = "the data"
        assertContentEquals(hash(algorithm, data), hash(algorithm, data.toByteArray()))
    }

    @Suppress("SameParameterValue")
    private fun signatureKey(
        key: String, date: LocalDateTime, region: String, service: String
    ): ByteArray =
        chainHmac(
            hmacAlgorithm,
            signatureKeyPrefix + key,
            date.format(dateFormatter),
            region,
            service,
            awsRequest
        )
}
