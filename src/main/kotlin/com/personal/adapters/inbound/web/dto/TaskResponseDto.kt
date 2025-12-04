package com.personal.adapters.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.personal.usecase.model.TaskResponse
import java.util.UUID

@Schema(
    name = "TaskResponse",
    description = "The task response",
)
data class TaskResponseDto(
    @field:Schema(description = "The task unique identifier")
    val id: UUID,
    @field:Schema(description = "The task label")
    val label: String,
    @field:Schema(description = "The task description")
    val description: String,
    @field:Schema(description = "Whether the task is completed")
    val completed: Boolean,
) {
    companion object {
        fun fromTaskResponse(response: TaskResponse): TaskResponseDto =
            TaskResponseDto(
                id = response.id,
                label = response.label,
                description = response.description,
                completed = response.completed,
            )
    }
}
