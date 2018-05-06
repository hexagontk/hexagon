package com.hexagonkt

import org.junit.Test

class StringsTest {
    @Test fun `Converting empty text to camel case fails` () {
        assert ("".snakeToCamel () == "")
    }

    @Test fun `Converting valid snake case texts to camel case succeed` () {
        assert ("alfa_beta".snakeToCamel () == "alfaBeta")
    }

    @Test fun `Converting valid camel case texts to snake case succeed` () {
        assert ("alfaBeta".camelToSnake () == "alfa_beta")
    }

    @Test fun `Normalize works as expected`() {
        val striped = "áéíóúñçÁÉÍÓÚÑÇ".stripAccents()
        assert(striped == "aeiouncAEIOUNC")
    }

    @Test fun `Eol is correct`() {
        assert(eol.contains('\n'))
    }
}
