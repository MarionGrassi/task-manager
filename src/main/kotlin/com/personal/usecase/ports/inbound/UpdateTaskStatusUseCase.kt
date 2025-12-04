package com.personal.usecase.ports.inbound

import com.github.michaelbull.result.Result
import com.personal.usecase.commands.UpdateTaskStatusCommand
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.model.TaskResponse

fun interface UpdateTaskStatusUseCase {
  fun execute(command: UpdateTaskStatusCommand): Result<TaskResponse, UseCaseError>
}
