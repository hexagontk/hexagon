package com.hexagonkt.serialization

import com.hexagonkt.serialization.SerializationManager.formats
import org.testng.annotations.Test
import kotlin.reflect.KClass

abstract class SerializationTest<out T : Any> (private val type: KClass<T>) {
    abstract val testObjects: List<T>

    // Some formats are excluded because they don't support all features
    private val ignoredFormats: Set<SerializationFormat> = setOf(Csv)

    @Test fun `Object is mapped and parsed back without error` () {
        (formats - ignoredFormats).forEach { contentType ->
            testObjects.forEach {
                val map = it.convertToMap ()

                val object2 = map.convertToObject (type)
                assert(it == object2)

                val modelString = it.serialize(contentType)
                assert(modelString == it.serialize(contentType.contentType))
                val object3 = modelString.parse(type, contentType)
                assert(it == object3)

                assert(modelString.parse(contentType) == map)

                val tempFile = createTempFile(suffix = contentType.contentType.replace('/', '.'))
                tempFile.deleteOnExit()
                tempFile.writeText(modelString)

                assert(tempFile.parse() == map)
            }

            val serializedObjects = testObjects.serialize(contentType)
            val tempFile = createTempFile(suffix = contentType.contentType.replace('/', '.'))
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
