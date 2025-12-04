package com.personal.adapters.outbound.persistence.entities

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import com.personal.domain.model.Task
import com.personal.domain.model.TaskId

@Document("tasks")
class TaskEntity(
    @Indexed(unique = true)
    val taskId: String,
    val label: String,
    val description: String,
    val completed: Boolean,
) {
    fun toDomain(): Task =
        Task.rehydrate(
            TaskId.fromString(taskId),
            label,
            description,
            completed,
        )

    companion object {
        fun fromDomain(t: Task) =
            TaskEntity(
                taskId = t.taskId.uuid.toString(),
                label = t.label,
                description = t.description,
                completed = t.completed,
            )
    }
}
