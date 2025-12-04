package com.personal.usecase.ports.inbound

import com.github.michaelbull.result.Result
import com.personal.usecase.commands.CreateMultipleTasksCommand
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.model.MultipleTasksResponse

fun interface CreateMultipleTasksUseCase {
  fun execute(command: CreateMultipleTasksCommand): Result<MultipleTasksResponse, UseCaseError>
}
