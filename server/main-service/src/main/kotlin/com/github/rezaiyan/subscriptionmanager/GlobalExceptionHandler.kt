package com.github.rezaiyan.subscriptionmanager

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.converter.HttpMessageNotReadableException

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleException(ex: Exception): ResponseEntity<Map<String, Any?>> {
        ex.printStackTrace() // Log stack trace for debugging
        val errorDetails = mapOf(
            "error" to ex::class.simpleName,
            "message" to (ex.message ?: "No message"),
            "stackTrace" to ex.stackTrace.take(5).map { it.toString() }
        )
        return ResponseEntity(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(ex: MethodArgumentNotValidException): Map<String, Any> {
        return mapOf(
            "error" to "ValidationError",
            "message" to ex.bindingResult.allErrors.map { it.defaultMessage ?: it.toString() }
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): Map<String, Any> {
        return mapOf(
            "error" to "TypeMismatch",
            "message" to (ex.message ?: "Type mismatch error")
        )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): Map<String, Any> {
        return mapOf(
            "error" to "DataIntegrityViolation",
            "message" to (ex.message ?: "Data integrity violation")
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleNotReadable(ex: HttpMessageNotReadableException): Map<String, Any> {
        return mapOf(
            "error" to "HttpMessageNotReadable",
            "message" to (ex.message ?: "Malformed JSON request")
        )
    }
} 