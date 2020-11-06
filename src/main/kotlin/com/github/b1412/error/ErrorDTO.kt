package com.github.b1412.error

data class ErrorDTO(
    val message: String,
    val errorFields: List<ErrorField>? = null
)

data class ErrorField(
    val field: String,
    val message: String
)
