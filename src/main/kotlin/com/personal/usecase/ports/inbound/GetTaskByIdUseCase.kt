package com.personal.usecase.ports.inbound

import com.github.michaelbull.result.Result
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.model.TaskResponse
import java.util.UUID

fun interface GetTaskByIdUseCase {
  fun execute(taskId: UUID): Result<TaskResponse, UseCaseError>
}
