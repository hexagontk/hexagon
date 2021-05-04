package com.hexagonkt.serialization

import kotlin.reflect.KClass

interface Mapper {

    fun convertToMap(instance: Any): Map<*, *>

    fun <T : Any> convertToObject(map: Map<*, *>, type: KClass<T>): T
}
