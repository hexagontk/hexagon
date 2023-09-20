package com.hexagonkt.core

import kotlin.test.Test
import kotlin.IllegalArgumentException
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ChecksTest {

    data class DataClass(
        val text: String? = "text",
        val texts: List<String>? = listOf("text 1", "text 2"),
        val date: LocalDate? = LocalDate.now(),
        val integer: Int? = 1,
        val decimal: Double? = 1.0,
        val date2: LocalDate? = date?.plusDays(1),
        val integer2: Int? = integer?.let { it + 1 },
        val decimal2: Double? = decimal?.let { it + 1.0 },
    )

    @Test fun `Require methods work properly`() {
        val data = DataClass()
        data.requireNotBlank(DataClass::text)
        data.requireNotBlanks(DataClass::texts)
        data.requireLower(DataClass::date, LocalDate.now().plusDays(1))
        data.requireGreater(DataClass::date, LocalDate.now().minusDays(1))
        data.requireLowerOrEquals(DataClass::date, LocalDate.now().plusDays(1))
        data.requireGreaterOrEquals(DataClass::date, LocalDate.now().minusDays(1))
        data.requireLowerOrEquals(DataClass::date, LocalDate.now())
        data.requireGreaterOrEquals(DataClass::date, LocalDate.now())
        data.requireGreater(DataClass::integer, 0)
        data.requireGreater(DataClass::decimal, 0.0)
        data.requireLower(DataClass::integer, 2)
        data.requireLower(DataClass::decimal, 2.0)
        data.requireGreaterOrEquals(DataClass::integer, 0)
        data.requireGreaterOrEquals(DataClass::decimal, 0.0)
        data.requireLowerOrEquals(DataClass::integer, 2)
        data.requireLowerOrEquals(DataClass::decimal, 2.0)
        data.requireGreaterOrEquals(DataClass::integer, 1)
        data.requireGreaterOrEquals(DataClass::decimal, 1.0)
        data.requireLowerOrEquals(DataClass::integer, 1)
        data.requireLowerOrEquals(DataClass::decimal, 1.0)
        data.requireGreaterOrEquals(DataClass::integer, DataClass::integer)
        data.requireGreaterOrEquals(DataClass::decimal, DataClass::decimal)
        data.requireLowerOrEquals(DataClass::integer, DataClass::integer)
        data.requireLowerOrEquals(DataClass::decimal, DataClass::decimal)
        data.requireLower(DataClass::integer, DataClass::integer2)
        data.requireLower(DataClass::decimal, DataClass::decimal2)
        data.requireGreater(DataClass::integer2, DataClass::integer)
        data.requireGreater(DataClass::decimal2, DataClass::decimal)
    }

    @Test fun `Require methods throw exceptions for incorrect data`() {
        val data = DataClass(
            text = " ",
            texts = listOf("text", " "),
            integer = 0,
            decimal = 0.0,
        )

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        fail("'text' cannot be blank") { data.requireNotBlank(DataClass::text) }
        fail("'texts' cannot contain blanks") { data.requireNotBlanks(DataClass::texts) }
        fail("'date' must be lower than $today: $today") {
            data.requireLower(DataClass::date, today)
        }
        fail("'date' must be equals or lower than $yesterday: $today") {
            data.requireLowerOrEquals(DataClass::date, yesterday)
        }
        fail("'integer' must be greater than 0: 0") {
            data.requireGreater(DataClass::integer, 0)
        }
        fail("'decimal' must be greater than 0.0: 0.0") {
            data.requireGreater(DataClass::decimal, 0.0)
        }
        fail("'integer' must be equals or greater than 1: 0") {
            data.requireGreaterOrEquals(DataClass::integer, 1)
        }
        fail("'decimal' must be equals or greater than 1.0: 0.0") {
            data.requireGreaterOrEquals(DataClass::decimal, 1.0)
        }
    }

    @Test fun `Require methods work properly with 'null' data`() {
        val data = DataClass(null, null, null, null, null)
        data.requireNotBlank(DataClass::text)
        data.requireNotBlanks(DataClass::texts)
        data.requireLower(DataClass::date, LocalDate.now().plusDays(1))
        data.requireGreater(DataClass::date, LocalDate.now().minusDays(1))
        data.requireLowerOrEquals(DataClass::date, LocalDate.now().plusDays(1))
        data.requireGreaterOrEquals(DataClass::date, LocalDate.now().minusDays(1))
        data.requireLowerOrEquals(DataClass::date, LocalDate.now())
        data.requireGreaterOrEquals(DataClass::date, LocalDate.now())
        data.requireGreater(DataClass::integer, 0)
        data.requireGreater(DataClass::decimal, 0.0)
        data.requireLower(DataClass::integer, 2)
        data.requireLower(DataClass::decimal, 2.0)
        data.requireGreaterOrEquals(DataClass::integer, 0)
        data.requireGreaterOrEquals(DataClass::decimal, 0.0)
        data.requireLowerOrEquals(DataClass::integer, 2)
        data.requireLowerOrEquals(DataClass::decimal, 2.0)
        data.requireGreaterOrEquals(DataClass::integer, 1)
        data.requireGreaterOrEquals(DataClass::decimal, 1.0)
        data.requireLowerOrEquals(DataClass::integer, 1)
        data.requireLowerOrEquals(DataClass::decimal, 1.0)
        data.requireGreaterOrEquals(DataClass::integer, DataClass::integer)
        data.requireGreaterOrEquals(DataClass::decimal, DataClass::decimal)
        data.requireLowerOrEquals(DataClass::integer, DataClass::integer)
        data.requireLowerOrEquals(DataClass::decimal, DataClass::decimal)
        data.requireLower(DataClass::integer, DataClass::integer2)
        data.requireLower(DataClass::decimal, DataClass::decimal2)
        data.requireGreater(DataClass::integer2, DataClass::integer)
        data.requireGreater(DataClass::decimal2, DataClass::decimal)
    }

    @Test fun `Ensure fails if collection size is larger`() {
        assertFailsWith<IllegalStateException> {
            listOf(1, 2, 3).checkSize(1..2)
        }
    }

    @Test fun `Ensure fails if collection size is smaller`() {
        assertFailsWith<IllegalStateException> {
            listOf(1, 2, 3).checkSize(4..5)
        }
    }

    @Test fun `Ensure returns the collection if size is correct`() {
        val list = listOf(1, 2, 3)
        assert(list.checkSize(0..3) == list)
        assert(list.checkSize(1..3) == list)
        assert(list.checkSize(2..3) == list)
        assert(list.checkSize(3..3) == list)
        assert(list.checkSize(0..4) == list)
    }

    private fun fail(message: String, block: () -> Unit) {
        val e = assertFailsWith<IllegalArgumentException>(block = block)
        assertEquals(message, e.message)
    }
}
