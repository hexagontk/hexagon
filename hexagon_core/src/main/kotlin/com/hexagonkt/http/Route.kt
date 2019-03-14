package com.hexagonkt.http

import kotlin.reflect.KClass

data class Route(
    val path: Path,
    val methods: LinkedHashSet<Method>,
    val requestType: KClass<*>? = null,
    val responseType: KClass<*>? = null,
    val metadata: Map<String, *> = emptyMap<String, Any>()) {

    constructor(path: Path, vararg methods: Method) : this(path, linkedSetOf(*methods))

    constructor(path: String, vararg methods: Method) : this(Path(path), linkedSetOf(*methods))

    fun list(): List<Route> = methods.map {
        Route(path, linkedSetOf(it), requestType, responseType, metadata)
    }
}
