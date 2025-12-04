package com.personal.adapters.inbound.web.errormanager.dto

import java.time.Instant

data class ErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val path: String,
    val code: String? = null,
    val details: List<String>? = null,
)
