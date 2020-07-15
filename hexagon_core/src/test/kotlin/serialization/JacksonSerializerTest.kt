package com.hexagonkt.serialization

import com.hexagonkt.helpers.Resource
import org.junit.jupiter.api.Test

class JacksonSerializerTest {

    @Test fun `Parse URL works ok`() {
        assert(Resource("data/companies.json").requireUrl().parseObjects<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tags.json").requireUrl().parseObjects<Map<*, *>>().isNotEmpty())
        assert(Resource("data/companies.yml").requireUrl().parseObjects<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tags.yml").requireUrl().parseObjects<Map<*, *>>().isNotEmpty())

        assert(Resource("data/company.json").requireUrl().parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tag.json").requireUrl().parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/company.yml").requireUrl().parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tag.yml").requireUrl().parse<Map<*, *>>().isNotEmpty())
    }

    @Test fun `Parse resource works ok`() {
        assert(Resource("data/companies.json").parseObjects<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tags.json").parseObjects<Map<*, *>>().isNotEmpty())
        assert(Resource("data/companies.yml").parseObjects<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tags.yml").parseObjects<Map<*, *>>().isNotEmpty())

        assert(Resource("data/company.json").parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tag.json").parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/company.yml").parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tag.yml").parse<Map<*, *>>().isNotEmpty())
    }
}
