package com.hexagonkt.serialization.json

import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.parseObjects
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URL

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JacksonSerializerTest {

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.formats = linkedSetOf(Json)
    }

    @Test fun `Parse URL works ok`() {
        assert(URL("classpath:data/companies.json").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tags.json").parseObjects<Map<*, *>>().isNotEmpty())

        assert(URL("classpath:data/company.json").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tag.json").parse<Map<*, *>>().isNotEmpty())
    }

    @Test fun `Parse resource works ok`() {
        assert(URL("classpath:data/companies.json").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tags.json").parseObjects<Map<*, *>>().isNotEmpty())

        assert(URL("classpath:data/company.json").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tag.json").parse<Map<*, *>>().isNotEmpty())
    }
}
