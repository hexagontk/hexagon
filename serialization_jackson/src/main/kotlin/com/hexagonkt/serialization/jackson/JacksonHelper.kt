package com.hexagonkt.serialization.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES
import com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES
import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import com.fasterxml.jackson.core.json.JsonReadFeature.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
import com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.Base64

object JacksonHelper {

    fun createObjectMapper(mapperFactory: JsonFactory): JsonMapper =
        JsonMapper
            .builder(mapperFactory)
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(FAIL_ON_EMPTY_BEANS, false)
            .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(ALLOW_JAVA_COMMENTS, true)
            .configure(ALLOW_SINGLE_QUOTES, true)
            .configure(ALLOW_TRAILING_COMMA, true)
            .configure(ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
            .configure(ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(ALLOW_UNESCAPED_CONTROL_CHARS, true)
            .configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(SORT_PROPERTIES_ALPHABETICALLY, false)
            .addModule(JavaTimeModule())
            .addModule(SimpleModule()
                .addSerializer(ByteBuffer::class.java, ByteBufferSerializer)
                .addDeserializer(ByteBuffer::class.java, ByteBufferDeserializer)
                .addSerializer(ClosedRange::class.java, ClosedRangeSerializer)
                .addDeserializer(ClosedRange::class.java, ClosedRangeDeserializer())
                .addSerializer(Float::class.java, FloatSerializer)
                .addDeserializer(Float::class.java, FloatDeserializer)
                .addSerializer(InetAddress::class.java, InetAddressSerializer)
                .addDeserializer(InetAddress::class.java, InetAddressDeserializer)
            )
            .build()

    fun nodeToCollection(node: JsonNode): Any? =
        when (node) {

            is ArrayNode -> node.toList().map { nodeToCollection(it) }
            is ObjectNode -> {
                var map = emptyMap<String, Any?>()

                for (f in node.fields())
                    map = map + (f.key to nodeToCollection(f.value))

                map
            }

            is TextNode -> node.textValue()
            is BigIntegerNode -> node.bigIntegerValue()
            is BooleanNode -> node.booleanValue()
            is DecimalNode -> node.doubleValue()
            is DoubleNode -> node.doubleValue()
            is FloatNode -> node.floatValue()
            is IntNode -> node.intValue()
            is LongNode -> node.longValue()
            is NullNode -> null
            is BinaryNode -> node.binaryValue()

            else -> error("Unknown node type: ${node::class.qualifiedName}")
        }

    fun mapNode(node: JsonNode): Any =
        nodeToCollection(node) ?: error("Parsed content is 'null'")

    object InetAddressSerializer : JsonSerializer<InetAddress>() {
        override fun serialize(
            value: InetAddress, gen: JsonGenerator, serializers: SerializerProvider) {

            gen.writeString(value.hostAddress)
        }
    }

    object InetAddressDeserializer : JsonDeserializer<InetAddress>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): InetAddress =
            InetAddress.getByName(p.text)
    }

    object FloatSerializer : JsonSerializer<Float>() {
        override fun serialize(value: Float, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeNumber(value.toBigDecimal().toDouble()) // BigDecimal needed for good rounding
        }
    }

    object FloatDeserializer : JsonDeserializer<Float>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Float = p.floatValue
    }

    object ByteBufferSerializer: JsonSerializer<ByteBuffer>() {
        override fun serialize(
            value: ByteBuffer, gen: JsonGenerator, serializers: SerializerProvider) {

            gen.writeString(Base64.getEncoder ().encodeToString (value.array()))
        }
    }

    object ByteBufferDeserializer: JsonDeserializer<ByteBuffer>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ByteBuffer =
            ByteBuffer.wrap (Base64.getDecoder ().decode (p.text))
    }

    object ClosedRangeSerializer: JsonSerializer<ClosedRange<*>> () {
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

    class ClosedRangeDeserializer(private val type: JavaType? = null) :
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
