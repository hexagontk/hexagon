package com.hexagonkt.serialization

import com.fasterxml.jackson.core.JsonParser.Feature.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
import com.fasterxml.jackson.databind.DeserializationFeature.WRAP_EXCEPTIONS
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES
import com.fasterxml.jackson.databind.SerializationFeature.*

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hexagonkt.helpers.asNumber
import com.hexagonkt.helpers.toLocalDate
import com.hexagonkt.helpers.toLocalDateTime
import com.hexagonkt.helpers.toLocalTime
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

internal object JacksonHelper {
    val mapper: ObjectMapper = createObjectMapper ()

    fun createObjectMapper(mapperFactory: JsonFactory = MappingJsonFactory()): ObjectMapper =
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

    private object ByteBufferSerializer: JsonSerializer<ByteBuffer>() {
        override fun serialize(
            value: ByteBuffer, gen: JsonGenerator, serializers: SerializerProvider) {

            gen.writeString (Base64.getEncoder ().encodeToString (value.array()))
        }
    }

    private object ByteBufferDeserializer: JsonDeserializer<ByteBuffer>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ByteBuffer =
            ByteBuffer.wrap (Base64.getDecoder ().decode (p.text))
    }

    private object LocalTimeSerializer: JsonSerializer<LocalTime> () {
        override fun serialize(
            value: LocalTime, gen: JsonGenerator, serializers: SerializerProvider) {

            gen.writeNumber(value.asNumber())
        }
    }

    private object LocalTimeDeserializer: JsonDeserializer<LocalTime> () {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalTime =
            p.intValue.toLocalTime()
    }

    private object LocalDateSerializer: JsonSerializer<LocalDate> () {
        override fun serialize(
            value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider) {

            gen.writeNumber(value.asNumber())
        }
    }

    private object LocalDateDeserializer: JsonDeserializer<LocalDate> () {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate =
            p.intValue.toLocalDate()
    }

    private object LocalDateTimeSerializer: JsonSerializer<LocalDateTime> () {
        override fun serialize(
            value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {

            gen.writeNumber(value.asNumber())
        }
    }

    private object LocalDateTimeDeserializer: JsonDeserializer<LocalDateTime> () {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime =
            p.longValue.toLocalDateTime()
    }

    private object ClosedRangeSerializer: JsonSerializer<ClosedRange<*>> () {
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
    private object ClosedRangeDeserializer :
        JsonDeserializer<ClosedRange<*>> (), ContextualDeserializer {

        private val valueType: ThreadLocal<JavaType?> = ThreadLocal.withInitial { null }

        override fun createContextual(
            ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {

            valueType.set(property.type.containedType(0))
            return ClosedRangeDeserializer
        }

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClosedRange<*> {
            val token = p.currentToken
            check (token == START_OBJECT) { "${token.name} should be: ${START_OBJECT.name}" }
            check(p.nextFieldName() == "start") { "Ranges start with 'start' field" }

            p.nextToken() // Start object
            val type = valueType.get()
            val start = ctxt.readValue<Comparable<Any>>(p, type)
            check(p.nextFieldName() == "endInclusive") { "Ranges end with 'endInclusive' field" }

            p.nextToken() // End array
            val end = ctxt.readValue<Comparable<Any>>(p, type)
            p.nextToken() // End array

            return start .. end
        }
    }
}
