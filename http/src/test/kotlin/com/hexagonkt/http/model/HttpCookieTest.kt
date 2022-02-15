package com.hexagonkt.http.model

import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class HttpCookieTest {

    @Test fun `Cookie name must not be blank`() {
        assertFailsWith<IllegalArgumentException> { HttpCookie(" ", "value") }
    }

    @Test fun `Cookie can be created`() {
        val cookie = HttpCookie("name", "value", 5, true)
        assertEquals("name", cookie.name)
        assertEquals("value", cookie.value)
        assertEquals(5, cookie.maxAge)
        assertTrue(cookie.secure)
    }

    @Test fun `Cookie can be deleted`() {
        val cookie = HttpCookie("name", "value", 5, true).delete()
        assertEquals("name", cookie.name)
        assertEquals("", cookie.value)
        assertEquals(0, cookie.maxAge)
        assertTrue(cookie.secure)
    }
}
