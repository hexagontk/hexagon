package co.there4.hexagon.serialization

import co.there4.hexagon.helpers.requireResource
import org.testng.annotations.Test
import kotlin.test.assertFailsWith

@Test class JacksonSerializerTest {
    fun serializing_an_unsupported_content_type_fails() {
        assertFailsWith<IllegalStateException> {
            JacksonSerializer.serialize("text", "invalid/type")
        }
    }

    fun parse_resource_works_ok() {
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
