package com.hexagonkt.core

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class DataTest {

    data class Player(
        val name: String,
        val number: Int,
        val category: ClosedRange<Int>
    )

    private val m: Map<Any, Any> = mapOf(
        "alpha" to "bravo",
        "tango" to 0,
        "list" to listOf("first", "second"),
        "nested" to mapOf(
            "zulu" to "charlie"
        ),
        0 to 1
    )

    @Test fun `Get nested keys inside a map returns the proper value`() {
        assert(m.keys<String>("nested", "zulu") == "charlie")
        assert(m.keys<Any>("nested", "zulu", "tango") == null)
        assert(m.keys<Any>("nested", "empty") == null)
        assert(m.keys<Any>("empty") == null)
        assert(m.keys<String>("alpha") == "bravo")
        assert(m.keys<Int>(0) == 1)

        assert(m["empty"] == null)
        assert(m["alpha"] == "bravo")
        assert(m[0] == 1)

        assertEquals(m("nested", "zulu"), "charlie")
        assertNull(m("nested", "zulu", "tango"))
        assertNull(m("nested", "empty"))
        assertNull(m("empty"))
        assertEquals(m("alpha"), "bravo")
        assertEquals(m(0), 1)

        val a: String? = m("alpha")
        val b: String? = m("nested", "zulu")
        val c = m<String>("alpha")
        val d = m<String>("nested", "zulu")
        assertEquals("bravo", a)
        assertEquals("charlie", b)
        assertEquals("bravo", c)
        assertEquals("charlie", d)
    }

    @Test fun `Utilities for mapping classes fields work as expected`() {
        val fm = fieldsMapOf(
            Player::category to 18..65,
            Player::name to "Magic",
            Player::number to 32,
        )

        assertEquals(18..65, fm(Player::category))
        assertEquals("Magic", fm(Player::name))
        assertEquals(32, fm(Player::number))
    }

    @Test fun `Require a value defined by a list of keys return the correct value`() {
        assert(m.requireKeys<String>("nested", "zulu") == "charlie")
        assert(m.requireKeys<String>("alpha") == "bravo")
        assert(m.requireKeys<Int>(0) == 1)
    }

    @Test fun `Require not found key fails`() {
        assertFailsWith<IllegalStateException> {
            m.require("void")
        }
    }

    @Test fun `Require keys with non existing keys fails`() {
        assertFailsWith<IllegalStateException> {
            m.requireKeys("nested", "zulu", "tango")
        }
    }

    @Test fun `Require not found key in map fails`() {
        assertFailsWith<IllegalStateException> {
            m.requireKeys("nested", "empty")
        }
    }

    @Test fun `Require key not found first level throws an error`() {
        assertFailsWith<IllegalStateException> {
            m.requireKeys("empty")
        }
    }

    @Test fun `Require existing key returns correct value`() {
        assert(m.require("alpha") == "bravo")
    }

    @Test fun `Filtered maps do not contain empty elements`() {
        assert(
            mapOf(
                "a" to "b",
                "b" to null,
                "c" to 1,
                "d" to listOf(1, 2),
                "e" to listOf<String>(),
                "f" to mapOf(0 to 1),
                "g" to mapOf<String, Int>(),
                "h" to mapOf("a" to true, "b" to null).filterEmpty(),
                "i" to mapOf("a" to listOf<Int>()).filterEmpty()
            ).filterEmpty() ==
            mapOf(
                "a" to "b",
                "c" to 1,
                "d" to listOf(1, 2),
                "f" to mapOf(0 to 1),
                "h" to mapOf("a" to true)
            )
        )
    }

    @Test fun `Filtered lists do not contain empty elements`() {
        assert(
            listOf(
                "a",
                null,
                listOf(1, 2),
                listOf<String>(),
                mapOf(0 to 1),
                mapOf<String, Int>(),
                mapOf("a" to true, "b" to null).filterEmpty(),
                mapOf("a" to listOf<Int>()).filterEmpty()
            ).filterEmpty() ==
            listOf(
                "a",
                listOf(1, 2),
                mapOf(0 to 1),
                mapOf("a" to true)
            )
        )
    }

    @Test fun `Filtered collections do not contain nested empty elements`() {
        assertEquals(
            mapOf(
                "a" to "b",
                "c" to 1,
                "d" to listOf(1, 2),
                "f" to mapOf(0 to 1),
                "h" to mapOf("a" to true),
                "m" to listOf(
                    mapOf("a" to 1, "b" to "c"),
                ),
            ),
            mapOf(
                "a" to "b",
                "b" to null,
                "c" to 1,
                "d" to listOf(1, 2),
                "e" to listOf<String>(),
                "f" to mapOf(0 to 1),
                "g" to mapOf<String, Int>(),
                "h" to mapOf("a" to true, "b" to null).filterEmpty(),
                "i" to mapOf("a" to listOf<Int>()).filterEmpty(),
                "j" to listOf(null, null),
                "k" to mapOf("a" to null, "b" to null),
                "l" to listOf(
                    null,
                    listOf(null),
                    mapOf("a" to null, "b" to null),
                ),
                "m" to listOf(
                    null,
                    mapOf("a" to 1, "b" to "c", "z" to null),
                    null,
                ),
            ).filterEmptyRecursive()
        )
    }

    @Test fun `Ensure fails if collection size is larger`() {
        assertFailsWith<IllegalStateException> {
            listOf(1, 2, 3).ensureSize(1..2)
        }
    }

    @Test fun `Ensure fails if collection size is smaller`() {
        assertFailsWith<IllegalStateException> {
            listOf(1, 2, 3).ensureSize(4..5)
        }
    }

    @Test fun `Ensure returns the collection if size is correct`() {
        val list = listOf(1, 2, 3)
        assert(list.ensureSize(0..3) == list)
        assert(list.ensureSize(1..3) == list)
        assert(list.ensureSize(2..3) == list)
        assert(list.ensureSize(3..3) == list)
        assert(list.ensureSize(0..4) == list)
    }

    @Test fun `Utilities to map data objects works correctly`() {
        data class DataClass(
            val a: Int,
            val b: Long,
            val c: Float,
            val d: Double,
            val e: Boolean,
            val f: String,
            val g: List<*>,
            val h: Map<*, *>,

            val i: List<Int>,
            val j: List<Long>,
            val k: List<Float>,
            val l: List<Double>,
            val m: List<Boolean>,
            val n: List<String>,
            val o: List<List<*>>,
            val p: List<Map<*, *>>,

            val q: Int?,

            val r: List<Int>,
        )

        val m = fieldsMapOfNotNull(
            DataClass::a to 1,
            DataClass::b to 2L,
            DataClass::c to 3.1F,
            DataClass::d to 4.2,
            DataClass::e to true,
            DataClass::f to "text",
            DataClass::g to listOf("a", "b"),
            DataClass::h to mapOf("c" to 0, "d" to true),

            DataClass::i to listOf(1),
            DataClass::j to listOf(2L),
            DataClass::k to listOf(3.1F),
            DataClass::l to listOf(4.2),
            DataClass::m to listOf(true),
            DataClass::n to listOf("text"),
            DataClass::o to listOf(listOf("a", "b")),
            DataClass::p to listOf(mapOf("c" to 0, "d" to true)),

            DataClass::q to null,
        )

        assertFalse(m.containsKey(DataClass::q.name))
        assertNull(m.getInt(DataClass::q))
        assertNull(m.getString(DataClass::q))
        assertNull(m.getFloat(DataClass::a))

        assertEquals(1, m["a"])
        assertEquals(1, m.getInt(DataClass::a))
        assertEquals(2L, m.getLong(DataClass::b))
        assertEquals(3.1F, m.getFloat(DataClass::c))
        assertEquals(4.2, m.getDouble(DataClass::d))
        assertEquals(true, m.getBoolean(DataClass::e))
        assertEquals("text", m.getString(DataClass::f))
        assertEquals(listOf("a", "b"), m.getList(DataClass::g))
        assertEquals(mapOf("c" to 0, "d" to true), m.getMap(DataClass::h))

        assertEquals(listOf(1), m.getInts(DataClass::i))
        assertEquals(listOf(2L), m.getLongs(DataClass::j))
        assertEquals(listOf(3.1F), m.getFloats(DataClass::k))
        assertEquals(listOf(4.2), m.getDoubles(DataClass::l))
        assertEquals(listOf(true), m.getBooleans(DataClass::m))
        assertEquals(listOf("text"), m.getStrings(DataClass::n))
        assertEquals(listOf(listOf("a", "b")), m.getLists(DataClass::o))
        assertEquals(listOf(mapOf("c" to 0, "d" to true)), m.getMaps(DataClass::p))

        assertEquals(1, m.requireInt(DataClass::a))
        assertEquals(2L, m.requireLong(DataClass::b))
        assertEquals(3.1F, m.requireFloat(DataClass::c))
        assertEquals(4.2, m.requireDouble(DataClass::d))
        assertEquals(true, m.requireBoolean(DataClass::e))
        assertEquals("text", m.requireString(DataClass::f))
        assertEquals(listOf("a", "b"), m.requireList(DataClass::g))
        assertEquals(mapOf("c" to 0, "d" to true), m.requireMap(DataClass::h))
        assertEquals(mapOf("c" to 0, "d" to true), m["h"])

        assertEquals(listOf(1), m.requireInts(DataClass::i))
        assertEquals(listOf(2L), m.requireLongs(DataClass::j))
        assertEquals(listOf(3.1F), m.requireFloats(DataClass::k))
        assertEquals(listOf(4.2), m.requireDoubles(DataClass::l))
        assertEquals(listOf(true), m.requireBooleans(DataClass::m))
        assertEquals(listOf("text"), m.requireStrings(DataClass::n))
        assertEquals(listOf(listOf("a", "b")), m.requireLists(DataClass::o))
        assertEquals(listOf(mapOf("c" to 0, "d" to true)), m.requireMaps(DataClass::p))

        val exception = assertFailsWith<IllegalStateException> { m.requireKey<Int>(DataClass::q) }
        assertEquals("'q' key not found, or wrong type (must be kotlin.Int)", exception.message)
    }

    @Test fun `Utilities to map data lists of objects works correctly`() {
        data class DataClass(
            val g: List<*>,
            val h: Map<*, *>,
            val i: List<Int>,
            val j: List<Long>,
            val k: List<Float>,
            val l: List<Double>,
            val m: List<Boolean>,
            val n: List<String>,
            val o: List<List<*>>,
            val p: List<Map<*, *>>,
            val q: Int,
        )

        val m = fieldsMapOfNotNull(
            DataClass::g to listOf("a", "b"),
            DataClass::h to mapOf("c" to 0, "d" to true),
            DataClass::i to listOf(1),
            DataClass::j to listOf(2L),
            DataClass::k to listOf(3.1F),
            DataClass::l to listOf(4.2),
            DataClass::m to listOf(true),
            DataClass::n to listOf("text"),
            DataClass::o to listOf(listOf("a", "b")),
            DataClass::p to listOf(mapOf("c" to 0, "d" to true)),
        )

        assertEquals(listOf("a", "b"), m.getListOrEmpty(DataClass::g))
        assertEquals(mapOf("c" to 0, "d" to true), m.getMapOrEmpty(DataClass::h))
        assertEquals(listOf(1), m.getIntsOrEmpty(DataClass::i))
        assertEquals(listOf(2L), m.getLongsOrEmpty(DataClass::j))
        assertEquals(listOf(3.1F), m.getFloatsOrEmpty(DataClass::k))
        assertEquals(listOf(4.2), m.getDoublesOrEmpty(DataClass::l))
        assertEquals(listOf(true), m.getBooleansOrEmpty(DataClass::m))
        assertEquals(listOf("text"), m.getStringsOrEmpty(DataClass::n))
        assertEquals(listOf(listOf("a", "b")), m.getListsOrEmpty(DataClass::o))
        assertEquals(listOf(mapOf("c" to 0, "d" to true)), m.getMapsOrEmpty(DataClass::p))

        val m2= fieldsMapOfNotNull(DataClass::q to 1)

        assertEquals(emptyList<Any>(), m2.getListOrEmpty(DataClass::g))
        assertEquals(emptyMap<String, Any>(), m2.getMapOrEmpty(DataClass::h))
        assertEquals(emptyList(), m2.getIntsOrEmpty(DataClass::i))
        assertEquals(emptyList(), m2.getLongsOrEmpty(DataClass::j))
        assertEquals(emptyList(), m2.getFloatsOrEmpty(DataClass::k))
        assertEquals(emptyList(), m2.getDoublesOrEmpty(DataClass::l))
        assertEquals(emptyList(), m2.getBooleansOrEmpty(DataClass::m))
        assertEquals(emptyList(), m2.getStringsOrEmpty(DataClass::n))
        assertEquals(emptyList(), m2.getListsOrEmpty(DataClass::o))
        assertEquals(emptyList(), m2.getMapsOrEmpty(DataClass::p))
    }

    @Test fun `All pairs from a lists map is created properly`() {
        assertEquals(
            listOf(
                "a" to 1,
                "a" to 2,
                "a" to 3,
                "b" to 4,
                "b" to 5,
                "b" to 6,
            ),
            mapOf("a" to listOf(1, 2, 3), "b" to listOf(4, 5, 6)).pairs()
        )
        assertEquals(
            listOf(
                "a" to 1,
                "a" to 2,
                "a" to 3,
            ),
            mapOf("a" to listOf(1, 2, 3)).pairs()
        )
        assertEquals(
            listOf(
                "a" to 1,
                "b" to 4,
            ),
            mapOf("a" to listOf(1), "b" to listOf(4)).pairs()
        )
        assertEquals(listOf(), mapOf("a" to listOf<Int>()).pairs())
        assertEquals(listOf(), mapOf("a" to listOf<Int>(), "b" to listOf()).pairs())
    }
}
