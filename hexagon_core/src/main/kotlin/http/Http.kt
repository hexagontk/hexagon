package com.hexagonkt.http

fun parseQueryParameters (query: String): Map<String, String> =
    if (query.isBlank())
        mapOf()
    else
        query.split("&".toRegex())
            .map {
                val kv = it.split("=")
                kv[0].trim () to (if (kv.size == 2) kv[1].trim() else "")
            }
            .toMap(LinkedHashMap())
