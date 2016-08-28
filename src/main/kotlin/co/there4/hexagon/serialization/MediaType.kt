package co.there4.hexagon.serialization

enum class Type {

}

data class MediaType (
    val type: String,
    val subtype: String,
    val parameters: Map<String,String>) {

    init {
        require(type.isNotBlank())
        require(subtype.isNotBlank())
    }

    fun withCharset(charset: String) = copy(parameters = parameters + ("charset" to charset))
}
