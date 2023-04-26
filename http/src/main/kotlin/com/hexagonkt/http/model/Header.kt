package com.hexagonkt.http.model

data class Header(
    override val name: String,
    override val values: List<Any>,
) : HttpField {

    override val value: Any? = values.firstOrNull()

    constructor(name: String, vararg values: Any) : this(name, values.map(Any::toString))

    override operator fun plus(value: Any): Header =
        copy(values = values + value.toString())

    override operator fun minus(element: Any): Header =
        copy(values = values - element.toString())
}
