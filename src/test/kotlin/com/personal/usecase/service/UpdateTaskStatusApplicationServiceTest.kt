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
import com.personal.usecase.commands.UpdateTaskStatusCommand
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import java.util.UUID

@DisplayName("UpdateTaskStatusApplicationService.execute(...)")
class UpdateTaskStatusApplicationServiceTest {
  private lateinit var taskRepository: TaskRepositoryPort
  private lateinit var service: UpdateTaskStatusApplicationService

  @BeforeEach
  fun setUp() {
    taskRepository = mockk()
    service = UpdateTaskStatusApplicationService(taskRepository)
  }

  @Nested
  @DisplayName("when updating task status to completed")
  inner class UpdatingToCompleted {
    @Test
    @DisplayName("should mark incomplete task as completed")
    fun `should update incomplete task to completed`() {
      // Given
      val taskUuid = UUID.randomUUID()
      val taskId = TaskId.fromUUID(taskUuid)
      val incompleteTask =
        Task
          .create(
            taskId = taskId,
            label = "Test Task",
            description = "Test Description",
            completed = false,
          ).getOrElse { error("Should not fail") }

      val completedTask = incompleteTask.updateStatus(true)

      val command =
        UpdateTaskStatusCommand(
          taskId = taskUuid,
          completed = true,
        )

      every { taskRepository.findById(taskId) } returns incompleteTask
      every { taskRepository.updateTask(any()) } returns completedTask

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isOk)
      result.fold(
        { response ->
          assertEquals(taskUuid, response.id)
          assertEquals("Test Task", response.label)
          assertEquals("Test Description", response.description)
          assertEquals(true, response.completed)
        },
        { _ -> fail("Expected Ok, but got Err") },
      )

        verify(exactly = 1) { taskRepository.findById(taskId) }
        verify(exactly = 1) { taskRepository.updateTask(any()) }
    }

    @Test
    @DisplayName("should handle already completed task")
    fun `should update already completed task to completed`() {
      // Given
      val taskUuid = UUID.randomUUID()
      val taskId = TaskId.fromUUID(taskUuid)
      val alreadyCompletedTask =
        Task
          .create(
            taskId = taskId,
            label = "Already Done",
            description = "This is already completed",
            completed = true,
          ).getOrElse { error("Should not fail") }

      val updatedTask = alreadyCompletedTask.updateStatus(true)

      val command =
        UpdateTaskStatusCommand(
          taskId = taskUuid,
          completed = true,
        )

      every { taskRepository.findById(taskId) } returns alreadyCompletedTask
      every { taskRepository.updateTask(any()) } returns updatedTask

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isOk)
      result.fold(
        { response ->
          assertEquals(taskUuid, response.id)
          assertEquals("Already Done", response.label)
          assertEquals(true, response.completed)
        },
        { _ -> fail("Expected Ok, but got Err") },
      )

        verify(exactly = 1) { taskRepository.findById(taskId) }
        verify(exactly = 1) { taskRepository.updateTask(any()) }
    }
  }

  @Nested
  @DisplayName("when updating task status to incomplete")
  inner class UpdatingToIncomplete {
    @Test
    @DisplayName("should mark completed task as incomplete")
    fun `should update completed task to incomplete`() {
      // Given
      val taskUuid = UUID.randomUUID()
      val taskId = TaskId.fromUUID(taskUuid)
      val completedTask =
        Task
          .create(
            taskId = taskId,
            label = "Completed Task",
            description = "This was done",
            completed = true,
          ).getOrElse { error("Should not fail") }

      val incompleteTask = completedTask.updateStatus(false)

      val command =
        UpdateTaskStatusCommand(
          taskId = taskUuid,
          completed = false,
        )

      every { taskRepository.findById(taskId) } returns completedTask
      every { taskRepository.updateTask(any()) } returns incompleteTask

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isOk)
      result.fold(
        { response ->
          assertEquals(taskUuid, response.id)
          assertEquals("Completed Task", response.label)
          assertEquals("This was done", response.description)
          assertEquals(false, response.completed)
        },
        { _ -> fail("Expected Ok, but got Err") },
      )

        verify(exactly = 1) { taskRepository.findById(taskId) }
        verify(exactly = 1) { taskRepository.updateTask(any()) }
    }

    @Test
    @DisplayName("should handle already incomplete task")
    fun `should update already incomplete task to incomplete`() {
      // Given
      val taskUuid = UUID.randomUUID()
      val taskId = TaskId.fromUUID(taskUuid)
      val incompleteTask =
        Task
          .create(
            taskId = taskId,
            label = "Not Done",
            description = "Still working on it",
            completed = false,
          ).getOrElse { error("Should not fail") }

      val updatedTask = incompleteTask.updateStatus(false)

      val command =
        UpdateTaskStatusCommand(
          taskId = taskUuid,
          completed = false,
        )

      every { taskRepository.findById(taskId) } returns incompleteTask
      every { taskRepository.updateTask(any()) } returns updatedTask

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isOk)
      result.fold(
        { response ->
          assertEquals(taskUuid, response.id)
          assertEquals("Not Done", response.label)
          assertEquals(false, response.completed)
        },
        { _ -> fail("Expected Ok, but got Err") },
      )

        verify(exactly = 1) { taskRepository.findById(taskId) }
        verify(exactly = 1) { taskRepository.updateTask(any()) }
    }
  }

  @Nested
  @DisplayName("when task does not exist")
  inner class TaskNotFound {
    @Test
    @DisplayName("should return Err(UseCaseError.TaskNotFound) when updating to completed")
    fun `should return error when task not found for completion`() {
      // Given
      val nonExistentTaskUuid = UUID.randomUUID()
      val taskId = TaskId.fromUUID(nonExistentTaskUuid)

      val command =
        UpdateTaskStatusCommand(
          taskId = nonExistentTaskUuid,
          completed = true,
        )

      every { taskRepository.findById(taskId) } returns null

      // When
      val result = service.execute(command)

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
        verify(exactly = 0) { taskRepository.updateTask(any()) }
    }
  }

  @Nested
  @DisplayName("when task update fails")
  inner class TaskUpdateFailed {
    @Test
    @DisplayName("should return Err(UseCaseError.TaskUpdateFailed) when updateTask returns null")
    fun `should return error when update fails`() {
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

      val command =
        UpdateTaskStatusCommand(
          taskId = taskUuid,
          completed = true,
        )

      every { taskRepository.findById(taskId) } returns existingTask
      every { taskRepository.updateTask(any()) } returns null

      // When
      val result = service.execute(command)

      // Then
      assertTrue(result.isErr)
      result.fold(
        { _ -> fail("Expected Err, but got Ok") },
        { error ->
          assertEquals(UseCaseError.TaskUpdateFailed, error)
        },
      )

        verify(exactly = 1) { taskRepository.findById(taskId) }
        verify(exactly = 1) { taskRepository.updateTask(any()) }
    }
  }
}
