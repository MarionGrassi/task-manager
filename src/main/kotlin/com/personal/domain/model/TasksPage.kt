package com.personal.domain.model

data class TasksPage(
  val tasks: List<Task>,
  val pagination: Pagination,
) {
  companion object {
    fun rehydrate(
      tasks: List<Task>,
      page: Int,
      size: Int,
      totalCount: Long,
    ): TasksPage =
      TasksPage(
        tasks = tasks,
        pagination = Pagination.rehydrate(page, size, totalCount),
      )
  }
}

data class Pagination(
  val page: Int,
  val size: Int,
  val totalCount: Long,
) {
  companion object {
    fun rehydrate(
      page: Int,
      size: Int,
      totalCount: Long,
    ): Pagination =
      Pagination(
        page = page,
        size = size,
        totalCount = totalCount,
      )
  }
}
