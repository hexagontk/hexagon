package com.hexagonkt.store.mongodb

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import com.fasterxml.jackson.core.JsonParser.Feature.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES
import com.fasterxml.jackson.databind.SerializationFeature.*

import com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import kotlin.reflect.KProperty1

/**
 * Provide codecs that use Jackson Object Mapper for all Java classes.
 */
internal  class JacksonCodecProvider<T : Any, K : Any> constructor(
//    private val type: KClass<T>,
    private val key: KProperty1<T, K>,
    private val useObjectId: Boolean = true) : CodecProvider {

//    init {
//        type.declaredMemberProperties
//            .filter { it.returnType.jvmErasure in listOf(LocalDate::class) }
//    }

    private companion object {

        private object ClosedRangeSerializer: JsonSerializer<ClosedRange<*>>() {
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
            .setSerializationInclusion(NON_EMPTY)
            .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"))
            .registerModule(KotlinModule ())
            .registerModule(JavaTimeModule())
            .registerModule(Jdk8Module())
            .registerModule(SimpleModule("SerializationModule", Version.unknownVersion())
                .addSerializer(ClosedRange::class.java, ClosedRangeSerializer)
                .addDeserializer(ClosedRange::class.java, ClosedRangeDeserializer())
            )

        val mapper: ObjectMapper = createObjectMapper()
            .registerModule(SimpleModule("MongoDbHelpers", Version.unknownVersion())
                .addSerializer(ObjectId::class.java, ToStringSerializer())
                .addDeserializer(ObjectId::class.java, object : JsonDeserializer<ObjectId>() {
                    override fun deserialize(p: JsonParser, ctx: DeserializationContext): ObjectId =
                        ObjectId(p.readValueAs(String::class.java))
                })
            )
    }

    /** {@inheritDoc} */
    override fun <TC> get(clazz: Class<TC>, registry: CodecRegistry): Codec<TC> =
        object : Codec<TC> {
            var documentCodec = registry.get(Document::class.java)

            /** {@inheritDoc} */
            override fun encode(writer: BsonWriter, value: TC, encoderContext: EncoderContext) {
                @Suppress("UNCHECKED_CAST")
                val valueMap = mapper.convertValue(value, Map::class.java) as Map<String, *>
                val map = HashMap(valueMap)

                if (useObjectId) {
                    val id =
                        if (map[key.name] == null) ObjectId()
                        else ObjectId(map[key.name].toString())

                    if (clazz == key.returnType)
                        map[key.name] = id
                }

                documentCodec.encode(writer, Document(map), encoderContext)
            }

            /** {@inheritDoc} */
            override fun decode(reader: BsonReader, decoderContext: DecoderContext): TC {
                val document: Document = documentCodec.decode(reader, decoderContext)

                if (useObjectId)
                    document.computeIfPresent(key.name) { _, value ->
                        (value as ObjectId).toHexString()
                    }

                return mapper.convertValue(document, clazz)
            }

            /** {@inheritDoc} */
            override fun getEncoderClass(): Class<TC> = clazz
        }
}
