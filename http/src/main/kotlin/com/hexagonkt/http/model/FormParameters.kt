package com.hexagonkt.http.model

data class FormParameters(
    val httpFields: Map<String, FormParameter>
) : Map<String, FormParameter> by httpFields {

    constructor(fields: List<FormParameter>) : this(fields.associateBy { it.name })

    constructor(vararg fields: FormParameter) : this(fields.toList())

    operator fun plus(element: FormParameter): FormParameters =
        copy(httpFields = httpFields + (element.name to element))

    operator fun plus(element: FormParameters): FormParameters =
        copy(httpFields = httpFields + element.httpFields)

    operator fun minus(name: String): FormParameters =
        copy(httpFields = httpFields - name)
}
