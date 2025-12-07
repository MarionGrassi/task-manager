package com.personal.usecase.service

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.toResultOr
import com.personal.domain.model.TaskId
import com.personal.usecase.commands.UpdateTaskStatusCommand
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.model.TaskResponse
import com.personal.usecase.model.toResponse
import com.personal.usecase.ports.inbound.UpdateTaskStatusUseCase
import com.personal.usecase.ports.outbound.TaskRepositoryPort

class UpdateTaskStatusApplicationService(
    private val taskRepository: TaskRepositoryPort,
) : UpdateTaskStatusUseCase {
    override fun execute(command: UpdateTaskStatusCommand): Result<TaskResponse, UseCaseError> =
        taskRepository
            .findById(TaskId.fromUUID(command.taskId))
            .toResultOr { UseCaseError.TaskNotFound }
            .andThen { task ->
                val updatedTask = task.updateStatus(command.completed)
                val savedTask = taskRepository.updateTask(updatedTask)
                savedTask?.let { Ok(it) } ?: Err(UseCaseError.TaskUpdateFailed)
            }
            .map { it.toResponse() }
}
