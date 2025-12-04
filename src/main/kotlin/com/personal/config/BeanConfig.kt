package com.personal.config

import com.personal.usecase.ports.inbound.CreateMultipleTasksUseCase
import com.personal.usecase.ports.inbound.CreateTaskUseCase
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import com.personal.usecase.service.CreateMultipleTasksApplicationService
import com.personal.usecase.service.CreateTaskApplicationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {
    @Bean
    fun createTaskUseCase(repository: TaskRepositoryPort): CreateTaskUseCase = CreateTaskApplicationService(repository)

    @Bean
    fun createMultipleTasksUseCase(repository: TaskRepositoryPort): CreateMultipleTasksUseCase =
        CreateMultipleTasksApplicationService(repository)
}
