package co.there4.hexagon.serialization

import java.io.InputStream
import kotlin.reflect.KClass

object JacksonSerializer {
    val mapper = createObjectMapper ()

    /** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
    private val formatList = listOf (
        JacksonJsonFormat (),
        JacksonYamlFormat ()
    )

    private val formats = mapOf (
        *formatList
            .map { it.contentType to it }
            .toTypedArray()
    )

    val contentTypes = formatList.map { it.contentType }

    fun toMap(obj: Any): Map<*, *> =
        mapper.convertValue (obj, Map::class.java) ?: throw IllegalStateException ()

    fun <T : Any> toObject(obj: Map<*, *>, type: KClass<T>): T =
        mapper.convertValue (obj, type.java)

    private fun getSerializationFormat (contentType: String) =
        formats[contentType] ?: throw IllegalArgumentException ("$contentType not found")

    fun serialize(obj: Any, contentType: String = defaultFormat) =
        getSerializationFormat (contentType).serialize(obj)

    fun <T: Any> parse(text: String, type: KClass<T>, contentType: String = defaultFormat) =
        getSerializationFormat (contentType).parse (text, type)

    fun <T: Any> parseList(text: String, type: KClass<T>, contentType: String = defaultFormat) =
        getSerializationFormat (contentType).parseList (text, type)

    fun <T: Any> parse(input: InputStream, type: KClass<T>, contentType: String = defaultFormat) =
        getSerializationFormat (contentType).parse (input, type)

    fun <T: Any> parseList(
        input: InputStream, type: KClass<T>, contentType: String = defaultFormat) =
            getSerializationFormat (contentType).parseList (input, type)

    fun parse(text: String, contentType: String = defaultFormat) =
        parse (text, Map::class, contentType)

    fun parseList(text: String, contentType: String = defaultFormat) =
        parseList (text, Map::class, contentType)

    fun parse(input: InputStream, contentType: String = defaultFormat) =
        parse (input, Map::class, contentType)

    fun parseList(input: InputStream, contentType: String = defaultFormat) =
        parseList (input, Map::class, contentType)
}
