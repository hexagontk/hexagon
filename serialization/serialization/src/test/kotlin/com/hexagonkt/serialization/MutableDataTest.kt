package com.hexagonkt.serialization

import com.hexagonkt.core.fieldsMapOf
import com.hexagonkt.core.requirePath
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MutableDataTest {

    data class MutableTask(
        var number: Int = 0,
        var title: String = "",
        var description: String = ""
    ) : MutableData {

        override fun data(): Map<String, *> =
            fieldsMapOf(
                MutableTask::description to description,
                MutableTask::number to number,
                MutableTask::title to title,
            )

        override fun with(data: Map<String, *>) {
            description = data.requirePath(MutableTask::description)
            number = data.requirePath(MutableTask::number)
            title = data.requirePath(MutableTask::title)
        }
    }

    @Test fun `Mutable data use case`() {
        val task = MutableTask()

        assertEquals(
            fieldsMapOf(
                MutableTask::description to task.description,
                MutableTask::number to task.number,
                MutableTask::title to task.title,
            ),
            task.data()
        )

        task.number = 1

        assertEquals(
            fieldsMapOf(
                MutableTask::description to task.description,
                MutableTask::number to task.number,
                MutableTask::title to task.title,
            ),
            task.data()
        )

        task.with(
            fieldsMapOf(
                MutableTask::description to "description",
                MutableTask::title to "title",
                MutableTask::number to task.number,
            )
        )

        assertEquals(
            fieldsMapOf(
                MutableTask::description to task.description,
                MutableTask::number to task.number,
                MutableTask::title to task.title,
            ),
            task.data()
        )

        assertEquals(1, task[MutableTask::number.name])
        assertEquals("description", task[MutableTask::description.name])
        assertEquals("title", task[MutableTask::title.name])
    }
}
