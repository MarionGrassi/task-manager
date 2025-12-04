package com.personal.usecase.service

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.personal.usecase.errors.UseCaseError
import com.personal.usecase.model.PaginationInfo
import com.personal.usecase.model.TaskCatalogueResponse
import com.personal.usecase.model.toResponse
import com.personal.usecase.ports.inbound.GetTasksUseCase
import com.personal.usecase.ports.outbound.TaskRepositoryPort
import kotlin.collections.map

class GetTasksApplicationService(
    private val taskRepository: TaskRepositoryPort,
) : GetTasksUseCase {
    override fun execute(
        page: Int,
        size: Int,
    ): Result<TaskCatalogueResponse, UseCaseError> =
        verifyPaging(page, size)
            .andThen {
                val tasksPage = taskRepository.findAll(page, size)
                Ok(
                    TaskCatalogueResponse(
                        items = tasksPage.tasks.map { it.toResponse() },
                        pagination =
                            PaginationInfo(
                                page = tasksPage.pagination.page,
                                size = tasksPage.pagination.size,
                                totalCount = tasksPage.pagination.totalCount,
                            ),
                    ),
                )
            }

    private fun verifyPaging(page: Int, size: Int): Result<Unit, UseCaseError> =
        when {
            page < 0 -> Err(UseCaseError.PageNumberNegative)
            size > 100 -> Err(UseCaseError.PageSizeTooLarge)
            size < 0 -> Err(UseCaseError.PageSizeNegative)
            else -> Ok(Unit)
        }
}
