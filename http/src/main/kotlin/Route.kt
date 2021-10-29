package com.hexagonkt.http

data class Route(val path: Path, val methods: LinkedHashSet<Method>) {

    constructor(path: Path, vararg methods: Method) : this(path, linkedSetOf(*methods))

    constructor(path: String, vararg methods: Method) : this(Path(path), linkedSetOf(*methods))

    fun list(): List<Route> = methods.map {
        Route(path, linkedSetOf(it))
    }
}
