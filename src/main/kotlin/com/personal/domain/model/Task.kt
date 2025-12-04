package com.personal.domain.model

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.fold
import com.personal.domain.errors.DomainError

class Task private constructor(
    val taskId: TaskId,
    val label: String,
    val description: String,
    val completed: Boolean,
) {
    fun updateStatus(completed: Boolean): Task = Task(taskId, label, description, completed)

    companion object {
        fun create(
            taskId: TaskId = TaskId.generate(),
            label: String,
            description: String,
            completed: Boolean = false,
        ): Result<Task, DomainError> =
            validate(label, description)
                .andThen {
                    Ok(Task(taskId, label, description, completed))
                }

        private fun validate(
            label: String,
            description: String,
        ): Result<Unit, DomainError> =
            when {
                label.isBlank() -> Err(DomainError.InvalidTaskLabel)
                description.isBlank() -> Err(DomainError.InvalidTaskDescription)
                else -> Ok(Unit)
            }

        fun rehydrate(
            taskId: TaskId,
            label: String,
            description: String,
            completed: Boolean,
        ): Task =
            validate(label, description)
                .fold(
                    {
                        Task(taskId, label, description, completed)
                    },
                    { error ->
                        error(error.message)
                    },
                )
    }
}
