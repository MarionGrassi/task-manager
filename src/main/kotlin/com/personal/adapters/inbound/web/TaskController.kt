package com.personal.adapters.inbound.web

import com.github.michaelbull.result.fold
import com.personal.adapters.inbound.web.dto.CreateTaskRequestDto
import com.personal.adapters.inbound.web.dto.TaskResponseDto
import com.personal.adapters.inbound.web.errormanager.ApplicationException
import com.personal.usecase.ports.inbound.CreateTaskUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Task", description = "Task Management API")
@RestController
@RequestMapping("/tasks")
class TaskController(
    private val createTaskUseCase: CreateTaskUseCase,
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

}
