package com.personal.usecase.ports.inbound

import com.github.michaelbull.result.Result
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.model.TaskCatalogueResponse

fun interface GetTasksUseCase {
  fun execute(
    page: Int,
    size: Int,
  ): Result<TaskCatalogueResponse, UseCaseError>
}
