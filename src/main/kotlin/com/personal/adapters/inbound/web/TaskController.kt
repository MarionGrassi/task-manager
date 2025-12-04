package com.personal.adapters.inbound.web

import com.github.michaelbull.result.fold
import com.personal.adapters.inbound.web.dto.CreateMultipleTasksRequestDto
import com.personal.adapters.inbound.web.dto.CreateTaskRequestDto
import com.personal.adapters.inbound.web.dto.MultipleTasksResponseDto
import com.personal.adapters.inbound.web.dto.TaskResponseDto
import com.personal.adapters.inbound.web.dto.TasksCatalogueDto
import com.personal.adapters.inbound.web.errormanager.ApplicationException
import com.personal.usecase.ports.inbound.CreateMultipleTasksUseCase
import com.personal.usecase.ports.inbound.CreateTaskUseCase
import com.personal.usecase.ports.inbound.GetTaskByIdUseCase
import com.personal.usecase.ports.inbound.GetTasksUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Task", description = "Task Management API")
@RestController
@RequestMapping("/tasks")
class TaskController(
    private val createTaskUseCase: CreateTaskUseCase,
    private val createMultipleTasksUseCase: CreateMultipleTasksUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val getTasksUseCase: GetTasksUseCase,
) {
    @Operation(
        summary = "Create a new task",
        description = "Creates a new task with the provided label, description, and completion status.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Task created successfully",
                content = [Content(schema = Schema(implementation = TaskResponseDto::class))],
            ),
            ApiResponse(responseCode = "400", description = "Invalid request body"),
            ApiResponse(responseCode = "401", description = "You are not authenticated"),
        ],
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createTask(
        @RequestBody request: CreateTaskRequestDto,
    ): TaskResponseDto =
        createTaskUseCase
            .execute(request.toCommand())
            .fold(
                { taskResponse ->
                    TaskResponseDto.fromTaskResponse(taskResponse)
                },
                { error ->
                    throw ApplicationException(error)
                },
            )

    @Operation(
        summary = "Create multiple tasks",
        description = "Creates multiple tasks at once with the provided list of task details.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Tasks created successfully",
                content = [Content(schema = Schema(implementation = MultipleTasksResponseDto::class))],
            ),
            ApiResponse(responseCode = "400", description = "Invalid request body"),
            ApiResponse(responseCode = "401", description = "You are not authenticated"),
        ],
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/bulk")
    fun createMultipleTasks(
        @RequestBody request: CreateMultipleTasksRequestDto,
    ): MultipleTasksResponseDto =
        createMultipleTasksUseCase
            .execute(request.toCommand())
            .fold(
                { multipleTasksResponse ->
                    MultipleTasksResponseDto.fromMultipleTasksResponse(multipleTasksResponse)
                },
                { error ->
                    throw ApplicationException(error)
                },
            )

    @Operation(
        summary = "Get a task by ID",
        description = "Retrieves a single task by its unique identifier.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Task retrieved successfully",
                content = [Content(schema = Schema(implementation = TaskResponseDto::class))],
            ),
            ApiResponse(responseCode = "404", description = "Task not found"),
            ApiResponse(responseCode = "401", description = "You are not authenticated"),
        ],
    )
    @GetMapping("/{id}")
    fun getTaskById(
        @PathVariable id: UUID,
    ): TaskResponseDto =
        getTaskByIdUseCase
            .execute(id)
            .fold(
                { taskResponse ->
                    TaskResponseDto.fromTaskResponse(taskResponse)
                },
                { error ->
                    throw ApplicationException(error)
                },
            )

    @Operation(
        summary = "Get all tasks",
        description = "Retrieves a paginated list of tasks.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Tasks retrieved successfully",
                content = [Content(schema = Schema(implementation = TasksCatalogueDto::class))],
            ),
            ApiResponse(responseCode = "401", description = "You are not authenticated"),
        ],
    )
    @GetMapping
    fun getTasks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): TasksCatalogueDto =
        getTasksUseCase
            .execute(page, size)
            .fold(
                { tasksPageResponse ->
                    TasksCatalogueDto.fromResponse(tasksPageResponse)
                },
                { error ->
                    throw ApplicationException(error)
                },
            )
}
