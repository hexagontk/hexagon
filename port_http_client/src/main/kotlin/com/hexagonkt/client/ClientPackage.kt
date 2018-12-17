package com.hexagonkt.client

import com.hexagonkt.http.Method.*

internal val http: Client = Client()

fun get (url: String, callHeaders: Map<String, List<String>> = LinkedHashMap()) =
    http.send (GET, url, null, callHeaders = callHeaders)

fun head (url: String, callHeaders: Map<String, List<String>> = LinkedHashMap()) =
    http.send (HEAD, url, null, callHeaders = callHeaders)

fun post (url: String, body: Any? = null, contentType: String? = http.contentType) =
    http.send (POST, url, body, contentType)

fun put (url: String, body: Any? = null, contentType: String? = http.contentType) =
    http.send (PUT, url, body, contentType)

fun delete (url: String, body: Any? = null, contentType: String? = http.contentType) =
    http.send (DELETE, url, body, contentType)

fun trace (url: String, body: Any? = null, contentType: String? = http.contentType) =
    http.send (TRACE, url, body, contentType)

fun options (url: String, body: Any? = null, contentType: String? = http.contentType) =
    http.send (OPTIONS, url, body, contentType)

fun patch (url: String, body: Any? = null, contentType: String? = http.contentType) =
    http.send (PATCH, url, body, contentType)
