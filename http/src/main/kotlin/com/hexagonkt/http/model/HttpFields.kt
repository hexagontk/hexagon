package com.hexagonkt.http.model

data class HttpFields<T : HttpField>(
    val httpFields: Map<String, T>
) : Map<String, T> by httpFields {

    constructor(fields: List<T>) : this(fields.associateBy { it.name })

    constructor(vararg fields: T) : this(fields.toList())

    operator fun plus(element: T): HttpFields<T> =
        copy(httpFields = httpFields + (element.name to element))

    operator fun plus(element: HttpFields<T>): HttpFields<T> =
        copy(httpFields = httpFields + element.httpFields)

    operator fun minus(name: String): HttpFields<T> =
        copy(httpFields = httpFields - name)
}

//data class HttpHeaders(
//    val httpFields: Map<String, Header>
//) : Map<String, Header> by httpFields {
//
//    constructor(fields: List<Header>) : this(fields.associateBy { it.name.lowercase() })
//
//    constructor(vararg fields: Header) : this(fields.toList())
//
//    operator fun plus(element: Header): HttpHeaders =
//        copy(httpFields = httpFields + (element.name to element))
//
//    operator fun plus(element: HttpHeaders): HttpHeaders =
//        copy(httpFields = httpFields + element.httpFields)
//
//    operator fun minus(name: String): HttpHeaders =
//        copy(httpFields = httpFields - name.lowercase())
//
//    override operator fun get(key: String): Header? =
//        httpFields[key.lowercase()]
//}
