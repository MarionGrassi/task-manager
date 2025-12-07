package com.personal.usecase.ports.outbound

import com.personal.domain.model.Task
import com.personal.domain.model.TaskId
import com.personal.domain.model.TasksPage

interface TaskRepositoryPort {
    fun save(task: Task): Task

    fun saveAll(tasks: List<Task>): List<Task>

    fun findById(taskId: TaskId): Task?

    fun findAll(
        page: Int,
        size: Int,
    ): TasksPage

    fun updateTask(task: Task): Task?
}
