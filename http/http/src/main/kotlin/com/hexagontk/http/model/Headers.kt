package com.hexagontk.http.model

import com.hexagontk.core.require

/**
 * Case-insensitive.
 */
data class Headers(val fields: List<HttpField>) : List<HttpField> by fields {
    val all: Map<String, List<HttpField>> by lazy { fields.groupBy { it.name.lowercase() } }
    val values: Map<String, HttpField> by lazy { fields.reversed().associateBy { it.name.lowercase() } }

    val keys: Set<String> by lazy { fields.map { it.name }.toSet() }

    constructor(vararg fields: HttpField) : this(fields.toList())

    operator fun plus(element: HttpField): Headers =
        Headers(fields = fields + (element))

    operator fun plus(element: Headers): Headers =
        Headers(fields = fields + element.fields)

    operator fun minus(name: String): Headers =
        name.lowercase().let { n ->
            Headers(fields = fields.filter { it.name != n })
        }

    operator fun get(key: String): HttpField? =
        values[key.lowercase()]

    fun all(key: String): List<HttpField> =
        all[key.lowercase()] ?: emptyList()

    fun require(key: String): HttpField =
        values.require(key.lowercase())
}
