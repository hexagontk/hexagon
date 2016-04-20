package co.there4.hexagon.serialization

import co.there4.hexagon.serialization.SerializationFormat.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.ByteBuffer
import java.util.*
import kotlin.reflect.KClass

class JacksonSerializer : Serializer {
    val MAPPER = createObjectMapper ()
    val WRITER = createObjectWriter ()

    fun createObjectMapper (): ObjectMapper {
        val byteBufferSerializer: JsonSerializer<ByteBuffer> =
            object : JsonSerializer<ByteBuffer> () {
                override fun serialize(
                    value: ByteBuffer, gen: JsonGenerator, serializers: SerializerProvider) {

                    gen.writeString (Base64.getEncoder ().encodeToString (value.array()))
                }
            }

        val byteBufferDeserializer: JsonDeserializer<ByteBuffer> =
            object : JsonDeserializer<ByteBuffer> () {
                override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ByteBuffer =
                    ByteBuffer.wrap (Base64.getDecoder ().decode (p.getText ()))
            }

        val mapper = ObjectMapper ()
        mapper.configure (FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure (FAIL_ON_EMPTY_BEANS, false)
        mapper.setSerializationInclusion (NON_EMPTY)
        mapper.setPropertyNamingStrategy (SNAKE_CASE)
        mapper.registerModule (Jdk8Module ())
        mapper.registerModule (JavaTimeModule ())
        mapper.registerModule (KotlinModule ())
        mapper.registerModule (object : SimpleModule () {
            init {
                addSerializer (ByteBuffer::class.java, byteBufferSerializer)
                addDeserializer (ByteBuffer::class.java, byteBufferDeserializer)
            }
        })
        return mapper
    }

    fun createObjectWriter (): ObjectWriter {
        val printer = DefaultPrettyPrinter ().withArrayIndenter (SYSTEM_LINEFEED_INSTANCE)
        return MAPPER.writer (printer)
    }

    override fun convertToMap(obj: Any): Map<*, *> =
        MAPPER.convertValue (obj, Map::class.java) ?: throw IllegalStateException ()

    override fun <T : Any> convertToObject(obj: Map<*, *>, type: KClass<T>): T = MAPPER.convertValue (obj, type.java)

    override fun serialize(format: SerializationFormat, obj: Any): String = when (format) {
        JSON -> serializeJson (obj)
        YAML -> serializeYaml (obj)
        XML -> serializeXml (obj)
    }

    override fun <T : Any> parse(format: SerializationFormat, text: String, type: KClass<T>): T =
        when (format) {
            JSON -> parseJson (text, type)
            YAML -> parseYaml (text, type)
            XML -> parseXml (text, type)
        }

    override fun <T : Any> parseList(format: SerializationFormat, text: String, type: KClass<T>) =
        when (format) {
            JSON -> parseJsonList (text, type)
            YAML -> parseYamlList (text, type)
            XML -> parseXmlList (text, type)
        }

    private fun serializeJson(obj: Any): String = WRITER.writeValueAsString (obj)

    private fun <T : Any> parseJson(text: String, type: KClass<T>): T =
        MAPPER.readValue (text, type.java)

    private fun <T : Any> parseJsonList(text: String, type: KClass<T>): List<T> {
        val t = MAPPER.getTypeFactory().constructCollectionType(List::class.java, type.java)
        return MAPPER.readValue (text, t)
    }

    private fun serializeXml(obj: Any): String = TODO()
    private fun <T: Any> parseXml(text: String, type: KClass<T>): T = TODO()
    private fun <T: Any> parseXmlList(text: String, type: KClass<T>): List<T> = TODO()

    private fun serializeYaml(obj: Any): String = TODO()
    private fun <T: Any> parseYaml(text: String, type: KClass<T>): T = TODO()
    private fun <T: Any> parseYamlList(text: String, type: KClass<T>): List<T> = TODO()
}
