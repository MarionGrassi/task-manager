package it.system.task

import it.IntegrationTestBase
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@DisplayName("Task API Integration Tests")
class TaskIT : IntegrationTestBase() {
    companion object {
        const val BASE_TASK_URL = "/tasks"
    }

    @Nested
    @DisplayName("Create Task")
    inner class CreateTask {
        @Test
        @DisplayName("Successfully creates a task with valid data")
        fun `creates task successfully`() {
            val requestBody =
                """
        {
          "label": "Complete project",
          "description": "Finish the API implementation",
          "completed": false
        }
        """.trimIndent()

            mockMvc
                .perform(
                    post(BASE_TASK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.label").value("Complete project"))
                .andExpect(jsonPath("$.description").value("Finish the API implementation"))
                .andExpect(jsonPath("$.completed").value(false))
        }

        @Test
        @DisplayName("Creates a task with completed status true")
        fun `creates completed task`() {
            val requestBody =
                """
        {
          "label": "Already done task",
          "description": "This task was already completed",
          "completed": true
        }
        """.trimIndent()

            mockMvc
                .perform(
                    post(BASE_TASK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.completed").value(true))
        }
    }
}
