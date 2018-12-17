package com.hexagonkt.http

import kotlin.reflect.KClass

data class Route(
    val path: Path,
    val methods: LinkedHashSet<HttpMethod>,
    val requestType: KClass<*>? = null,
    val responseType: KClass<*>? = null,
    val metadata: Map<String, *> = emptyMap<String, Any>()) {

    constructor(path: Path, vararg methods: HttpMethod) : this(path, linkedSetOf(*methods))
}
