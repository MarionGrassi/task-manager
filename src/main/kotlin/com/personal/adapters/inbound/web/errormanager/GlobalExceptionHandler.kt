package com.personal.adapters.inbound.web.errormanager

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.personal.adapters.inbound.web.errormanager.dto.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        logger.debug(ex) { "Validation error: $errors for path ${request.requestURI}" }

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                path = request.requestURI,
                details = errors,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.debug(ex) { "Method argument error: ${ex.message} for path ${request.requestURI}" }

        val paramName = ex.name
        val expected = ex.requiredType?.simpleName ?: "unknown"
        val received = ex.value?.toString() ?: "null"

        val fallbackDto =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Parameter ‘$paramName’ expected a value of type $expected but got ‘$received’.",
                path = request.requestURI,
            )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(fallbackDto)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.debug(ex) { "Illegal argument: ${ex.message} for path ${request.requestURI}" }

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                path = request.requestURI,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.debug(ex) { "Method not allowed: ${ex.method} not supported for path ${request.requestURI}" }

        // Build a brief message explaining which methods are allowed
        val allowed = ex.supportedHttpMethods?.joinToString(", ") { it.name() } ?: "N/A"
        val errorMessage = "Request method '${ex.method}' not supported. Supported methods: $allowed"

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.METHOD_NOT_ALLOWED.value(),
                error = errorMessage,
                path = request.requestURI,
            )
        // Add "Allow" header listing the allowed methods (optional but recommended)
        val headers =
            HttpHeaders().apply {
                ex.supportedHttpMethods?.forEach { add(HttpHeaders.ALLOW, it.name()) }
            }
        return ResponseEntity(errorResponse, headers, HttpStatus.METHOD_NOT_ALLOWED)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: InvalidFormatException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.debug(ex) { "'${request.pathInfo}' was called with wrong format" }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = ex.originalMessage ?: "Could not deserialize request",
            path = request.requestURI
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Unhandled exception: ${ex.message} for path ${request.requestURI}" }
        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "An unexpected error occurred. Please try again later or contact support.",
                path = request.requestURI,
            )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
