package com.hexagonkt.http.patterns

interface PathPattern {

    val pattern: String
    val prefix: Boolean

    fun addPrefix(prefix: String?): PathPattern

    fun matches(requestUrl: String): Boolean

    fun extractParameters(requestUrl: String): Map<String, String>

    fun insertParameters(parameters: Map<String, Any>): String =
        error("${type()} path pattern does not accept parameters")

    fun describe(): String =
        "${type()}${if (prefix) " (PREFIX)" else ""} '$pattern'"

    private fun type(): String =
        javaClass.simpleName.removeSuffix(typeSuffix)

    private companion object {
        val typeSuffix: String = PathPattern::class.java.simpleName
    }
}
