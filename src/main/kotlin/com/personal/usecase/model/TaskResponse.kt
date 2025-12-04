package com.personal.usecase.model

import java.util.UUID

data class TaskResponse(
    val id: UUID,
    val label: String,
    val description: String,
    val completed: Boolean,
)
