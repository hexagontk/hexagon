package co.there4.hexagon.serialization

import co.there4.hexagon.serialization.*
import co.there4.hexagon.serialization.*
import org.testng.annotations.Test
import kotlin.reflect.KClass

@Test abstract class SerializationTest<T : Any> (val type: KClass<T>) {
    abstract val testObjects: List<T>

    fun object_is_mapped_and_parsed_back_without_error () {
        testObjects.forEach {
            val map = it.toMap ()

            val object2 = map.toObject (type)
            assert (it.equals (object2))

            val modelString = it.serialize ()
            val object3 = modelString.parse (type)
            assert (it.equals (object3))
        }

        val modelListString = testObjects.serialize()
        assert (modelListString.parseList(type).size == testObjects.size)
    }
}
