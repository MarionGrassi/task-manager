package com.personal.adapters.inbound.web.errormanager

import com.personal.adapters.inbound.web.errormanager.dto.ErrorResponse
import com.personal.usecase.errors.UseCaseError
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApplicationErrorHandler {
    @ExceptionHandler(ApplicationException::class)
    fun handleCreationEx(
        ex: ApplicationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error = ex.error
        val status =
            when (error) {
                is UseCaseError.DomainValidation -> HttpStatus.BAD_REQUEST
                is UseCaseError.TaskNotFound -> HttpStatus.NOT_FOUND
                is UseCaseError.PageNumberNegative -> HttpStatus.BAD_REQUEST
                is UseCaseError.PageSizeNegative -> HttpStatus.BAD_REQUEST
                is UseCaseError.PageSizeTooLarge -> HttpStatus.BAD_REQUEST
            }

        return ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    status = status.value(),
                    error = error.message,
                    code = error.code,
                    path = request.requestURI.orEmpty(),
                ),
            )
    }
}
