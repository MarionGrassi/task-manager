package com.personal.usecase.model

import com.personal.domain.model.Task

fun Task.toResponse() =
    TaskResponse(
        id = taskId.uuid,
        label = label,
        description = description,
        completed = completed,
    )
