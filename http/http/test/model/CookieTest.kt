package com.hexagontk.http.model

import com.hexagontk.http.model.CookieSameSite.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class CookieTest {

    @Test fun `Cookie name must not be blank`() {
        assertFailsWith<IllegalArgumentException> { Cookie(" ", "value") }
    }

    @Test fun `Cookie can be created`() {
        val cookie = Cookie("name", "value", 5, true)
        assertEquals("name", cookie.name)
        assertEquals("value", cookie.value)
        assertEquals(5, cookie.maxAge)
        assertTrue(cookie.secure)
        assertFalse(cookie.deleted)

        assertEquals(LAX, cookie.with(sameSite = LAX).sameSite)
        assertEquals(NONE, cookie.with(sameSite = NONE).sameSite)
        assertEquals(STRICT, cookie.with(sameSite = STRICT).sameSite)
    }

    @Test fun `Cookie can be deleted`() {
        val cookie = Cookie("name", "value", 5, true).delete()
        assertEquals("name", cookie.name)
        assertEquals("", cookie.value)
        assertEquals(0, cookie.maxAge)
        assertTrue(cookie.secure)
        assertTrue(cookie.deleted)

        assertEqualCookies(cookie, Cookie("name", secure = true).delete())
    }
}
