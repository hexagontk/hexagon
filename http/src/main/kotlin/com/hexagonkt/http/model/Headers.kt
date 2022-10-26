package com.hexagonkt.http.model

data class Headers(
    val httpFields: Map<String, Header>
) : Map<String, Header> by httpFields {

    constructor(fields: List<Header>) : this(fields.associateBy { it.name.lowercase() })

    constructor(vararg fields: Header) : this(fields.toList())

    operator fun plus(element: Header): Headers =
        copy(httpFields = httpFields + (element.name to element))

    operator fun plus(element: Headers): Headers =
        copy(httpFields = httpFields + element.httpFields)

    operator fun minus(name: String): Headers =
        copy(httpFields = httpFields - name.lowercase())

    override operator fun get(key: String): Header? =
        httpFields[key.lowercase()]
}
