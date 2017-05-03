package co.there4.hexagon.web

data class Route (
    val path: Path,
    val method: Set<HttpMethod>) {

    constructor(path: Path, vararg methods: HttpMethod) : this(path, setOf(*methods))
}
