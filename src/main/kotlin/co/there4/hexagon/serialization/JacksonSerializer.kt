package co.there4.hexagon.serialization

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.InputStream
import kotlin.reflect.KClass
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER

object JacksonSerializer {
    val mapper = createObjectMapper ()

    /** List of formats. NOTE should be defined AFTER mapper definition to avoid runtime issues. */
    private val formatList = listOf (
        JacksonSerializationFormat("json"),
        JacksonSerializationFormat("yaml") {
            with(YAMLFactory()) { configure(WRITE_DOC_START_MARKER, false) }
        }
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

    fun <T: Any> parse(input: InputStream, type: KClass<T>, contentType: String = defaultFormat) =
        getSerializationFormat (contentType).parse (input, type)

    fun <T: Any> parseList(
        input: InputStream, type: KClass<T>, contentType: String = defaultFormat) =
            getSerializationFormat (contentType).parseList (input, type)
}
