package com.hexagonkt.security

import org.junit.jupiter.api.Test

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
