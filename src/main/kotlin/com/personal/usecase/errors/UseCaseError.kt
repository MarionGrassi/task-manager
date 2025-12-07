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

    data object TaskUpdateFailed : UseCaseError() {
        override val code = "TASK_UPDATE_FAILED"
        override val message = "The task could not be updated."
    }

    data object PageNumberNegative : UseCaseError() {
        override val code = "PAGE_NUMBER_NEGATIVE"
        override val message = "The page number must not be negative."
    }

    data object PageSizeNegative : UseCaseError() {
        override val code = "PAGE_SIZE_NEGATIVE"
        override val message = "The page size must not be negative."
    }

    data object PageSizeTooLarge : UseCaseError() {
        override val code = "PAGE_SIZE_TOO_LARGE"
        override val message = "The page size must not exceed 100."
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
