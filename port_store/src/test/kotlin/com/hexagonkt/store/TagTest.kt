package com.hexagonkt.store

import org.testng.annotations.Test
import java.lang.System.currentTimeMillis

@Test class TagTest : ObjectIdRepositoryTest<Tag>(Tag::class, Tag::id) {
    override fun createObject() = Tag(name = "Tag")
    override fun changeObject(obj: Tag) = obj.copy(name = "${obj.name} ${currentTimeMillis()}")

    override val testObjects = listOf(
        Tag(name = "foo"),
        Tag(name = "bar")
    )

    fun check() {
        performing_crud_operations_with_lists_of_objects_behaves_as_expected()

        one_object_is_stored_and_loaded_without_error()
        many_objects_are_stored_and_loaded_without_error()
        replace_object_stores_modified_data_in_db()
        find_and_replace_object_stores_modified_data_in_db()

        `Object is mapped and parsed back without error`()
    }
}
