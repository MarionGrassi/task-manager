package com.personal.usecase.service

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.personal.domain.model.Task
import com.personal.usecase.commands.CreateMultipleTasksCommand
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.errors.toUseCaseError
import com.personal.usecase.model.MultipleTasksResponse
import com.personal.usecase.model.toResponse
import com.personal.usecase.ports.inbound.CreateMultipleTasksUseCase
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.text.trim

class CreateMultipleTasksApplicationService(
  private val taskRepository: TaskRepositoryPort,
) : CreateMultipleTasksUseCase {
  override fun execute(command: CreateMultipleTasksCommand): Result<MultipleTasksResponse, UseCaseError> =
    validateAndCreateTasks(command)
      .map { tasks -> taskRepository.saveAll(tasks) }
      .map { savedTasks -> savedTasks.map { it.toResponse() } }
      .map { taskResponses -> MultipleTasksResponse(tasks = taskResponses) }

  private fun validateAndCreateTasks(command: CreateMultipleTasksCommand): Result<List<Task>, UseCaseError> {
    val tasks = mutableListOf<Task>()

    command.tasks.forEach { taskCommand ->
      Task
        .create(
          label = taskCommand.label.trim(),
          description = taskCommand.description.trim(),
          completed = taskCommand.completed,
        ).mapError { error -> error.toUseCaseError() }
        .fold(
          { task -> tasks.add(task) },
          { error -> return Err(error) },
        )
    }

    return Ok(tasks)
  }
}
