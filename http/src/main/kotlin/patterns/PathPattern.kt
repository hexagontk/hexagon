package com.hexagonkt.http.patterns

interface PathPattern {

    val pattern: String
    val prefix: Boolean

    fun addPrefix(prefix: String?): PathPattern

    fun matches(requestUrl: String): Boolean

    fun extractParameters(requestUrl: String): Map<String, String>

    fun describe(): String =
        "${javaClass.simpleName}${if (prefix) " (PREFIX)" else ""} $pattern"
}
