package com.hexagonkt.serialization

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URL

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class YamlSerializerTest {

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.formats = linkedSetOf(Yaml)
    }

    @Test fun `Parse URL works ok`() {
        assert(URL("classpath:data/companies.yml").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tags.yml").parseObjects<Map<*, *>>().isNotEmpty())

        assert(URL("classpath:data/company.yml").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tag.yml").parse<Map<*, *>>().isNotEmpty())
    }

    @Test fun `Parse resource works ok`() {
        assert(URL("classpath:data/companies.yml").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tags.yml").parseObjects<Map<*, *>>().isNotEmpty())

        assert(URL("classpath:data/company.yml").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tag.yml").parse<Map<*, *>>().isNotEmpty())
    }
}
