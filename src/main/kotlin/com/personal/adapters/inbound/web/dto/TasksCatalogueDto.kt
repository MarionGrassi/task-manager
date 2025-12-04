package com.personal.adapters.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.personal.usecase.model.TaskCatalogueResponse
import kotlin.collections.map

@Schema(
  name = "TasksPageResponse",
  description = "Paginated list of tasks",
)
data class TasksCatalogueDto(
  @field:Schema(description = "List of tasks")
  val tasks: List<TaskResponseDto>,
  @field:Schema(description = "Pagination information")
  val pagination: PaginationInfoDto,
) {
  companion object {
    fun fromResponse(response: TaskCatalogueResponse): TasksCatalogueDto =
      TasksCatalogueDto(
        tasks = response.items.map { TaskResponseDto.fromTaskResponse(it) },
        pagination = PaginationInfoDto.fromModel(response.pagination),
      )
  }
}
