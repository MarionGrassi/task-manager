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
import com.personal.usecase.commands.CreateMultipleTasksCommand
import com.personal.usecase.commands.CreateTaskCommand
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import io.mockk.MockKMatcherScope
import java.util.UUID

@DisplayName("CreateMultipleTasksApplicationService.execute(...)")
class CreateMultipleTasksApplicationServiceTest {
  private lateinit var taskRepository: TaskRepositoryPort
  private lateinit var service: CreateMultipleTasksApplicationService

  @BeforeEach
  fun setUp() {
    taskRepository = mockk()
    service = CreateMultipleTasksApplicationService(taskRepository)
  }

  @Nested
  @DisplayName("when the command is valid")
  inner class ValidCommand {
    @Test
    @DisplayName("should create and save multiple tasks successfully")
    fun `should create and save multiple tasks with valid data`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "Task 1",
                  description = "Description 1",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "Task 2",
                  description = "Description 2",
                  completed = true,
              ),
              CreateTaskCommand(
                  label = "Task 3",
                  description = "Description 3",
                  completed = false,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

      val taskId1 = TaskId.fromUUID(UUID.randomUUID())
      val taskId2 = TaskId.fromUUID(UUID.randomUUID())
      val taskId3 = TaskId.fromUUID(UUID.randomUUID())

      val expectedTasks =
          listOf(
              Task
                  .create(
                      taskId = taskId1,
                      label = "Task 1",
                      description = "Description 1",
                      completed = false,
                  ).getOrElse { error("Should not fail") },
              Task
                  .create(
                      taskId = taskId2,
                      label = "Task 2",
                      description = "Description 2",
                      completed = true,
                  ).getOrElse { error("Should not fail") },
              Task
                  .create(
                      taskId = taskId3,
                      label = "Task 3",
                      description = "Description 3",
                      completed = false,
                  ).getOrElse { error("Should not fail") },
          )

      every { taskRepository.saveAll(any()) } returns expectedTasks

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isOk)
      result.fold(
        { response ->
          assertEquals(3, response.tasks.size)
          assertEquals(taskId1.uuid, response.tasks[0].id)
          assertEquals("Task 1", response.tasks[0].label)
          assertEquals("Description 1", response.tasks[0].description)
          assertEquals(false, response.tasks[0].completed)

          assertEquals(taskId2.uuid, response.tasks[1].id)
          assertEquals("Task 2", response.tasks[1].label)
          assertEquals("Description 2", response.tasks[1].description)
          assertEquals(true, response.tasks[1].completed)

          assertEquals(taskId3.uuid, response.tasks[2].id)
          assertEquals("Task 3", response.tasks[2].label)
          assertEquals("Description 3", response.tasks[2].description)
          assertEquals(false, response.tasks[2].completed)
        },
        { _ -> fail("Expected Ok, but got Err") },
      )

