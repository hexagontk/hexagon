package com.hexagontk.http.model

/**
 * Case-sensitive HTTP fields.
 */
class Parameters(fields: List<HttpField>) : HttpFields(fields, { it }) {

    constructor(vararg fields: HttpField) : this(fields.toList())

    operator fun plus(element: HttpField): Parameters =
        Parameters(fields = fields + (element))

    operator fun plus(element: Parameters): Parameters =
        Parameters(fields = fields + element.fields)

    operator fun minus(name: String): Parameters =
        name.let(keyMapper).let { n ->
            Parameters(fields = fields.filter { it.name != n })
        }
}
