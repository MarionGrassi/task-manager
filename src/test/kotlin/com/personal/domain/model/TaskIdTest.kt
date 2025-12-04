package com.personal.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.UUID

class TaskIdTest {

    @Test
    fun `generate returns a new TaskId with a valid UUID`() {
        val taskId = TaskId.generate()

        assertThat(taskId.uuid).isNotNull()
        assertThat(taskId.uuid.toString()).hasSize(36) // Valid UUID length
    }

    @Test
    fun `fromUUID returns TaskId with provided UUID`() {
        val uuid = UUID.randomUUID()
        val taskId = TaskId.fromUUID(uuid)

        assertThat(taskId.uuid).isEqualTo(uuid)
    }

    @Test
    fun `fromString returns TaskId when UUID string is valid`() {
        val uuid = UUID.randomUUID()
        val taskId = TaskId.fromString(uuid.toString())

        assertThat(taskId.uuid).isEqualTo(uuid)
    }

    @Test
    fun `fromString throws IllegalStateException when UUID string is blank`() {
        assertThatThrownBy {
            TaskId.fromString("")
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessage("UUID string must not be blank")
    }

    @Test
    fun `fromString throws IllegalStateException when UUID string is invalid`() {
        val invalid = "not-a-uuid-at-all"

        assertThatThrownBy {
            TaskId.fromString(invalid)
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Invalid UUID format: $invalid")
    }
}
