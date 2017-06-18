package co.there4.hexagon.store

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.*
import org.bson.conversions.Bson
import org.testng.annotations.Test

@Test class RepositoryPackageTest {
    data class Example(val foo: String, var bar: Int)

    private infix fun Bson.sameAs(other: Bson) = this.toString() == other.toString()

    fun comparison_filters_work_as_expected() {
        assert(Example::foo eq "bar" sameAs eq("foo", "bar"))
        assert(Example::bar gte 1 sameAs gte("bar", 1))
        assert(Example::bar gt 1 sameAs gt("bar", 1))
        assert(Example::bar lte 1 sameAs lte("bar", 1))
        assert(Example::bar lt 1 sameAs lt("bar", 1))
        assert(Example::bar isIn listOf(1, 2) sameAs Filters.`in`("bar", listOf(1, 2)))
    }
}
