package com.personal.config

import com.personal.usecase.ports.inbound.CreateMultipleTasksUseCase
import com.personal.usecase.ports.inbound.CreateTaskUseCase
import com.personal.usecase.ports.inbound.GetTaskByIdUseCase
import com.personal.usecase.ports.inbound.GetTasksUseCase
import com.personal.usecase.ports.inbound.UpdateTaskStatusUseCase
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import com.personal.usecase.service.CreateMultipleTasksApplicationService
import com.personal.usecase.service.CreateTaskApplicationService
import com.personal.usecase.service.GetTaskByIdApplicationService
import com.personal.usecase.service.GetTasksApplicationService
import com.personal.usecase.service.UpdateTaskStatusApplicationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {
    @Bean
    fun createTaskUseCase(repository: TaskRepositoryPort): CreateTaskUseCase = CreateTaskApplicationService(repository)

    @Bean
    fun createMultipleTasksUseCase(repository: TaskRepositoryPort): CreateMultipleTasksUseCase =
        CreateMultipleTasksApplicationService(repository)

    @Bean
    fun getTaskByIdUseCase(repository: TaskRepositoryPort): GetTaskByIdUseCase =
        GetTaskByIdApplicationService(repository)

    @Bean
    fun getTasksUseCase(repository: TaskRepositoryPort): GetTasksUseCase = GetTasksApplicationService(repository)

    @Bean
    fun updateTaskStatusUseCase(repository: TaskRepositoryPort): UpdateTaskStatusUseCase =
        UpdateTaskStatusApplicationService(repository)
}
