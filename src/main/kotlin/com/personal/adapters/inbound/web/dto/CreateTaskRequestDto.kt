package com.personal.adapters.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.personal.usecase.commands.CreateTaskCommand

@Schema(
    name = "CreateTaskRequest",
    description = "Request to create a new task",
)
data class CreateTaskRequestDto(
    @field:Schema(description = "The task label", required = true, example = "Complete project")
    val label: String,
    @field:Schema(description = "The task description", required = true, example = "Finish the API implementation")
    val description: String,
    @field:Schema(description = "Whether the task is completed", required = false, defaultValue = "false")
    val completed: Boolean = false,
) {
    fun toCommand(): CreateTaskCommand =
        CreateTaskCommand(
            label = label,
            description = description,
            completed = completed,
        )
}
