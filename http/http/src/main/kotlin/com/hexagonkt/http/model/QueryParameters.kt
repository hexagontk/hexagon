package com.hexagonkt.http.model

data class QueryParameters(
    val httpFields: Map<String, QueryParameter>
) : Map<String, QueryParameter> by httpFields {

    constructor(fields: List<QueryParameter>) : this(fields.associateBy { it.name })

    constructor(vararg fields: QueryParameter) : this(fields.toList())

    operator fun plus(element: QueryParameter): QueryParameters =
        copy(httpFields = httpFields + (element.name to element))

    operator fun plus(element: QueryParameters): QueryParameters =
        copy(httpFields = httpFields + element.httpFields)

    operator fun minus(name: String): QueryParameters =
        copy(httpFields = httpFields - name)
}
