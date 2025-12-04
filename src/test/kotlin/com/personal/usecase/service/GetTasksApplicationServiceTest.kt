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
import com.personal.domain.model.TasksPage
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import java.util.UUID
import kotlin.collections.forEachIndexed
import kotlin.collections.map

@DisplayName("GetTasksApplicationService.execute(...)")
class GetTasksApplicationServiceTest {
    private lateinit var taskRepository: TaskRepositoryPort
    private lateinit var service: GetTasksApplicationService

    @BeforeEach
    fun setUp() {
        taskRepository = mockk()
        service = GetTasksApplicationService(taskRepository)
    }

    @Test
    @DisplayName("should return paginated tasks successfully")
    fun `should return first page of tasks`() {
        // Given
        val page = 0
        val size = 10

        val task1 =
            Task
                .create(
                    taskId = TaskId.fromUUID(UUID.randomUUID()),
                    label = "Task 1",
                    description = "Description 1",
                    completed = false,
                ).getOrElse { error("Should not fail") }

        val task2 =
            Task
                .create(
                    taskId = TaskId.fromUUID(UUID.randomUUID()),
                    label = "Task 2",
                    description = "Description 2",
                    completed = true,
                ).getOrElse { error("Should not fail") }

        val task3 =
            Task
                .create(
                    taskId = TaskId.fromUUID(UUID.randomUUID()),
                    label = "Task 3",
                    description = "Description 3",
                    completed = false,
                ).getOrElse { error("Should not fail") }

        val tasksPage = TasksPage.rehydrate(listOf(task1, task2, task3), page, size, 3)

        every { taskRepository.findAll(page, size) } returns tasksPage

        // When
        val result = service.execute(page, size)

        // Then
        assertTrue(result.isOk)
        result.fold(
            { response ->
                assertEquals(3, response.items.size)
                assertEquals(task1.taskId.uuid, response.items[0].id)
                assertEquals("Task 1", response.items[0].label)
                assertEquals("Description 1", response.items[0].description)
                assertEquals(false, response.items[0].completed)

                assertEquals(task2.taskId.uuid, response.items[1].id)
                assertEquals("Task 2", response.items[1].label)
                assertEquals(true, response.items[1].completed)

                assertEquals(task3.taskId.uuid, response.items[2].id)
                assertEquals("Task 3", response.items[2].label)

                assertEquals(0, response.pagination.page)
                assertEquals(10, response.pagination.size)
                assertEquals(3L, response.pagination.totalCount)
            },
            { _ -> fail("Expected Ok, but got Err") },
        )

        verify(exactly = 1) { taskRepository.findAll(page, size) }
    }

    @Test
    @DisplayName("should return second page of tasks")
    fun `should return second page with correct pagination info`() {
        // Given
        val page = 1
        val size = 5

        val task1 =
            Task
                .create(
                    taskId = TaskId.fromUUID(UUID.randomUUID()),
                    label = "Task 6",
                    description = "Description 6",
                    completed = false,
                ).getOrElse { error("Should not fail") }

        val task2 =
            Task
                .create(
                    taskId = TaskId.fromUUID(UUID.randomUUID()),
                    label = "Task 7",
                    description = "Description 7",
                    completed = true,
                ).getOrElse { error("Should not fail") }

        val tasksPage = TasksPage.rehydrate(listOf(task1, task2), page, size, 12)

        every { taskRepository.findAll(page, size) } returns tasksPage

        // When
        val result = service.execute(page, size)

        // Then
        assertTrue(result.isOk)
        result.fold(
            { response ->
                assertEquals(2, response.items.size)
                assertEquals(1, response.pagination.page)
                assertEquals(5, response.pagination.size)
                assertEquals(12L, response.pagination.totalCount)
            },
            { _ -> fail("Expected Ok, but got Err") },
        )

        verify(exactly = 1) { taskRepository.findAll(page, size) }
    }

    @Test
    @DisplayName("should return empty list when no tasks exist")
    fun `should return empty list with correct pagination`() {
        // Given
        val page = 0
        val size = 10

        val tasksPage = TasksPage.rehydrate(emptyList(), page, size, 0)

        every { taskRepository.findAll(page, size) } returns tasksPage

        // When
        val result = service.execute(page, size)

        // Then
        assertTrue(result.isOk)
        result.fold(
            { response ->
                assertEquals(0, response.items.size)
                assertEquals(0, response.pagination.page)
                assertEquals(10, response.pagination.size)
                assertEquals(0L, response.pagination.totalCount)
            },
            { _ -> fail("Expected Ok, but got Err") },
        )

        verify(exactly = 1) { taskRepository.findAll(page, size) }
    }

    @Test
    @DisplayName("should return empty list when requesting page beyond available data")
    fun `should return empty list for page beyond data`() {
        // Given
        val page = 10
        val size = 10

        val tasksPage = TasksPage.rehydrate(emptyList(), page, size, 15)

        every { taskRepository.findAll(page, size) } returns tasksPage

        // When
        val result = service.execute(page, size)

        // Then
        assertTrue(result.isOk)
        result.fold(
            { response ->
                assertEquals(0, response.items.size)
                assertEquals(10, response.pagination.page)
                assertEquals(10, response.pagination.size)
                assertEquals(15L, response.pagination.totalCount)
            },
            { _ -> fail("Expected Ok, but got Err") },
        )

        verify(exactly = 1) { taskRepository.findAll(page, size) }
    }

    @Test
    @DisplayName("should return Err(UseCaseError.PageNumberNegative)")
    fun `should return PageNumberNegative when page is negative`() {
        // Given
        val page = -1
        val size = 10

        // When
        val result = service.execute(page, size)

        // Then
        assertTrue(result.isErr)
        result.fold(
            { _ -> fail("Expected Err, but got Ok") },
            { error ->
                assertEquals(UseCaseError.PageNumberNegative, error)
                assertEquals("PAGE_NUMBER_NEGATIVE", error.code)
                assertEquals("The page number must not be negative.", error.message)
            },
        )

        verify(exactly = 0) { taskRepository.findAll(page, size) }
    }

    @Test
    @DisplayName("should return Err(UseCaseError.PageSizeNegative)")
    fun `should return PageSizeNegative when size is negative`() {
        // Given
        val page = 0
        val size = -2

        // When
        val result = service.execute(page, size)

        // Then
        assertTrue(result.isErr)
        result.fold(
            { _ -> fail("Expected Err, but got Ok") },
            { error ->
                assertEquals(UseCaseError.PageSizeNegative, error)
                assertEquals("PAGE_SIZE_NEGATIVE", error.code)
                assertEquals("The page size must not be negative.", error.message)
            },
        )

        verify(exactly = 0) { taskRepository.findAll(page, size) }
    }

    @Test
    @DisplayName("should return Err(UseCaseError.PageSizeTooLarge)")
    fun `should return PageSizeTooLarge when size is too large`() {
        // Given
        val page = 0
        val size = 101

        // When
        val result = service.execute(page, size)

        // Then
        assertTrue(result.isErr)
        result.fold(
            { _ -> fail("Expected Err, but got Ok") },
            { error ->
                assertEquals(UseCaseError.PageSizeTooLarge, error)
                assertEquals("PAGE_SIZE_TOO_LARGE", error.code)
                assertEquals("The page size must not exceed 100.", error.message)
            },
        )

        verify(exactly = 0) { taskRepository.findAll(page, size) }
    }
}


