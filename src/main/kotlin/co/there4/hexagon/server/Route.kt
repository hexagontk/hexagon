package co.there4.hexagon.server

data class Route (
    val path: Path,
    val method: Set<HttpMethod>) {

    constructor(path: Path, vararg methods: HttpMethod) : this(path, setOf(*methods))
}
