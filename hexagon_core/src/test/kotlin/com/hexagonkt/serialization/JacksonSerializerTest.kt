package com.hexagonkt.serialization

import com.hexagonkt.helpers.Resource
import org.testng.annotations.Test

@Test class JacksonSerializerTest {
    fun `Parse URL works ok`() {
        assert(Resource("data/companies.json").requireUrl().parseList<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tags.json").requireUrl().parseList<Map<*, *>>().isNotEmpty())
        assert(Resource("data/companies.yaml").requireUrl().parseList<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tags.yaml").requireUrl().parseList<Map<*, *>>().isNotEmpty())

        assert(Resource("data/company.json").requireUrl().parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tag.json").requireUrl().parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/company.yaml").requireUrl().parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tag.yaml").requireUrl().parse<Map<*, *>>().isNotEmpty())
    }

    fun `Parse resource works ok`() {
        assert(Resource("data/companies.json").parseList<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tags.json").parseList<Map<*, *>>().isNotEmpty())
        assert(Resource("data/companies.yaml").parseList<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tags.yaml").parseList<Map<*, *>>().isNotEmpty())

        assert(Resource("data/company.json").parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tag.json").parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/company.yaml").parse<Map<*, *>>().isNotEmpty())
        assert(Resource("data/tag.yaml").parse<Map<*, *>>().isNotEmpty())
    }
}
