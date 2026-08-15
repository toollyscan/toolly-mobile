package com.toolly.foundation

/**
 * Provider-neutral result returned across Toolly boundaries.
 *
 * Infrastructure exceptions and SDK error types must be mapped before they reach this API.
 */
sealed interface ToollyResult<out T> {
    data class Success<T>(val value: T) : ToollyResult<T>

    data class Failure(
        val error: ToollyError,
    ) : ToollyResult<Nothing>
}

data class ToollyError(
    val code: ToollyErrorCode,
    val safeMessage: String,
)

enum class ToollyErrorCode {
    VALIDATION,
    UNAVAILABLE,
    UNAUTHORIZED,
    CONFLICT,
    QUOTA,
    CORRUPT,
    RETRYABLE,
    PERMANENT,
    UNKNOWN,
}
