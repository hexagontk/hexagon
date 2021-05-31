package com.hexagonkt.serialization

import kotlin.reflect.KClass

object JacksonMapper : Mapper {

    override fun toFieldsMap(instance: Any): Map<*, *> =
        JacksonHelper.mapper.convertValue(instance, Map::class.java)

    override fun <T : Any> toObject(map: Map<*, *>, type: KClass<T>): T =
        JacksonHelper.mapper.convertValue(map, type.java)
}
