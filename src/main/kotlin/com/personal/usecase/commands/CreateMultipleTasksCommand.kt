package com.personal.usecase.commands

data class CreateMultipleTasksCommand(
  val tasks: List<CreateTaskCommand>,
)
