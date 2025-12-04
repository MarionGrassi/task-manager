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
import com.personal.usecase.commands.CreateTaskCommand
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import java.util.UUID

@DisplayName("CreateTaskApplicationService.execute(...)")
class CreateTaskApplicationServiceTest {
    private lateinit var taskRepository: TaskRepositoryPort
    private lateinit var service: CreateTaskApplicationService

    @BeforeEach
    fun setUp() {
        taskRepository = mockk()
        service = CreateTaskApplicationService(taskRepository)
    }

    @Nested
    @DisplayName("when the command is valid")
    inner class ValidCommand {
        @Test
        @DisplayName("should create and save the task successfully")
        fun `should create and save task with valid data`() {
            // Given
            val command =
                CreateTaskCommand(
                    label = "Test Task",
                    description = "Test Description",
                    completed = false,
                )

            val taskId = TaskId.fromUUID(UUID.randomUUID())
            val expectedTask =
                Task
                    .create(
                        taskId = taskId,
                        label = "Test Task",
                        description = "Test Description",
                        completed = false,
                    ).getOrElse { error("Should not fail") }

            every { taskRepository.save(any()) } returns expectedTask

            // When
            val result = service.execute(command)

            // Then
            assertTrue(result.isOk)
            result.fold(
                { response ->
                    assertEquals(taskId.uuid, response.id)
                    assertEquals("Test Task", response.label)
                    assertEquals("Test Description", response.description)
                    assertEquals(false, response.completed)
                },
                { _ -> fail("Expected Ok, but got Err") },
            )

            verify(exactly = 1) { taskRepository.save(any()) }
        }

        @Test
        @DisplayName("should trim label and description before creating task")
        fun `should trim whitespace from label and description`() {
            // Given
            val command =
                CreateTaskCommand(
                    label = "  Test Task  ",
                    description = "  Test Description  ",
                    completed = true,
                )

            val taskId = TaskId.fromUUID(UUID.randomUUID())
            val expectedTask =
                Task
                    .create(
                        taskId = taskId,
                        label = "Test Task",
                        description = "Test Description",
                        completed = true,
                    ).getOrElse { error("Should not fail") }

            every { taskRepository.save(any()) } returns expectedTask

            // When
            val result = service.execute(command)

            // Then
            assertTrue(result.isOk)
            result.fold(
                { response ->
                    assertEquals("Test Task", response.label)
                    assertEquals("Test Description", response.description)
                    assertEquals(true, response.completed)
                },
                { _ -> fail("Expected Ok, but got Err") },
            )

            verify(exactly = 1) { taskRepository.save(any()) }
        }

        @Test
        @DisplayName("should create task with completed status true")
        fun `should create task with completed status`() {
            // Given
            val command =
                CreateTaskCommand(
                    label = "Completed Task",
                    description = "Already completed",
                    completed = true,
                )

            val taskId = TaskId.fromUUID(UUID.randomUUID())
            val expectedTask =
                Task
                    .create(
                        taskId = taskId,
                        label = "Completed Task",
                        description = "Already completed",
                        completed = true,
                    ).getOrElse { error("Should not fail") }

            every { taskRepository.save(any()) } returns expectedTask

            // When
            val result = service.execute(command)

            // Then
            assertTrue(result.isOk)
            result.fold(
                { response ->
                    assertEquals(true, response.completed)
                },
                { _ -> fail("Expected Ok, but got Err") },
            )
        }
    }

    @Nested
    @DisplayName("when the label is invalid")
    inner class InvalidLabel {
        @Test
        @DisplayName("should return Err(UseCaseError.DomainValidation) for blank label")
        fun `should return error when label is blank`() {
            // Given
            val command =
                CreateTaskCommand(
                    label = "   ",
                    description = "Valid Description",
                    completed = false,
                )

            // When
            val result = service.execute(command)

            // Then
            assertTrue(result.isErr)
            result.fold(
                { _ -> fail("Expected Err, but got Ok") },
                { error ->
                    assertTrue(error is UseCaseError.DomainValidation)
                    val domainError = error as UseCaseError.DomainValidation
                    assertEquals("INVALID_TASK_LABEL", domainError.code)
                    assertEquals("The task label cannot be blank.", domainError.message)
                },
            )

            verify(exactly = 0) { taskRepository.save(any()) }
        }

        @Test
        @DisplayName("should return Err(UseCaseError.DomainValidation) for empty label")
        fun `should return error when label is empty`() {
            // Given
            val command =
                CreateTaskCommand(
                    label = "",
                    description = "Valid Description",
                    completed = false,
                )

            // When
            val result = service.execute(command)

            // Then
            assertTrue(result.isErr)
            result.fold(
                { _ -> fail("Expected Err, but got Ok") },
                { error ->
                    assertTrue(error is UseCaseError.DomainValidation)
                    val domainError = error as UseCaseError.DomainValidation
                    assertEquals("INVALID_TASK_LABEL", domainError.code)
                },
            )
        }
    }

    @Nested
    @DisplayName("when the description is invalid")
    inner class InvalidDescription {
        @Test
        @DisplayName("should return Err(UseCaseError.DomainValidation) for blank description")
        fun `should return error when description is blank`() {
            // Given
            val command =
                CreateTaskCommand(
                    label = "Valid Label",
                    description = "   ",
                    completed = false,
                )

            // When
            val result = service.execute(command)

            // Then
            assertTrue(result.isErr)
            result.fold(
                { _ -> fail("Expected Err, but got Ok") },
                { error ->
                    assertTrue(error is UseCaseError.DomainValidation)
                    val domainError = error as UseCaseError.DomainValidation
                    assertEquals("INVALID_TASK_DESCRIPTION", domainError.code)
                    assertEquals("The task description cannot be blank.", domainError.message)
                },
            )

            verify(exactly = 0) { taskRepository.save(any()) }
        }

        @Test
        @DisplayName("should return Err(UseCaseError.DomainValidation) for empty description")
        fun `should return error when description is empty`() {
            // Given
            val command =
                CreateTaskCommand(
                    label = "Valid Label",
                    description = "",
                    completed = false,
                )

            // When
            val result = service.execute(command)

            // Then
            assertTrue(result.isErr)
            result.fold(
                { _ -> fail("Expected Err, but got Ok") },
                { error ->
                    assertTrue(error is UseCaseError.DomainValidation)
                    val domainError = error as UseCaseError.DomainValidation
                    assertEquals("INVALID_TASK_DESCRIPTION", domainError.code)
                },
            )
        }
    }
}
