package com.personal.usecase.service

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.personal.domain.model.Task
import com.personal.usecase.commands.CreateTaskCommand
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.errors.toUseCaseError
import com.personal.usecase.model.TaskResponse
import com.personal.usecase.model.toResponse
import com.personal.usecase.ports.inbound.CreateTaskUseCase
import com.personal.usecase.ports.outbound.TaskRepositoryPort

class CreateTaskApplicationService(
    private val taskRepository: TaskRepositoryPort,
) : CreateTaskUseCase {
    override fun execute(command: CreateTaskCommand): Result<TaskResponse, UseCaseError> =
        Task
            .create(
                label = command.label.trim(),
                description = command.description.trim(),
                completed = command.completed,
            ).mapError { error -> error.toUseCaseError() }
            .map { task ->
                val savedTask = taskRepository.save(task)
                savedTask.toResponse()
            }
}
