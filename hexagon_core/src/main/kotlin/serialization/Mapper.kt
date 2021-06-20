package com.hexagonkt.serialization

import kotlin.reflect.KClass

interface Mapper {

    fun toFieldsMap(instance: Any): Map<*, *>

    fun <T : Any> toObject(map: Map<*, *>, type: KClass<T>): T
}
