package com.github.b1412.error

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import mu.KotlinLogging
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler
    fun requestBodyError(exception: HttpMessageNotReadableException): ResponseEntity<*> {
        logger.error("read http request error", exception)
        val cause = exception.cause
        if (cause is InvalidFormatException) {
            val errorFields = listOf(ErrorField(message = cause.originalMessage, field = cause.getErrorField()))
            return ResponseEntity
                .status(BAD_REQUEST)
                .body(ErrorDTO(message = "Invalid parameter(s).", errorFields = errorFields))
        }
        return ResponseEntity
            .status(BAD_REQUEST)
            .body(ErrorDTO(message = exception.message!!))
    }

    @ExceptionHandler
    fun invalidRequestError(exception: MethodArgumentNotValidException): ResponseEntity<*> {
        logger.error("invalid request body", exception)
        val errorFields = exception.bindingResult.fieldErrors.map {
            ErrorField(message = it.defaultMessage!!, field = it.field)
        }
        return ResponseEntity
            .status(BAD_REQUEST)
            .body(ErrorDTO(message = "Invalid parameter(s).", errorFields = errorFields))
    }

    @ExceptionHandler
    fun handleUnknownException(exception: Exception): ResponseEntity<*> {
        logger.error("unknown error", exception)
        return ResponseEntity
            .status(INTERNAL_SERVER_ERROR)
            .body(ErrorDTO(message = exception.message!!))
    }

    private fun InvalidFormatException.getErrorField(): String {
        return this.path.joinToString("") {
            if (it.index == -1) ".${it.fieldName}" else "[${it.index}]"
        }.substring(1)
    }
}