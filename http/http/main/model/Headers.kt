package com.hexagontk.http.model

/**
 * Case-insensitive.
 */
class Headers(fields: List<HttpField>) : HttpFields(fields, String::lowercase) {

    constructor(vararg fields: HttpField) : this(fields.toList())

    operator fun plus(element: HttpField): Headers =
        Headers(fields = fields + (element))

    operator fun plus(element: Headers): Headers =
        Headers(fields = fields + element.fields)

    operator fun minus(name: String): Headers =
        name.let(keyMapper).let { n ->
            Headers(fields = fields.filter { it.name != n })
        }
}
