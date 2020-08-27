package com.hexagonkt.serialization

import org.junit.jupiter.api.Test
import java.net.URL

class JacksonSerializerTest {

    @Test fun `Parse URL works ok`() {
        assert(URL("classpath:data/companies.json").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tags.json").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/companies.yml").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tags.yml").parseObjects<Map<*, *>>().isNotEmpty())

        assert(URL("classpath:data/company.json").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tag.json").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/company.yml").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tag.yml").parse<Map<*, *>>().isNotEmpty())
    }

    @Test fun `Parse resource works ok`() {
        assert(URL("classpath:data/companies.json").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tags.json").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/companies.yml").parseObjects<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tags.yml").parseObjects<Map<*, *>>().isNotEmpty())

        assert(URL("classpath:data/company.json").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tag.json").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/company.yml").parse<Map<*, *>>().isNotEmpty())
        assert(URL("classpath:data/tag.yml").parse<Map<*, *>>().isNotEmpty())
    }
}
