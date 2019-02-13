package com.hexagonkt.serialization

import com.hexagonkt.helpers.Resource
import org.testng.annotations.Test

@Test class JacksonSerializerTest {
    fun `Parse URL works ok`() {
        assert(Resource("data/companies.json").requireUrl().parseList().isNotEmpty())
        assert(Resource("data/tags.json").requireUrl().parseList().isNotEmpty())
        assert(Resource("data/companies.yaml").requireUrl().parseList().isNotEmpty())
        assert(Resource("data/tags.yaml").requireUrl().parseList().isNotEmpty())

        assert(Resource("data/company.json").requireUrl().parse().isNotEmpty())
        assert(Resource("data/tag.json").requireUrl().parse().isNotEmpty())
        assert(Resource("data/company.yaml").requireUrl().parse().isNotEmpty())
        assert(Resource("data/tag.yaml").requireUrl().parse().isNotEmpty())
    }

    fun `Parse resource works ok`() {
        assert(Resource("data/companies.json").parseList().isNotEmpty())
        assert(Resource("data/tags.json").parseList().isNotEmpty())
        assert(Resource("data/companies.yaml").parseList().isNotEmpty())
        assert(Resource("data/tags.yaml").parseList().isNotEmpty())

        assert(Resource("data/company.json").parse().isNotEmpty())
        assert(Resource("data/tag.json").parse().isNotEmpty())
        assert(Resource("data/company.yaml").parse().isNotEmpty())
        assert(Resource("data/tag.yaml").parse().isNotEmpty())
    }
}
