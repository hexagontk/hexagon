package co.there4.hexagon.serialization

import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import java.io.InputStream
import kotlin.reflect.KClass

internal class JacksonYamlFormat : SerializationFormat {
    override val contentType = "application/yaml"

    private val mapper = createObjectMapper(createFactory())
    private val writer = createObjectWriter()

    fun createFactory(): YAMLFactory {
        val mapperFactory = YAMLFactory()
        mapperFactory.configure (WRITE_DOC_START_MARKER, false)
        return mapperFactory
    }

    fun createObjectWriter (): ObjectWriter {
        val printer = DefaultPrettyPrinter ().withArrayIndenter (SYSTEM_LINEFEED_INSTANCE)
        return mapper.writer (printer)
    }

    override fun serialize(obj: Any): String = writer.writeValueAsString (obj)

    override fun <T : Any> parse(text: String, type: KClass<T>): T =
        mapper.readValue (text, type.java)

    override fun <T : Any> parseList(text: String, type: KClass<T>): List<T> {
        val listType = mapper.typeFactory.constructCollectionType(List::class.java, type.java)
        return mapper.readValue (text, listType)
    }

    override fun <T : Any> parse(input: InputStream, type: KClass<T>): T =
        mapper.readValue (input, type.java)

    override fun <T : Any> parseList(input: InputStream, type: KClass<T>): List<T> {
        val listType = mapper.typeFactory.constructCollectionType(List::class.java, type.java)
        return mapper.readValue (input, listType)
    }
}
