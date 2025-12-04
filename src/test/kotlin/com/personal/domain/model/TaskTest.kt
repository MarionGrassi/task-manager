package com.personal.domain.model

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import com.personal.domain.errors.DomainError
import org.junit.jupiter.api.Test
import kotlin.test.DefaultAsserter.fail
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TaskTest {

    @Test
    fun `create returns Ok when inputs are valid`() {
        val result = Task.create(
            label = "Do laundry",
            description = "Wash and dry clothes"
        )

        assertTrue(result.isOk)
        result.fold(
            { task ->
                assertEquals("Do laundry", task.label)
                assertEquals("Wash and dry clothes", task.description)
                assertFalse(task.completed)
            },
            { _ -> fail("Expected Ok, but got Err") },
        )
    }

    @Test
    fun `create returns Err when label is blank`() {
        val result = Task.create(
            label = " ",
            description = "valid"
        )

        assertTrue(result.isErr)
        result.fold(
            { _ ->
                fail("Expected Err, but got Ok")
            },
            { error ->
                assertEquals(DomainError.InvalidTaskLabel, error)
            },
        )
    }

    @Test
    fun `create returns Err when description is blank`() {
        val result = Task.create(
            label = "valid",
            description = " "
        )

        assertTrue(result.isErr)
        result.fold(
            { _ ->
                fail("Expected Err, but got Ok")
            },
            { error ->
                assertEquals(DomainError.InvalidTaskDescription, error)
            },
        )
    }

    @Test
    fun `rehydrate returns a valid Task when inputs are valid`() {
        val taskId = TaskId.generate()

        val task = Task.rehydrate(
            taskId = taskId,
            label = "Existing",
            description = "Existing desc",
            completed = true
        )

        assertEquals(taskId, task.taskId)
        assertEquals("Existing", task.label)
        assertEquals("Existing desc", task.description)
        assertTrue(task.completed)
    }

    @Test
    fun `rehydrate throws IllegalStateException when label is blank`() {
        assertFailsWith<IllegalStateException> {
            Task.rehydrate(
                taskId = TaskId.generate(),
                label = "",
                description = "Desc",
                completed = false
            )
        }
    }

    @Test
    fun `rehydrate throws IllegalStateException when description is blank`() {
        assertFailsWith<IllegalStateException> {
            Task.rehydrate(
                taskId = TaskId.generate(),
                label = "Task",
                description = "",
                completed = false
            )
        }
    }
}
