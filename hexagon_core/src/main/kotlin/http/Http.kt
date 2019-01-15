package com.hexagonkt.http

import com.hexagonkt.helpers.Jvm.charset
import com.hexagonkt.http.Method.*
import java.net.URLDecoder
import java.net.URLEncoder

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

/** Set containing all HTTP methods. */
val ALL: LinkedHashSet<Method> by lazy { linkedSetOf(*Method.values()) }

/** Shortcut to create a route for a filter (with all methods). */
fun all(path: String = "/"): Route = Route(Path(path), ALL)

/** Shortcut to create a GET route. */
fun get(path: String = "/"): Route = Route(Path(path), GET)

/** Shortcut to create a HEAD route. */
fun head(path: String = "/"): Route = Route(Path(path), HEAD)

/** Shortcut to create a POST route. */
fun post(path: String = "/"): Route = Route(Path(path), POST)

/** Shortcut to create a PUT route. */
fun put(path: String = "/"): Route = Route(Path(path), PUT)

/** Shortcut to create a DELETE route. */
fun delete(path: String = "/"): Route = Route(Path(path), DELETE)

/** Shortcut to create a TRACE route. */
fun tracer(path: String = "/"): Route = Route(Path(path), TRACE)

/** Shortcut to create a OPTIONS route. */
fun options(path: String = "/"): Route = Route(Path(path), OPTIONS)

/** Shortcut to create a PATCH route. */
fun patch(path: String = "/"): Route = Route(Path(path), PATCH)

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

fun String.urlDecode() = URLDecoder.decode(this, charset.name())

fun String.urlEncode() = URLEncoder.encode(this, charset.name())

