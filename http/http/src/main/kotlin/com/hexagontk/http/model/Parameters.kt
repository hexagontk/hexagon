package com.hexagontk.http.model

import com.hexagontk.core.require

/**
 * TODO Rename to 'Parameters' (case sensitive)
 */
data class Parameters(val fields: List<HttpField>) : List<HttpField> by fields {
    val all: Map<String, List<HttpField>> by lazy { fields.groupBy { it.name } }
    val values: Map<String, HttpField> by lazy { fields.reversed().associateBy { it.name } }

    val keys: Set<String> by lazy { fields.map { it.name }.toSet() }

    constructor(vararg fields: HttpField) : this(fields.toList())

    operator fun plus(element: HttpField): Parameters =
        Parameters(fields = fields + (element))

    operator fun plus(element: Parameters): Parameters =
        Parameters(fields = fields + element.fields)

    operator fun minus(name: String): Parameters =
        name.let { n ->
            Parameters(fields = fields.filter { it.name != n })
        }

    operator fun get(key: String): HttpField? =
        values[key]

    fun all(key: String): List<HttpField> =
        all[key] ?: emptyList()

    fun require(key: String): HttpField =
        values.require(key)
}
