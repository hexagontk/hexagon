package com.hexagonkt.vertx.store.mongodb

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.hexagonkt.vertx.serialization.JacksonHelper
import kotlin.reflect.KClass

/**
 * TODO .
 */
object MongoDbJson {
    val mapper: ObjectMapper = JacksonHelper.setupObjectMapper(ObjectMapper())
        .registerModule(SimpleModule("MongoDbModule", Version.unknownVersion())
            .addSerializer(Float::class.java, FloatSerializer)
            .addDeserializer(Float::class.java, FloatDeserializer)
        )

    private object FloatSerializer : JsonSerializer<Float>() {
        override fun serialize(value: Float, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeNumber(value.toBigDecimal().toDouble()) // BigDecimal needed for good rounding
        }
    }

    private object FloatDeserializer : JsonDeserializer<Float>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Float = p.floatValue
    }

    fun convertToMap(value: Any): Map<String, *> = mapper.convertValue (value, Map::class.java)
        .filter { it.key is String }
        .mapKeys { it.key.toString() }

    fun <T : Any> convertToObject(type: KClass<T>, value: Map<*, *>): T =
        mapper.convertValue(value, type.java)
}
