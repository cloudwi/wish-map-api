package com.mindbridge.wishmap.exception

data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)