        verify(exactly = 1) { taskRepository.saveAll(any()) }
    }

    @Test
    @DisplayName("should create single task when command contains one task")
    fun `should create single task successfully`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "Single Task",
                  description = "Single Description",
                  completed = false,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

      val taskId = TaskId.fromUUID(UUID.randomUUID())
      val expectedTask =
        Task
          .create(
            taskId = taskId,
            label = "Single Task",
            description = "Single Description",
            completed = false,
          ).getOrElse { error("Should not fail") }

      every { taskRepository.saveAll(any()) } returns listOf(expectedTask)

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isOk)
      result.fold(
        { response ->
          assertEquals(1, response.tasks.size)
          assertEquals(taskId.uuid, response.tasks[0].id)
          assertEquals("Single Task", response.tasks[0].label)
          assertEquals("Single Description", response.tasks[0].description)
          assertEquals(false, response.tasks[0].completed)
        },
        { _ -> fail("Expected Ok, but got Err") },
      )

        verify(exactly = 1) { taskRepository.saveAll(any()) }
    }

    @Test
    @DisplayName("should trim label and description before creating tasks")
    fun `should trim whitespace from label and description`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "  Task 1  ",
                  description = "  Description 1  ",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "  Task 2  ",
                  description = "  Description 2  ",
                  completed = true,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

      val taskId1 = TaskId.fromUUID(UUID.randomUUID())
      val taskId2 = TaskId.fromUUID(UUID.randomUUID())

      val expectedTasks =
          listOf(
              Task
                  .create(
                      taskId = taskId1,
                      label = "Task 1",
                      description = "Description 1",
                      completed = false,
                  ).getOrElse { error("Should not fail") },
              Task
                  .create(
                      taskId = taskId2,
                      label = "Task 2",
                      description = "Description 2",
                      completed = true,
                  ).getOrElse { error("Should not fail") },
          )

      every { taskRepository.saveAll(any()) } returns expectedTasks

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isOk)
      result.fold(
        { response ->
          assertEquals(2, response.tasks.size)
          assertEquals("Task 1", response.tasks[0].label)
          assertEquals("Description 1", response.tasks[0].description)
          assertEquals("Task 2", response.tasks[1].label)
          assertEquals("Description 2", response.tasks[1].description)
        },
        { _ -> fail("Expected Ok, but got Err") },
      )

        verify(exactly = 1) { taskRepository.saveAll(any()) }
    }

    @Test
    @DisplayName("should handle empty list of tasks")
    fun `should handle empty task list`() {
      // Given
      val command = CreateMultipleTasksCommand(tasks = emptyList())

      every { taskRepository.saveAll(any()) } returns emptyList()

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isOk)
      result.fold(
        { response ->
          assertEquals(0, response.tasks.size)
        },
        { _ -> fail("Expected Ok, but got Err") },
      )

        verify(exactly = 1) { taskRepository.saveAll(any()) }
    }
  }

  @Nested
  @DisplayName("when one task has invalid label")
  inner class InvalidLabel {
    @Test
    @DisplayName("should return Err when first task has blank label")
    fun `should return error when first task label is blank`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "   ",
                  description = "Description 1",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "Task 2",
                  description = "Description 2",
                  completed = false,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

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

        verify(exactly = 0) { taskRepository.saveAll(any()) }
    }

    @Test
    @DisplayName("should return Err when second task has empty label")
    fun `should return error when second task label is empty`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "Task 1",
                  description = "Description 1",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "",
                  description = "Description 2",
                  completed = false,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

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

        verify(exactly = 0) { taskRepository.saveAll(any()) }
    }

    @Test
    @DisplayName("should return Err when last task has blank label")
    fun `should return error when last task label is blank`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "Task 1",
                  description = "Description 1",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "Task 2",
                  description = "Description 2",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "   ",
                  description = "Description 3",
                  completed = false,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

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

        verify(exactly = 0) { taskRepository.saveAll(any()) }
    }
  }

  @Nested
  @DisplayName("when one task has invalid description")
  inner class InvalidDescription {
    @Test
    @DisplayName("should return Err when first task has blank description")
    fun `should return error when first task description is blank`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "Task 1",
                  description = "   ",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "Task 2",
                  description = "Description 2",
                  completed = false,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

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

        verify(exactly = 0) { taskRepository.saveAll(any()) }
    }

    @Test
    @DisplayName("should return Err when middle task has empty description")
    fun `should return error when middle task description is empty`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "Task 1",
                  description = "Description 1",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "Task 2",
                  description = "",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "Task 3",
                  description = "Description 3",
                  completed = false,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

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

        verify(exactly = 0) { taskRepository.saveAll(any()) }
    }
  }

  @Nested
  @DisplayName("when multiple tasks have validation errors")
  inner class MultipleErrors {
    @Test
    @DisplayName("should return Err for first error encountered")
    fun `should fail fast and return first validation error`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "Task 1",
                  description = "Description 1",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "",
                  description = "Description 2",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "Task 3",
                  description = "",
                  completed = false,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isErr)
      result.fold(
        { _ -> fail("Expected Err, but got Ok") },
        { error ->
          assertTrue(error is UseCaseError.DomainValidation)
          val domainError = error as UseCaseError.DomainValidation
          // Should fail on the second task's invalid label
          assertEquals("INVALID_TASK_LABEL", domainError.code)
        },
      )

        verify(exactly = 0) { taskRepository.saveAll(any()) }
    }

    @Test
    @DisplayName("should not save any tasks when one is invalid")
    fun `should not save any tasks when validation fails`() {
      // Given
      val taskCommands =
          listOf(
              CreateTaskCommand(
                  label = "Valid Task 1",
                  description = "Valid Description 1",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "Valid Task 2",
                  description = "Valid Description 2",
                  completed = false,
              ),
              CreateTaskCommand(
                  label = "",
                  description = "Description 3",
                  completed = false,
              ),
          )

      val command = CreateMultipleTasksCommand(tasks = taskCommands)

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isErr)

      // Verify that saveAll was never called, even though first two tasks were valid
        verify(exactly = 0) { taskRepository.saveAll(any()) }
    }
  }
}
