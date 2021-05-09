package com.hexagonkt.serialization

import kotlin.reflect.KClass

object JacksonMapper : Mapper {

    override fun convertToMap(instance: Any): Map<*, *> =
        JacksonHelper.mapper.convertValue(instance, Map::class.java)

    override fun <T : Any> convertToObject(map: Map<*, *>, type: KClass<T>): T =
        JacksonHelper.mapper.convertValue(map, type.java)
}
