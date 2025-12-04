package com.personal.usecase.ports.outbound

import com.personal.domain.model.Task

interface TaskRepositoryPort {
    fun save(task: Task): Task

    fun saveAll(tasks: List<Task>): List<Task>
}
