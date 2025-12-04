package it.system.task

import com.fasterxml.jackson.module.kotlin.readValue
import com.personal.adapters.inbound.web.dto.TaskResponseDto
import it.IntegrationTestBase
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

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

    @Nested
    @DisplayName("Create Multiple Tasks")
    inner class CreateMultipleTasks {
        @Test
        @DisplayName("Successfully creates multiple tasks")
        fun `creates multiple tasks successfully`() {
            val requestBody =
                """
        {
          "tasks": [
            {
              "label": "Task 1",
              "description": "First task",
              "completed": false
            },
            {
              "label": "Task 2",
              "description": "Second task",
              "completed": true
            },
            {
              "label": "Task 3",
              "description": "Third task",
              "completed": false
            }
          ]
        }
        """.trimIndent()

            mockMvc
                .perform(
                    post("$BASE_TASK_URL/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tasks", hasSize<Int>(3)))
                .andExpect(jsonPath("$.tasks[0].label").value("Task 1"))
                .andExpect(jsonPath("$.tasks[1].label").value("Task 2"))
                .andExpect(jsonPath("$.tasks[2].label").value("Task 3"))
        }
    }

    @Nested
    @DisplayName("Get Task By ID")
    inner class GetTaskById {
        @Test
        @DisplayName("Successfully retrieves a task by ID")
        fun `retrieves task by id successfully`() {
            val taskId = createTask("Retrieve me", "Task to retrieve", false)

            mockMvc
                .perform(
                    get("$BASE_TASK_URL/{id}", taskId)
                        .accept(MediaType.APPLICATION_JSON),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.label").value("Retrieve me"))
                .andExpect(jsonPath("$.description").value("Task to retrieve"))
                .andExpect(jsonPath("$.completed").value(false))
        }

        @Test
        @DisplayName("Returns 404 when task not found")
        fun `returns 404 for non-existent task`() {
            val nonExistentId = UUID.randomUUID()

            mockMvc
                .perform(
                    get("$BASE_TASK_URL/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON),
                ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound)
        }
    }

    // Helper method to create a task and return its ID
    private fun createTask(
        label: String,
        description: String,
        completed: Boolean,
    ): UUID {
        val requestBody =
            """
      {
        "label": "$label",
        "description": "$description",
        "completed": $completed
      }
      """.trimIndent()

        val result =
            mockMvc
                .perform(
                    post(BASE_TASK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(status().isCreated)
                .andReturn()

        val response = objectMapper.readValue<TaskResponseDto>(result.response.contentAsString)
        return response.id
    }
}
