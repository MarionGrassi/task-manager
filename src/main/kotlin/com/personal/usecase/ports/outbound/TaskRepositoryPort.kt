package com.personal.usecase.ports.outbound

import com.personal.domain.model.Task
import com.personal.domain.model.TaskId

interface TaskRepositoryPort {
    fun save(task: Task): Task

    fun saveAll(tasks: List<Task>): List<Task>

    fun findById(taskId: TaskId): Task?
}
