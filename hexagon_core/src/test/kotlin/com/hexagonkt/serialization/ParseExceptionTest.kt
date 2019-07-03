package com.hexagonkt.serialization

import com.fasterxml.jackson.databind.JsonMappingException
import org.testng.annotations.Test
import java.lang.IllegalStateException

class ParseExceptionTest {
    @Test fun `ParseException obtains field name properly`() {
        assert(ParseException(null).field == "")
        assert(ParseException(IllegalStateException()).field == "")
        assert(ParseException(JsonMappingException(null, "message")).field == "")

        val jsonMappingException = JsonMappingException(null, "message")
        jsonMappingException.prependPath("type", "field")
        assert(ParseException(jsonMappingException).field == "java.lang.String[\"field\"]")
    }
}
