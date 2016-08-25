package co.there4.hexagon.serialization

import org.testng.annotations.Test
import kotlin.test.assertFailsWith

@Test class JacksonSerializerTest {
    fun serializing_an_unsupported_content_type_fails() {
        assertFailsWith<IllegalArgumentException> {
            JacksonSerializer.serialize("text", "invalid/type")
        }
    }

    fun parse_resource_works_ok() {
        assert(resourceParseList("data/companies.json").isNotEmpty())
        assert(resourceParseList("data/tags.json").isNotEmpty())
        assert(resourceParseList("data/companies.yaml").isNotEmpty())
        assert(resourceParseList("data/tags.yaml").isNotEmpty())

        assert(resourceParse("data/company.json").isNotEmpty())
        assert(resourceParse("data/tag.json").isNotEmpty())
        assert(resourceParse("data/company.yaml").isNotEmpty())
        assert(resourceParse("data/tag.yaml").isNotEmpty())
    }
}
