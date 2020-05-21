package com.hexagonkt.http

import com.hexagonkt.helpers.Jvm.charset
import com.hexagonkt.http.Method.GET
import com.hexagonkt.http.Method.HEAD
import com.hexagonkt.http.Method.POST
import com.hexagonkt.http.Method.PUT
import com.hexagonkt.http.Method.DELETE
import com.hexagonkt.http.Method.TRACE
import com.hexagonkt.http.Method.OPTIONS
import com.hexagonkt.http.Method.PATCH
import java.net.URLDecoder
import java.net.URLEncoder

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

/** Set containing all HTTP methods. */
val ALL: LinkedHashSet<Method> by lazy { linkedSetOf(*Method.values()) }

/** Shortcut to create a route for a filter (with all methods). */
fun any(path: String = "/"): Route = Route(Path(path), ALL)

/** Shortcut to create a GET route. */
fun get(path: String = "/"): Route = Route(path, GET)

/** Shortcut to create a HEAD route. */
fun head(path: String = "/"): Route = Route(path, HEAD)

/** Shortcut to create a POST route. */
fun post(path: String = "/"): Route = Route(path, POST)

/** Shortcut to create a PUT route. */
fun put(path: String = "/"): Route = Route(path, PUT)

/** Shortcut to create a DELETE route. */
fun delete(path: String = "/"): Route = Route(path, DELETE)

/** Shortcut to create a TRACE route. */
fun tracer(path: String = "/"): Route = Route(path, TRACE)

/** Shortcut to create a OPTIONS route. */
fun options(path: String = "/"): Route = Route(path, OPTIONS)

/** Shortcut to create a PATCH route. */
fun patch(path: String = "/"): Route = Route(path, PATCH)

fun parseQueryParameters (query: String): Map<String, List<String>> =
    if (query.isBlank())
        mapOf()
    else
        query.split("&".toRegex())
            .map {
                val keyValue = it.split("=").map(String::trim)
                val key = keyValue[0]
                val value = if (keyValue.size == 2) keyValue[1] else ""
                key.urlDecode() to value.urlDecode()
            }
            .groupBy { it.first }
            .mapValues { pair -> pair.value.map { it.second } }
            .mapValues { if (it.value == listOf("")) emptyList() else it.value }

fun httpDate (date: LocalDateTime = LocalDateTime.now()): String =
    RFC_1123_DATE_TIME.format(ZonedDateTime.of(date, ZoneId.of("GMT")))

fun String.urlDecode(): String = URLDecoder.decode(this, charset.name())

fun String.urlEncode(): String = URLEncoder.encode(this, charset.name())

