package com.personal.adapters.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.personal.usecase.model.PaginationInfo

data class PaginationInfoDto(
  @field:Schema(description = "Current page number (0-based)", example = "1")
  val page: Int,
  @field:Schema(description = "Number of items per page", example = "10")
  val size: Int,
  @field:Schema(description = "Total number of items available", example = "100")
  val totalCount: Long,
) {
  companion object {
    fun fromModel(pagination: PaginationInfo): PaginationInfoDto =
      PaginationInfoDto(
        page = pagination.page,
        size = pagination.size,
        totalCount = pagination.totalCount,
      )
  }
}
