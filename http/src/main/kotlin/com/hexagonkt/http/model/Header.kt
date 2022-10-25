package com.hexagonkt.http.model

data class Header(
    override val name: String,
    override val values: List<String>,
) : HttpField {

    override val value: String? = values.firstOrNull()

    constructor(name: String, vararg values: Any) : this(name, values.map(Any::toString))

    override operator fun plus(value: Any): HttpField =
        copy(values = values + value.toString())

    override operator fun minus(element: Any): HttpField =
        copy(values = values - element.toString())
}
