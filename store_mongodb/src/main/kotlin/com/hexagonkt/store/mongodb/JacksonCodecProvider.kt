package com.hexagonkt.store.mongodb

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.hexagonkt.serialization.JacksonHelper.createObjectMapper
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import kotlin.reflect.KProperty1

/**
 * Provide codecs that use Jackson Object Mapper for all Java classes.
 */
internal  class JacksonCodecProvider<T : Any, K : Any> constructor(
//    private val type: KClass<T>,
    private val key: KProperty1<T, K>,
    private val useObjectId: Boolean = true,
    private val useUnderscoreId: Boolean = true) : CodecProvider {

//    init {
//        type.declaredMemberProperties
//            .filter { it.returnType.jvmErasure in listOf(LocalDate::class) }
//    }

    companion object {
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
            internal var documentCodec = registry.get(Document::class.java)

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
                        map.put(key.name, id)
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
