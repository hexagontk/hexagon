package com.hexagonkt.http.model

import com.hexagonkt.http.model.HttpStatusType.*
import kotlin.test.Test
import kotlin.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class HttpStatusTest {

    @Test fun `Custom status codes gets the proper type`() {
        HttpStatus(100).let {
            assertEquals(100, it.code)
            assertEquals(INFORMATION, it.type)
        }
        HttpStatus(199).let {
            assertEquals(199, it.code)
            assertEquals(INFORMATION, it.type)
        }
        HttpStatus(200).let {
            assertEquals(200, it.code)
            assertEquals(SUCCESS, it.type)
        }
        HttpStatus(299).let {
            assertEquals(299, it.code)
            assertEquals(SUCCESS, it.type)
        }
        HttpStatus(300).let {
            assertEquals(300, it.code)
            assertEquals(REDIRECTION, it.type)
        }
        HttpStatus(399).let {
            assertEquals(399, it.code)
            assertEquals(REDIRECTION, it.type)
        }
        HttpStatus(400).let {
            assertEquals(400, it.code)
            assertEquals(CLIENT_ERROR, it.type)
        }
        HttpStatus(499).let {
            assertEquals(499, it.code)
            assertEquals(CLIENT_ERROR, it.type)
        }
        HttpStatus(500).let {
            assertEquals(500, it.code)
            assertEquals(SERVER_ERROR, it.type)
        }
        HttpStatus(599).let {
            assertEquals(599, it.code)
            assertEquals(SERVER_ERROR, it.type)
        }
    }

    @Test fun `Custom status codes can override their proper type`() {
        HttpStatus(100, SUCCESS).let {
            assertEquals(100, it.code)
            assertEquals(SUCCESS, it.type)
        }
        HttpStatus(200, INFORMATION).let {
            assertEquals(200, it.code)
            assertEquals(INFORMATION, it.type)
        }
    }

    @Test fun `Invalid custom status codes throw exceptions on creation`() {
        assertFailsWith<IllegalArgumentException> { HttpStatus(99, INFORMATION)  }
        assertFailsWith<IllegalArgumentException> { HttpStatus(600, INFORMATION)  }
        assertFailsWith<IllegalArgumentException> { HttpStatus(99)  }
        assertFailsWith<IllegalArgumentException> { HttpStatus(600)  }
    }
}
