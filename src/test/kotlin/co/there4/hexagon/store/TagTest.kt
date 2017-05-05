package co.there4.hexagon.store

import org.testng.annotations.Test
import java.lang.System.currentTimeMillis

@Test class TagTest : ObjectIdRepositoryTest<Tag>(Tag::class, Tag::id) {
    override fun createObject() = Tag(name = "Tag")
    override fun changeObject(obj: Tag) = obj.copy(name = "${obj.name} ${currentTimeMillis()}")

    override val testObjects = listOf(
        Tag(name = "foo"),
        Tag(name = "bar")
    )
}
