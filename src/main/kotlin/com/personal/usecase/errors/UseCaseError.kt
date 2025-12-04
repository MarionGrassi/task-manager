package com.personal.usecase.errors

import com.personal.domain.errors.DomainError

sealed class UseCaseError {
    abstract val code: String
    abstract val message: String

    data class DomainValidation(
        override val code: String,
        override val message: String,
    ) : UseCaseError()

    data object TaskNotFound : UseCaseError() {
        override val code = "TASK_NOT_FOUND"
        override val message = "The task was not found."
    }
}

fun DomainError.toUseCaseError(): UseCaseError =
    when (this) {
        DomainError.InvalidTaskLabel,
        DomainError.InvalidTaskDescription,
            ->
            UseCaseError.DomainValidation(
                code = this.code,
                message = this.message,
            )
    }
