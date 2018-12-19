package com.hexagonkt.http

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

fun parseQueryParameters (query: String): Map<String, String> =
    if (query.isBlank())
        mapOf()
    else
        query.split("&".toRegex())
            .map {
                val keyValue = it.split("=").map(String::trim)
                val key = keyValue[0]
                val value = if (keyValue.size == 2) keyValue[1] else ""
                key to value
            }
            .toMap(LinkedHashMap())

fun httpDate (date: LocalDateTime = LocalDateTime.now()): String =
    RFC_1123_DATE_TIME.format(ZonedDateTime.of(date, ZoneId.of("GMT")))
