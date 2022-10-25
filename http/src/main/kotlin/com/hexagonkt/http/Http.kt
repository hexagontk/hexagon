package com.hexagonkt.http

import com.hexagonkt.core.disableChecks
import com.hexagonkt.core.Jvm
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpFields
import com.hexagonkt.http.model.QueryParameter
import java.math.BigInteger
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

val checkedHeaders: List<String> = listOf("content-type", "accept", "set-cookie", "authorization")

val gmtZone: ZoneId = ZoneId.of("GMT")

val httpDateFormatter: DateTimeFormatter = RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC)

fun checkHeaders(headers: HttpFields<Header>) {
    if (disableChecks)
        return

    val headersKeys = headers.httpFields.keys
    val invalidHeaders = checkedHeaders.filter { headersKeys.contains(it) }

    check(invalidHeaders.isEmpty()) {
        val invalidHeadersText = invalidHeaders.joinToString(",") { "'$it'" }

        """
        Special headers should be handled with their respective properties (i.e.: contentType)
        instead setting them in the headers map. Ignored headers: $invalidHeadersText
        """.trimIndent()
    }
}

/**
 * Parse query string such as `paramA=valueA&paramB=valueB` into a map of several key-value pairs
 * separated by '&' where *key* is the param name before '=' as String and *value* is the string
 * after '=' as a list of String (as a query parameter may have many values).
 *
 * Note: Missing the '=' sign, or missing value after '=' (e.g `foo=` or `foo`) will result into an
 * empty string value.
 *
 * @param query URL query string. E.g.: `param=value&foo=bar`.
 * @return Map with query parameter keys bound to a list with their values.
 *
 */
fun parseQueryString(query: String): HttpFields<QueryParameter> =
    if (query.isBlank())
        HttpFields()
    else
        HttpFields(
            query
                .split("&".toRegex())
                .map {
                    val keyValue = it.split("=").map(String::trim)
                    val key = keyValue[0]
                    val value = if (keyValue.size == 2) keyValue[1] else ""
                    key.urlDecode() to value.urlDecode()
                }
                .filter { it.first.isNotBlank() }
                .groupBy { it.first }
                .mapValues { pair -> pair.value.map { it.second } }
                .map { (k, v) -> QueryParameter(k, v) }
        )

fun formatQueryString(parameters: HttpFields<QueryParameter>): String =
    parameters.allPairs
        .filter { it.first.isNotBlank() }
        .joinToString("&") { (k, v) ->
            if (v.isBlank()) k.urlEncode()
            else "${k.urlEncode()}=${v.urlEncode()}"
        }

fun String.urlDecode(): String =
    URLDecoder.decode(this, Jvm.charset.name())

fun String.urlEncode(): String =
    URLEncoder.encode(this, Jvm.charset.name())

fun LocalDateTime.toHttpFormat(): String =
    RFC_1123_DATE_TIME.format(ZonedDateTime.of(this, gmtZone))

fun Instant.toHttpFormat(): String =
    httpDateFormatter.format(this)

fun parseContentType(contentType: String): ContentType {
    val typeParameter = contentType.split(";")
    val fullType = typeParameter.first().trim()
    val mimeType = MediaType(fullType)

    return when (typeParameter.size) {
        1 -> ContentType(mimeType)
        2 -> {
            val parameter = typeParameter.last()
            val nameValue = parameter.split("=")
            if (nameValue.size != 2)
                error("Invalid content type format: $contentType")

            val name = nameValue.first().trim()
            val value = nameValue.last().trim()

            when (name.trim().lowercase()) {
                "boundary" -> ContentType(mimeType, boundary = value)
                "charset" -> ContentType(mimeType, charset = Charset.forName(value))
                "q" -> ContentType(mimeType, q = value.toDouble())
                else -> error("Invalid content type format: $contentType")
            }
        }
        else -> error("Invalid content type format: $contentType")
    }
}

fun bodyToBytes(body: Any): ByteArray =
    when (body) {
        is String -> body.toByteArray()
        is ByteArray -> body
        is Int -> BigInteger.valueOf(body.toLong()).toByteArray()
        is Long -> BigInteger.valueOf(body).toByteArray()
        else -> error("Unsupported body type: ${body.javaClass.simpleName}")
    }
