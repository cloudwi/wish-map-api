package com.mindbridge.wishmap.common.error

data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)
