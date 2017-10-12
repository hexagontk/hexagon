package com.hexagonkt.serialization

import com.hexagonkt.serialization.SerializationManager.contentTypes
import kotlin.reflect.KClass

abstract class SerializationTest<T : Any> (val type: KClass<T>) {
    abstract val testObjects: List<T>

    fun object_is_mapped_and_parsed_back_without_error () {
        contentTypes.forEach { contentType ->
            testObjects.forEach {
                val map = it.convertToMap ()

                val object2 = map.convertToObject (type)
                assert(it == object2)

                val modelString = it.serialize(contentType)
                val object3 = modelString.parse(type, contentType)
                assert(it == object3)

                assert(modelString.parse(contentType) == map)

                val tempFile = createTempFile(suffix = contentType.replace('/', '.'))
                tempFile.deleteOnExit()
                tempFile.writeText(modelString)

                assert(tempFile.parse() == map)
            }

            val serializedObjects = testObjects.serialize(contentType)
            val tempFile = createTempFile(suffix = contentType.replace('/', '.'))
            tempFile.deleteOnExit()
            tempFile.writeText(serializedObjects)
            val testMaps = testObjects.map { it.convertToMap() }

            assert(tempFile.parseList() == testMaps)
            assert(testMaps == serializedObjects.parseList(contentType))
            assert(testObjects == testMaps.convertToObjects(type))
        }

        val modelListString = testObjects.serialize()
        assert (modelListString.parseList(type).size == testObjects.size)
    }
}
