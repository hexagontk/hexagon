package com.hexagonkt.http.model

import com.hexagonkt.core.require

data class HttpFields<T : HttpField>(
    val httpFields: Map<String, T>
) {

    constructor(fields: List<T>) : this(fields.associateBy { it.name })

    constructor(vararg fields: T) : this(fields.toList())

    operator fun plus(element: T): HttpFields<T> =
        copy(httpFields = httpFields + (element.name to element))

    val allPairs: List<Pair<String, String>> by lazy {
        httpFields.flatMap { (k, v) -> v.values.map { k to it } }
    }

    val allValues: Map<String, List<String>> by lazy {
        httpFields.mapValues { it.value.values }
    }

    val values: Map<String, String> by lazy {
        httpFields
            .filterValues { it.values.isNotEmpty() }
            .mapValues { it.value.values.first() }
    }

    fun isEmpty(): Boolean =
        httpFields.isEmpty()

    operator fun get(key: String): String? =
        httpFields[key]?.values?.firstOrNull()

    fun require(key: String): String =
        httpFields.require(key).values.first()
}
