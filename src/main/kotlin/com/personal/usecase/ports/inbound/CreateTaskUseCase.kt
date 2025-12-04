package com.personal.usecase.ports.inbound

import com.personal.usecase.commands.CreateTaskCommand
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.model.TaskResponse
import com.github.michaelbull.result.Result

fun interface CreateTaskUseCase {
    fun execute(command: CreateTaskCommand): Result<TaskResponse, UseCaseError>
}
