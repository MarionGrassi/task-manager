package com.personal.usecase.service

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.personal.domain.model.Task
import com.personal.domain.model.TaskId
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import java.util.UUID

@DisplayName("GetTaskByIdApplicationService.execute(...)")
class GetTaskByIdApplicationServiceTest {
    private lateinit var taskRepository: TaskRepositoryPort
    private lateinit var service: GetTaskByIdApplicationService

    @BeforeEach
    fun setUp() {
        taskRepository = mockk()
        service = GetTaskByIdApplicationService(taskRepository)
    }

    @Test
    @DisplayName("should return TaskResponse with correct data")
    fun `should return task when found`() {
        // Given
        val taskUuid = UUID.randomUUID()
        val taskId = TaskId.fromUUID(taskUuid)
        val existingTask =
            Task
                .create(
                    taskId = taskId,
                    label = "Test Task",
                    description = "Test Description",
                    completed = false,
                ).getOrElse { error("Should not fail") }

        every { taskRepository.findById(taskId) } returns existingTask

        // When
        val result = service.execute(taskUuid)

        // Then
        assertTrue(result.isOk)
        result.fold(
            { response ->
                assertEquals(taskUuid, response.id)
                assertEquals("Test Task", response.label)
                assertEquals("Test Description", response.description)
                assertEquals(false, response.completed)
            },
            { _ -> fail("Expected Ok, but got Err") },
        )

        verify(exactly = 1) { taskRepository.findById(taskId) }
    }

    @Test
    @DisplayName("should return completed task correctly")
    fun `should return completed task with correct status`() {
        // Given
        val taskUuid = UUID.randomUUID()
        val taskId = TaskId.fromUUID(taskUuid)
        val completedTask =
            Task
                .create(
                    taskId = taskId,
                    label = "Completed Task",
                    description = "Already done",
                    completed = true,
                ).getOrElse { error("Should not fail") }

        every { taskRepository.findById(taskId) } returns completedTask

        // When
        val result = service.execute(taskUuid)

        // Then
        assertTrue(result.isOk)
        result.fold(
            { response ->
                assertEquals(taskUuid, response.id)
                assertEquals("Completed Task", response.label)
                assertEquals("Already done", response.description)
                assertEquals(true, response.completed)
            },
            { _ -> fail("Expected Ok, but got Err") },
        )

        verify(exactly = 1) { taskRepository.findById(taskId) }
    }

    @Test
    @DisplayName("should handle task with long description")
    fun `should return task with long description`() {
        // Given
        val taskUuid = UUID.randomUUID()
        val taskId = TaskId.fromUUID(taskUuid)
        val longDescription =
            "This is a very long description that contains multiple sentences. " +
                    "It might span several lines and contain detailed information about the task. " +
                    "The system should handle this correctly."

        val task =
            Task
                .create(
                    taskId = taskId,
                    label = "Task with long description",
                    description = longDescription,
                    completed = false,
                ).getOrElse { error("Should not fail") }

        every { taskRepository.findById(taskId) } returns task

        // When
        val result = service.execute(taskUuid)

        // Then
        assertTrue(result.isOk)
        result.fold(
            { response ->
                assertEquals(longDescription, response.description)
            },
            { _ -> fail("Expected Ok, but got Err") },
        )

        verify(exactly = 1) { taskRepository.findById(taskId) }
    }

    @Test
    @DisplayName("should handle task with special characters in label and description")
    fun `should return task with special characters`() {
        // Given
        val taskUuid = UUID.randomUUID()
        val taskId = TaskId.fromUUID(taskUuid)
        val task =
            Task
                .create(
                    taskId = taskId,
                    label = "Task @#$% with special chars!",
                    description = "Description with émojis 🎉 and symbols €£¥",
                    completed = false,
                ).getOrElse { error("Should not fail") }

        every { taskRepository.findById(taskId) } returns task

        // When
        val result = service.execute(taskUuid)

        // Then
        assertTrue(result.isOk)
        result.fold(
            { response ->
                assertEquals("Task @#$% with special chars!", response.label)
                assertEquals("Description with émojis 🎉 and symbols €£¥", response.description)
            },
            { _ -> fail("Expected Ok, but got Err") },
        )

        verify(exactly = 1) { taskRepository.findById(taskId) }
    }

    @Test
    @DisplayName("should return Err(UseCaseError.TaskNotFound)")
    fun `should return TaskNotFound error when task does not exist`() {
        // Given
        val nonExistentTaskUuid = UUID.randomUUID()
        val taskId = TaskId.fromUUID(nonExistentTaskUuid)

        every { taskRepository.findById(taskId) } returns null

        // When
        val result = service.execute(nonExistentTaskUuid)

        // Then
        assertTrue(result.isErr)
        result.fold(
            { _ -> fail("Expected Err, but got Ok") },
            { error ->
                assertEquals(UseCaseError.TaskNotFound, error)
                assertEquals("TASK_NOT_FOUND", error.code)
                assertEquals("The task was not found.", error.message)
            },
        )

        verify(exactly = 1) { taskRepository.findById(taskId) }
    }
}
