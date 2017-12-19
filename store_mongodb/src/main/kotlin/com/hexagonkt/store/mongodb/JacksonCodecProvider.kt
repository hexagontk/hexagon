package com.hexagonkt.store.mongodb

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.core.JsonParser.Feature.*
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import com.fasterxml.jackson.databind.DeserializationFeature.*
import com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Provide codecs that use Jackson Object Mapper for all Java classes.
 */
internal  class JacksonCodecProvider<T : Any, K : Any> internal constructor(
    private val entity: KClass<T>,
    private val key: KProperty1<T, K>,
    private val generateKey: Boolean = true) : CodecProvider {

    /** {@inheritDoc} */
    override fun <TC> get(clazz: Class<TC>, registry: CodecRegistry): Codec<TC> = object : Codec<TC> {
        internal var documentCodec = registry.get(Document::class.java)

        /** {@inheritDoc} */
        override fun encode(writer: BsonWriter, value: TC, encoderContext: EncoderContext) {
            val map = mapper.convertValue(value, Map::class.java) as Map<String, Any>

            if (generateKey) {
                val id = if (map[key.name] == null)
                    ObjectId()
                else
                    ObjectId(map[key.name].toString())

//                if (clazz == key.returnType)
//                    map.put(key.name, id)
            }

            documentCodec.encode(writer, Document(map), encoderContext)
        }

        /** {@inheritDoc} */
        override fun decode(reader: BsonReader, decoderContext: DecoderContext): TC {
            val document: Document = documentCodec.decode(reader, decoderContext)

            if (generateKey)
                document.computeIfPresent(key.name) { _, value -> (value as ObjectId).toHexString() }

            return mapper.convertValue(document, clazz)
        }

        /** {@inheritDoc} */
        override fun getEncoderClass(): Class<TC> = clazz
    }

    companion object {
        val mapper: ObjectMapper = ObjectMapper()
            .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(FAIL_ON_EMPTY_BEANS, false)
            .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(ALLOW_COMMENTS, true)
            .configure(ALLOW_SINGLE_QUOTES, true)
            .configure(WRAP_EXCEPTIONS, false)
            .configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .setSerializationInclusion(NON_EMPTY)
            .registerModule(Jdk8Module())
            .registerModule(JavaTimeModule())
            .registerModule(SimpleModule("MongoDbHelpers", Version.unknownVersion())
                .addSerializer(ObjectId::class.java, ToStringSerializer())
                .addDeserializer(ObjectId::class.java, object : JsonDeserializer<ObjectId>() {
                    override fun deserialize(p: JsonParser, ctx: DeserializationContext): ObjectId =
                        ObjectId(p.readValueAs(String::class.java))
                })
            )

        val writer: ObjectWriter = mapper.writer(
            DefaultPrettyPrinter().withArrayIndenter(SYSTEM_LINEFEED_INSTANCE)
        )
    }
}
