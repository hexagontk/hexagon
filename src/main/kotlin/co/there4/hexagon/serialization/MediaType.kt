package co.there4.hexagon.serialization

data class MediaType (
    val type: Type,
    val subtype: String,
    val parameters: Map<String, String>) {

    enum class Type {

    }

    init {
        require(subtype.isNotBlank())
    }

    fun withCharset(charset: String) = copy(parameters = parameters + ("charset" to charset))
}
