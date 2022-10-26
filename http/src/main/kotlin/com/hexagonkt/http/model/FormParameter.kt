package com.hexagonkt.http.model

data class FormParameter(
    override val name: String,
    override val values: List<String>,
) : HttpField {

    override val value: String? = values.firstOrNull()

    constructor(name: String, vararg values: Any) : this(name, values.map(Any::toString))

    override operator fun plus(value: Any): FormParameter =
        copy(values = values + value.toString())

    override operator fun minus(element: Any): FormParameter =
        copy(values = values - element.toString())
}
