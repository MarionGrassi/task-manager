package com.personal.usecase.service

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.toResultOr
import com.personal.domain.model.TaskId
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.model.TaskResponse
import com.personal.usecase.model.toResponse
import com.personal.usecase.ports.inbound.GetTaskByIdUseCase
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import java.util.UUID

class GetTaskByIdApplicationService(
  private val taskRepository: TaskRepositoryPort,
) : GetTaskByIdUseCase {
  override fun execute(taskId: UUID): Result<TaskResponse, UseCaseError> =
    taskRepository
      .findById(TaskId.fromUUID(taskId))
      .toResultOr { UseCaseError.TaskNotFound }
      .map { it.toResponse() }
}
