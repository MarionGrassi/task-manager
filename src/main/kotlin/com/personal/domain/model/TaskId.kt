package com.personal.domain.model

import java.util.UUID

@ConsistentCopyVisibility
data class TaskId private constructor(
    val uuid: UUID,
) {
    companion object {
        fun generate(): TaskId = TaskId(UUID.randomUUID())

        fun fromString(uuidString: String): TaskId {
            check(uuidString.isNotBlank()) { "UUID string must not be blank" }
            return try {
                TaskId(UUID.fromString(uuidString))
            } catch (_: IllegalArgumentException) {
                error("Invalid UUID format: $uuidString")
            }
        }

        fun fromUUID(uuid: UUID): TaskId = TaskId(uuid)
    }
}
