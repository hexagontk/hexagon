package co.there4.hexagon.server

import kotlin.reflect.KClass

data class Route (
    val path: Path,
    val method: Set<HttpMethod>,
    val requestType: KClass<*>? = null,
    val responseType: KClass<*>? = null,
    val metadata: Map<String, *> = emptyMap<String, Any>()) {

    constructor(path: Path, vararg methods: HttpMethod) : this(path, setOf(*methods))
}
