package com.personal.usecase.ports.outbound

import com.personal.domain.model.Task

fun interface TaskRepositoryPort {
    fun save(task: Task): Task
}
