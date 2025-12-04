package com.personal.domain.errors

sealed class DomainError {
    abstract val code: String
    abstract val message: String

    data object InvalidTaskLabel : DomainError() {
        override val code = "INVALID_TASK_LABEL"
        override val message = "The task label cannot be blank."
    }

    data object InvalidTaskDescription : DomainError() {
        override val code = "INVALID_TASK_DESCRIPTION"
        override val message = "The task description cannot be blank."
    }
}
