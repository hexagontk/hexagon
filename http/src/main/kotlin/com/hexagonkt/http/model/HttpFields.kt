package com.hexagonkt.http.model

/**
 * TODO
 * Container for headers, query parameters and form parameters
 */
data class HttpFields<T : HttpField>(
    val httpFields: Map<String, T>
) : Map<String, T> by httpFields {

    constructor(fields: List<T>) : this(fields.associateBy { it.name })

    constructor(vararg fields: T) : this(fields.toList())

    operator fun plus(element: T): HttpFields<T> =
        copy(httpFields = httpFields + (element.name to element))
}
