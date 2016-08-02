package co.there4.hexagon.serialization

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.JsonToken.START_ARRAY
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.InputStream
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
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

    private fun createObjectMapper (): ObjectMapper {
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
                    ByteBuffer.wrap (Base64.getDecoder ().decode (p.text))
            }

        val localTimeSerializer: JsonSerializer<LocalTime> =
            object : JsonSerializer<LocalTime> () {
                override fun serialize(
                    value: LocalTime, gen: JsonGenerator, serializers: SerializerProvider) {

                    gen.writeStartArray()
                    gen.writeNumber(value.hour)
                    gen.writeNumber(value.minute)
                    gen.writeNumber(value.second)
                    gen.writeNumber(value.nano)
                    gen.writeEndArray()
                }
            }

        val localTimeDeserializer: JsonDeserializer<LocalTime> =
            object : JsonDeserializer<LocalTime> () {
                override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalTime {
                    val result = LocalTime.of(
                        p.nextIntValue(0),
                        p.nextIntValue(0),
                        p.nextIntValue(0),
                        p.nextIntValue(0)
                    )
                    p.nextToken()
                    return result
                }
            }

        val localDateSerializer: JsonSerializer<LocalDate> =
            object : JsonSerializer<LocalDate> () {
                override fun serialize(
                    value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider) {

                    gen.writeStartArray()
                    gen.writeNumber(value.year)
                    gen.writeNumber(value.monthValue)
                    gen.writeNumber(value.dayOfMonth)
                    gen.writeEndArray()
                }
            }

        val localDateDeserializer: JsonDeserializer<LocalDate> =
            object : JsonDeserializer<LocalDate> () {
                override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate {
                    val result = LocalDate.of(
                        p.nextIntValue(0),
                        p.nextIntValue(0),
                        p.nextIntValue(0)
                    )
                    p.nextToken()
                    return result
                }
            }

        val mapper = ObjectMapper ()
        mapper.configure (FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure (FAIL_ON_EMPTY_BEANS, false)
        mapper.setSerializationInclusion (NON_NULL)
        mapper.registerModule (Jdk8Module ())
        mapper.registerModule (JavaTimeModule ())
        mapper.registerModule (KotlinModule ())
        mapper.registerModule (object : SimpleModule () {
            init {
                addSerializer (ByteBuffer::class.java, byteBufferSerializer)
                addDeserializer (ByteBuffer::class.java, byteBufferDeserializer)
                addSerializer (LocalTime::class.java, localTimeSerializer)
                addDeserializer (LocalTime::class.java, localTimeDeserializer)
                addSerializer (LocalDate::class.java, localDateSerializer)
                addDeserializer (LocalDate::class.java, localDateDeserializer)
            }
        })
        return mapper
    }

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

    fun <T: Any> parseList(input: InputStream, type: KClass<T>, contentType: String = defaultFormat) =
        getSerializationFormat (contentType).parseList (input, type)
}
