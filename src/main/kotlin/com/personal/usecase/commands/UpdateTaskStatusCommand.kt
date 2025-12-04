package com.personal.usecase.commands

import java.util.UUID

data class UpdateTaskStatusCommand(
  val taskId: UUID,
  val completed: Boolean,
)
