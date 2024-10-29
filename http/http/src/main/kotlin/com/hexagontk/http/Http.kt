package com.hexagontk.http

import com.hexagontk.core.GMT_ZONE
import com.hexagontk.core.Platform
import com.hexagontk.core.media.MediaType
import com.hexagontk.core.text.encodeToBase64
import com.hexagontk.http.model.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.time.*
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

internal val HTTP_DATE_FORMATTER: DateTimeFormatter by lazy { RFC_1123_DATE_TIME.withZone(UTC) }

fun basicAuth(user: String, password: String = ""): String =
    "$user:$password".encodeToBase64()

/**
 * Parse query string such as `paramA=valueA&paramB=valueB` into a map of several key-value pairs
 * separated by '&' where *key* is the param name before '=' as String and *value* is the string
 * after '=' as a list of String (as a query parameter may have many values).
 *
 * Note: Missing the '=' sign, or missing value after '=' (e.g. `foo=` or `foo`) will result into an
 * empty string value.
 *
 * @param query URL query string. E.g.: `param=value&foo=bar`.
 * @return Map with query parameter keys bound to a list with their values.
 *
 */
fun parseQueryString(query: String): Parameters =
    if (query.isBlank())
        Parameters()
    else
        Parameters(
            query
                .split("&".toRegex())
                .map {
                    val keyValue = it.split("=").map(String::trim)
                    val key = keyValue[0]
                    val value = if (keyValue.size == 2) keyValue[1] else ""
                    key.urlDecode() to value.urlDecode()
                }
                .filter { it.first.isNotBlank() }
                .map { (k, v) -> Field(k, v) }
        )

fun formatQueryString(parameters: Parameters): String =
    parameters
        .filter { it.name.isNotBlank() }
        .joinToString("&") {
            if (it.text.isBlank()) it.name.urlEncode()
            else "${it.name.urlEncode()}=${it.text.urlEncode()}"
        }

fun String.urlDecode(): String =
    URLDecoder.decode(this, Platform.charset)

fun String.urlEncode(): String =
    URLEncoder.encode(this, Platform.charset)

fun LocalDateTime.toHttpFormat(): String =
    HTTP_DATE_FORMATTER
        .format(ZonedDateTime.of(this, Platform.zoneId).withZoneSameInstant(GMT_ZONE))

fun Instant.toHttpFormat(): String =
    HTTP_DATE_FORMATTER.format(this)

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
