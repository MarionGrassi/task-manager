package com.personal.usecase.model

data class TaskCatalogueResponse(
  val items: List<TaskResponse>,
  val pagination: PaginationInfo,
)

data class PaginationInfo(
  val page: Int,
  val size: Int,
  val totalCount: Long,
)
