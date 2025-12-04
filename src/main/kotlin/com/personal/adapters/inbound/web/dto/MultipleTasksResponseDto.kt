package com.personal.adapters.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.personal.usecase.model.MultipleTasksResponse
import kotlin.collections.map

@Schema(
  name = "MultipleTasksResponse",
  description = "Response containing multiple created tasks",
)
data class MultipleTasksResponseDto(
  @field:Schema(description = "List of created tasks")
  val tasks: List<TaskResponseDto>,
) {
  companion object {
    fun fromMultipleTasksResponse(response: MultipleTasksResponse): MultipleTasksResponseDto =
      MultipleTasksResponseDto(
        tasks = response.tasks.map { TaskResponseDto.fromTaskResponse(it) },
      )
  }
}
