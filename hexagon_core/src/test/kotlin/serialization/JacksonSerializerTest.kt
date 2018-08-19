package com.hexagonkt.serialization

import com.hexagonkt.helpers.requireResource
import org.testng.annotations.Test

@Test class JacksonSerializerTest {
    fun `Parse resource works ok`() {
        assert(requireResource("data/companies.json").parseList().isNotEmpty())
        assert(requireResource("data/tags.json").parseList().isNotEmpty())
        assert(requireResource("data/companies.yaml").parseList().isNotEmpty())
        assert(requireResource("data/tags.yaml").parseList().isNotEmpty())

        assert(requireResource("data/company.json").parse().isNotEmpty())
        assert(requireResource("data/tag.json").parse().isNotEmpty())
        assert(requireResource("data/company.yaml").parse().isNotEmpty())
        assert(requireResource("data/tag.yaml").parse().isNotEmpty())
    }
}
