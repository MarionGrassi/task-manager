package com.personal.adapters.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.personal.usecase.commands.UpdateTaskStatusCommand
import java.util.UUID

@Schema(
  name = "UpdateTaskStatusRequest",
  description = "Request to update a task's completion status",
)
data class UpdateTaskStatusRequestDto(
  @field:Schema(description = "Whether the task is completed", required = true, example = "true")
  val completed: Boolean,
) {
  fun toCommand(taskId: UUID): UpdateTaskStatusCommand =
    UpdateTaskStatusCommand(
      taskId = taskId,
      completed = completed,
    )
}
