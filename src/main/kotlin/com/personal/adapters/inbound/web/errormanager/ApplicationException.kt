package com.personal.adapters.inbound.web.errormanager

import com.personal.usecase.errors.UseCaseError

class ApplicationException(
    val error: UseCaseError,
) : RuntimeException()
