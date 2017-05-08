package co.there4.hexagon.serialization

import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.helpers.asNumber
import co.there4.hexagon.helpers.toLocalDate
import co.there4.hexagon.helpers.toLocalDateTime
import co.there4.hexagon.helpers.toLocalTime

import com.fasterxml.jackson.core.JsonParser.Feature.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
import com.fasterxml.jackson.databind.DeserializationFeature.WRAP_EXCEPTIONS
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES
import com.fasterxml.jackson.databind.SerializationFeature.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

import kotlin.reflect.KClass

val contentTypes = JacksonSerializer.contentTypes
val defaultFormat by lazy { setting<String>("contentType") ?: contentTypes.first() }

fun Any.convertToMap(): Map<*, *> = JacksonSerializer.toMap (this)

fun <T : Any> Map<*, *>.convertToObject(type: KClass<T>): T =
    JacksonSerializer.toObject(this, type)

fun <T : Any> List<Map<*, *>>.convertToObjects(type: KClass<T>): List<T> =
    this.map { it: Map<*, *> -> it.convertToObject(type) }

fun Any.serialize (contentType: String = defaultFormat) =
    JacksonSerializer.serialize(this, contentType)

fun <T : Any> InputStream.parse (type: KClass<T>, contentType: String = defaultFormat) =
    JacksonSerializer.parse (this, type, contentType)
fun InputStream.parse (contentType: String = defaultFormat) = this.parse (Map::class, contentType)
fun <T : Any> InputStream.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    JacksonSerializer.parseList (this, type, contentType)
fun InputStream.parseList (contentType: String = defaultFormat) =
    this.parseList (Map::class, contentType)

fun <T : Any> String.parse (type: KClass<T>, contentType: String = defaultFormat) =
    toStream(this).parse (type, contentType)
fun String.parse (contentType: String = defaultFormat) = this.parse (Map::class, contentType)
fun <T : Any> String.parseList (type: KClass<T>, contentType: String = defaultFormat) =
    toStream(this).parseList (type, contentType)
fun String.parseList(contentType: String = defaultFormat) = this.parseList (Map::class, contentType)

fun <T : Any> File.parse (type: KClass<T>) =
    this.inputStream().parse (type, "application/" + this.extension)
fun File.parse () = this.parse (Map::class)
fun <T : Any> File.parseList (type: KClass<T>): List<T> =
    this.inputStream().parseList (type, "application/" + this.extension)
fun File.parseList () = this.parseList (Map::class)

fun <T : Any> URL.parse (type: KClass<T>) =
    this.openStream().parse (type, "application/" + this.file.substringAfterLast('.'))
fun URL.parse () = this.parse (Map::class)
fun <T : Any> URL.parseList (type: KClass<T>): List<T> =
    this.openStream().parseList (type, "application/" + this.file.substringAfterLast('.'))
fun URL.parseList () = this.parseList (Map::class)

private fun toStream(text: String) = ByteArrayInputStream(text.toByteArray(UTF_8))

private fun JsonToken.checkIs(expected: JsonToken) {
    check (this == expected) { "${this.name} should be: ${expected.name}" }
}

internal fun createObjectMapper(mapperFactory: JsonFactory = MappingJsonFactory()) =
    ObjectMapper (mapperFactory)
        .configure (FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure (FAIL_ON_EMPTY_BEANS, false)
        .configure (ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure (ALLOW_COMMENTS, true)
        .configure (ALLOW_SINGLE_QUOTES, true)
        .configure (WRAP_EXCEPTIONS, false)
        .configure (FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        .configure (ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .setSerializationInclusion (NON_EMPTY)
        .registerModule (KotlinModule ())
        .registerModule (SimpleModule("SerializationModule", Version.unknownVersion())
            .addSerializer (ByteBuffer::class.java, ByteBufferSerializer)
            .addDeserializer (ByteBuffer::class.java, ByteBufferDeserializer)
            .addSerializer (LocalTime::class.java, LocalTimeSerializer)
            .addDeserializer (LocalTime::class.java, LocalTimeDeserializer)
            .addSerializer (LocalDate::class.java, LocalDateSerializer)
            .addDeserializer (LocalDate::class.java, LocalDateDeserializer)
            .addSerializer (LocalDateTime::class.java, LocalDateTimeSerializer)
            .addDeserializer (LocalDateTime::class.java, LocalDateTimeDeserializer)
            .addSerializer (ClosedRange::class.java, ClosedRangeSerializer)
            .addDeserializer (ClosedRange::class.java, ClosedRangeDeserializer)
        )

internal object ByteBufferSerializer: JsonSerializer<ByteBuffer>() {
    override fun serialize(value: ByteBuffer, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString (Base64.getEncoder ().encodeToString (value.array()))
    }
}

internal object ByteBufferDeserializer: JsonDeserializer<ByteBuffer>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ByteBuffer =
        ByteBuffer.wrap (Base64.getDecoder ().decode (p.text))
}

internal object LocalTimeSerializer: JsonSerializer<LocalTime> () {
    override fun serialize(value: LocalTime, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeNumber(value.asNumber())
    }
}

internal object LocalTimeDeserializer: JsonDeserializer<LocalTime> () {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalTime =
        p.intValue.toLocalTime()
}

internal object LocalDateSerializer: JsonSerializer<LocalDate> () {
    override fun serialize(value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeNumber(value.asNumber())
    }
}

internal object LocalDateDeserializer: JsonDeserializer<LocalDate> () {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate =
        p.intValue.toLocalDate()
}

internal object LocalDateTimeSerializer: JsonSerializer<LocalDateTime> () {
    override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeNumber(value.asNumber())
    }
}

internal object LocalDateTimeDeserializer: JsonDeserializer<LocalDateTime> () {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime =
        p.longValue.toLocalDateTime()
}

internal object ClosedRangeSerializer: JsonSerializer<ClosedRange<*>> () {
    override fun serialize(
        value: ClosedRange<*>, gen: JsonGenerator, serializers: SerializerProvider) {

        val start = value.start
        val end = value.endInclusive
        val valueSerializer = serializers.findValueSerializer(start.javaClass)

        gen.writeStartObject()

        gen.writeFieldName("start")
        valueSerializer.serialize(start, gen, serializers)

        gen.writeFieldName("endInclusive")
        valueSerializer.serialize(end, gen, serializers)

        gen.writeEndObject()
    }
}

// TODO Not thread safe!!! (as proved by parallel tests)
internal object ClosedRangeDeserializer: JsonDeserializer<ClosedRange<*>> (), ContextualDeserializer {
    private val valueType: ThreadLocal<JavaType?> = ThreadLocal.withInitial { null }

    override fun createContextual(
        ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {

        valueType.set(property.type.containedType(0))
        return ClosedRangeDeserializer
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClosedRange<*> {
        p.currentToken.checkIs(START_OBJECT)
        check(p.nextFieldName() == "start") { "Ranges should start with 'start' field" }
        p.nextToken() // Start object
        val type = valueType.get()
        @Suppress("UNCHECKED_CAST") val start = ctxt.readValue<Any>(p, type) as Comparable<Any>
        check(p.nextFieldName() == "endInclusive") { "Ranges should end with 'endInclusive' field" }
        p.nextToken() // End array
        @Suppress("UNCHECKED_CAST") val end = ctxt.readValue<Any>(p, type) as Comparable<Any>
        p.nextToken() // End array
        return start .. end
    }
}
