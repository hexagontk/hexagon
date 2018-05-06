package com.hexagonkt.vertx.serialization

import org.junit.Test
import java.lang.ClassLoader.getSystemClassLoader
import java.net.URL

class JacksonSerializerTest {
    private fun resource(resource: String): URL? = getSystemClassLoader().getResource(resource)

    private fun requireResource(resource: String): URL =
        resource(resource) ?: error("$resource not found")

    @Test fun `parse resource works ok`() {
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
