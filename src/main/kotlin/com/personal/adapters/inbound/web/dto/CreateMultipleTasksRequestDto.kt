package com.personal.adapters.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.personal.usecase.commands.CreateMultipleTasksCommand
import kotlin.collections.map

@Schema(
  name = "CreateMultipleTasksRequest",
  description = "Request to create multiple tasks at once",
)
data class CreateMultipleTasksRequestDto(
  @field:Schema(description = "List of tasks to create", required = true)
  val tasks: List<CreateTaskRequestDto>,
) {
  fun toCommand(): CreateMultipleTasksCommand =
    CreateMultipleTasksCommand(
      tasks = tasks.map { it.toCommand() },
    )
}
