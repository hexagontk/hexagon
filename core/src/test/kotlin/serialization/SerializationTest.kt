package com.hexagonkt.core.serialization

import com.hexagonkt.core.serialization.SerializationManager.formats
import com.hexagonkt.serialization.json.JacksonMapper
import com.hexagonkt.serialization.json.Json
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File
import java.nio.file.Files
import kotlin.reflect.KClass

@TestInstance(PER_CLASS)
abstract class SerializationTest<out T : Any>(private val type: KClass<T>) {
    abstract val testObjects: List<T>

    private fun tempFile(suffix: String): File =
        Files.createTempFile("", suffix).toFile()

    @BeforeAll fun initialize() {
        formats = linkedSetOf(Json)
        SerializationManager.mapper = JacksonMapper
    }

    protected fun checkMapParse() {
        formats.forEach { contentType ->
            testObjects.forEach {
                val map = it.toFieldsMap()

                val object2 = map.toObject(type)
                assert(it == object2)
                assert(it !== object2)

                val modelString = it.serialize(contentType)
                assert(modelString == it.serialize(contentType.contentType))
                val object3 = modelString.parse(type, contentType)
                assert(it == object3)
                assert(it !== object3)

                assert(modelString.parse<Map<*, *>>(contentType) == map)

                val tempFile = tempFile(contentType.contentType.replace('/', '.'))
                tempFile.deleteOnExit()
                tempFile.writeText(modelString)

                assert(tempFile.parse<Map<*, *>>() == map)
            }

            val serializedObjects = testObjects.serialize(contentType)
            val tempFile = tempFile(contentType.contentType.replace('/', '.'))
            tempFile.deleteOnExit()
            tempFile.writeText(serializedObjects)
            val testMaps = testObjects.map { it.toFieldsMap() }

            assert(tempFile.parseObjects<Map<*, *>>() == testMaps)
            assert(testMaps == serializedObjects.parseObjects<Map<*, *>>(contentType))
            assert(testObjects == testMaps.map { it.toObject(type) })
        }

        val modelListString = testObjects.serialize()
        assert (modelListString.parseObjects(type).size == testObjects.size)
    }
}
