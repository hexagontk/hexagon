package com.hexagonkt.serialization

import com.hexagonkt.serialization.SerializationManager.formats
import kotlin.reflect.KClass

abstract class SerializationTest<out T : Any>(private val type: KClass<T>) {
    abstract val testObjects: List<T>

    protected fun checkMapParse() {
        formats.forEach { contentType ->
            testObjects.forEach {
                val map = it.convertToMap()

                val object2 = map.convertToObject(type)
                assert(it == object2)
                assert(it !== object2)

                val modelString = it.serialize(contentType)
                assert(modelString == it.serialize(contentType.contentType))
                val object3 = modelString.parse(type, contentType)
                assert(it == object3)
                assert(it !== object3)

                assert(modelString.parse<Map<*, *>>(contentType) == map)

                val tempFile = createTempFile(suffix = contentType.contentType.replace('/', '.'))
                tempFile.deleteOnExit()
                tempFile.writeText(modelString)

                assert(tempFile.parse<Map<*, *>>() == map)
            }

            val serializedObjects = testObjects.serialize(contentType)
            val tempFile = createTempFile(suffix = contentType.contentType.replace('/', '.'))
            tempFile.deleteOnExit()
            tempFile.writeText(serializedObjects)
            val testMaps = testObjects.map { it.convertToMap() }

            assert(tempFile.parseObjects<Map<*, *>>() == testMaps)
            assert(testMaps == serializedObjects.parseObjects<Map<*, *>>(contentType))
            assert(testObjects == testMaps.convertToObjects(type))
        }

        val modelListString = testObjects.serialize()
        assert (modelListString.parseObjects(type).size == testObjects.size)
    }
}
