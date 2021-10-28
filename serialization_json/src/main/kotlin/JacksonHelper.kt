package com.hexagonkt.serialization.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS
import com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES
import com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES
import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
import com.fasterxml.jackson.databind.MappingJsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hexagonkt.serialization.ParseException
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.Base64

object JacksonHelper {

    val mapper: ObjectMapper by lazy { createObjectMapper () }

    fun createObjectMapper(mapperFactory: JsonFactory = MappingJsonFactory()): ObjectMapper =
        setupObjectMapper(ObjectMapper(mapperFactory))

    fun setupObjectMapper(objectMapper: ObjectMapper): ObjectMapper = objectMapper
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(FAIL_ON_EMPTY_BEANS, false)
        .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(ALLOW_COMMENTS, true)
        .configure(ALLOW_SINGLE_QUOTES, true)
        .configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .configure(SORT_PROPERTIES_ALPHABETICALLY, false)
        .registerModule(KotlinModule())
        .registerModule(JavaTimeModule())
        .registerModule(Jdk8Module())
        .registerModule(SimpleModule("SerializationModule", Version.unknownVersion())
            .addSerializer(ByteBuffer::class.java, ByteBufferSerializer)
            .addDeserializer(ByteBuffer::class.java, ByteBufferDeserializer)
            .addSerializer(ClosedRange::class.java, ClosedRangeSerializer)
            .addDeserializer(ClosedRange::class.java, ClosedRangeDeserializer())
            .addSerializer(Float::class.java, FloatSerializer)
            .addDeserializer(Float::class.java, FloatDeserializer)
            .addSerializer(InetAddress::class.java, InetAddressSerializer)
            .addDeserializer(InetAddress::class.java, InetAddressDeserializer)
        )

    fun parseException(e: Exception?): ParseException =
        ParseException((e as? JsonMappingException)?.pathReference ?: "", e)

    private object InetAddressSerializer : JsonSerializer<InetAddress>() {
        override fun serialize(
            value: InetAddress, gen: JsonGenerator, serializers: SerializerProvider) {

            gen.writeString(value.hostAddress)
        }
    }

    private object InetAddressDeserializer : JsonDeserializer<InetAddress>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): InetAddress =
            InetAddress.getByName(p.text)
    }

    private object FloatSerializer : JsonSerializer<Float>() {
        override fun serialize(value: Float, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeNumber(value.toBigDecimal().toDouble()) // BigDecimal needed for good rounding
        }
    }

    private object FloatDeserializer : JsonDeserializer<Float>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Float = p.floatValue
    }

    private object ByteBufferSerializer: JsonSerializer<ByteBuffer>() {
        override fun serialize(
            value: ByteBuffer, gen: JsonGenerator, serializers: SerializerProvider) {

            gen.writeString(Base64.getEncoder ().encodeToString (value.array()))
        }
    }

    private object ByteBufferDeserializer: JsonDeserializer<ByteBuffer>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ByteBuffer =
            ByteBuffer.wrap (Base64.getDecoder ().decode (p.text))
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

    private class ClosedRangeDeserializer(private val type: JavaType? = null) :
        JsonDeserializer<ClosedRange<*>> (), ContextualDeserializer {

        override fun createContextual(
            ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> =
                ClosedRangeDeserializer(property.type.containedType(0))

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClosedRange<*> {
            val token = p.currentToken
            check (token == START_OBJECT) { "${token.name} should be: ${START_OBJECT.name}" }
            check(p.nextFieldName() == "start") { "Ranges start with 'start' field" }

            p.nextToken() // Start object
            val start = ctxt.readValue<Comparable<Any>>(p, type)
            check(p.nextFieldName() == "endInclusive") { "Ranges end with 'endInclusive' field" }

            p.nextToken() // End array
            val end = ctxt.readValue<Comparable<Any>>(p, type)
            p.nextToken() // End array

            return start .. end
        }
    }
}
