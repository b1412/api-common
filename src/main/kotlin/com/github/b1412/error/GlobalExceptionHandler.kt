package com.github.b1412.error

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler



/**
 * Handle exceptions threw by frameworks
 */
@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler
    fun httpMessageNotReadable(exception: HttpMessageNotReadableException): ResponseEntity<*> {
        val cause = exception.cause
        if (cause is InvalidFormatException) {
            val errorFields = listOf(ErrorField(message = cause.originalMessage, field = cause.getErrorField()))
            return ResponseEntity
                .status(BAD_REQUEST)
                .body(ErrorDTO(message = "Invalid parameter(s).", errorFields = errorFields))
        } else if (cause is JsonParseException) {
            return ResponseEntity
                .status(BAD_REQUEST)
                .body(ErrorDTO(message = cause.localizedMessage))
        }
        return ResponseEntity
            .status(BAD_REQUEST)
            .body(ErrorDTO(message = exception.message!!))
    }

    @ExceptionHandler
    fun methodArgumentNotValid(exception: MethodArgumentNotValidException): ResponseEntity<*> {
        val errorFields = exception.bindingResult.fieldErrors.map {
            ErrorField(message = it.defaultMessage!!, field = it.field)
        }
        return ResponseEntity
            .status(BAD_REQUEST)
            .body(ErrorDTO(message = "Invalid parameter(s).", errorFields = errorFields))
    }

    @ExceptionHandler
    fun dataIntegrityViolation(exception: DataIntegrityViolationException): ResponseEntity<*> {
        logger.error("db error", exception)
        val cause = exception.cause
        if (cause is ConstraintViolationException) {
            val (error, detail) = cause.sqlException.message!!.split("\n")
            return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                    ErrorDTO(
                        message = detail,
                        listOf(
                            ErrorField(
                                field = StringUtils.substringBetween(detail, "(", ")"),
                                message = error
                            )
                        )
                    )
                )
        }
        return ResponseEntity
            .status(INTERNAL_SERVER_ERROR)
            .body(ErrorDTO(message = "Data error"))
    }

    @ExceptionHandler
    fun invalidDataAccessApiUsage(exception: InvalidDataAccessApiUsageException): ResponseEntity<*> {
        return ResponseEntity
            .status(BAD_REQUEST)
            .body(
                ErrorDTO(message = exception.rootCause!!.message!!)
            )
    }

    @ExceptionHandler
    fun emptyResultDataAccessException(exception: EmptyResultDataAccessException): ResponseEntity<*> {
        val message = exception.message!!
        return ResponseEntity
            .status(BAD_REQUEST)
            .body(
                ErrorDTO(message = message)
            )
    }

    @ExceptionHandler
    fun httpRequestMethodNotSupportedException(exception: HttpRequestMethodNotSupportedException): ResponseEntity<*> {
        val message = exception.message!!
        return ResponseEntity.notFound().build<Unit>()
    }

    @ExceptionHandler
    fun unknown(exception: Exception): ResponseEntity<*> {
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