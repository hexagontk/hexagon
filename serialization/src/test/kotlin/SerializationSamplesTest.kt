package com.hexagonkt.serialization

import com.hexagonkt.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SerializationSamplesTest {

    internal data class Person(
        val givenName: String,
        val familyName: String,
        val birthDate: LocalDate
    )

    @Test fun serializationUsage() {
        // serializationUsage
        SerializationManager.formats = linkedSetOf(Json) // Loads JSON format (using it as default)
        val jason = Person("Jason", "Jackson", LocalDate.of(1989, 12, 31))

        val jasonJson = jason.serialize(Json) // Can also be Yaml or an string: "application/json"
        val parsedJason = jasonJson.parse(Person::class) // Uses default format (JSON)

        assert(jason == parsedJason)
        assert(jason !== parsedJason)
        // serializationUsage
    }
}
