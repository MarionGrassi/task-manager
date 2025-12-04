package com.personal.usecase.commands

data class CreateTaskCommand(
    val label: String,
    val description: String,
    val completed: Boolean = false,
)
