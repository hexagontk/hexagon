package com.hexagonkt.core

import kotlin.test.*

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

    @Test fun `Utilities to map data objects work correctly`() {
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
        assertNull(m[DataClass::q.name])

        assertEquals(1, m["a"])
        assertEquals(1, m[DataClass::a.name])
        assertEquals(2L, m[DataClass::b.name])
        assertEquals(3.1F, m[DataClass::c.name])
        assertEquals(4.2, m[DataClass::d.name])
        assertEquals(true, m[DataClass::e.name])
        assertEquals("text", m[DataClass::f.name])
        assertEquals(listOf("a", "b"), m[DataClass::g.name])
        assertEquals(mapOf("c" to 0, "d" to true), m[DataClass::h.name])

        assertEquals(listOf(1), m[DataClass::i.name])
        assertEquals(listOf(2L), m[DataClass::j.name])
        assertEquals(listOf(3.1F), m[DataClass::k.name])
        assertEquals(listOf(4.2), m[DataClass::l.name])
        assertEquals(listOf(true), m[DataClass::m.name])
        assertEquals(listOf("text"), m[DataClass::n.name])
        assertEquals(listOf(listOf("a", "b")), m[DataClass::o.name])
        assertEquals(listOf(mapOf("c" to 0, "d" to true)), m[DataClass::p.name])
    }

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

    @Test fun `Utilities to map not null values work correctly`() {
        assertEquals(
            mapOf(
                "a" to 1,
                "b" to true,
                "c" to 'c',
            ),
            mapOfNotNull(
                "a" to 1,
                "b" to true,
                "c" to 'c',
                "d" to null,
            )
        )
    }
}
