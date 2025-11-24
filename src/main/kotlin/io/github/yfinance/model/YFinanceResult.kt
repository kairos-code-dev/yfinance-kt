package io.github.yfinance.model

/**
 * Result type for YFinance operations
 */
sealed class YFinanceResult<out T> {
    /**
     * Successful result
     */
    data class Success<T>(val data: T) : YFinanceResult<T>()

    /**
     * Failed result with error information
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val errorType: ErrorType = ErrorType.UNKNOWN
    ) : YFinanceResult<Nothing>()

    /**
     * Types of errors that can occur
     */
    enum class ErrorType {
        NETWORK_ERROR,
        INVALID_SYMBOL,
        INVALID_PARAMETERS,
        PARSING_ERROR,
        API_ERROR,
        RATE_LIMIT,
        UNKNOWN
    }

    /**
     * Check if result is successful
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Check if result is an error
     */
    fun isError(): Boolean = this is Error

    /**
     * Get data if successful, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Get data if successful, throw exception otherwise
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw YFinanceException(message, cause, errorType)
    }

    /**
     * Execute block if successful
     */
    inline fun onSuccess(block: (T) -> Unit): YFinanceResult<T> {
        if (this is Success) {
            block(data)
        }
        return this
    }

    /**
     * Execute block if error
     */
    inline fun onError(block: (Error) -> Unit): YFinanceResult<T> {
        if (this is Error) {
            block(this)
        }
        return this
    }

    /**
     * Map successful result to another type
     */
    inline fun <R> map(transform: (T) -> R): YFinanceResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Exception thrown by YFinance operations
 */
class YFinanceException(
    message: String,
    cause: Throwable? = null,
    val errorType: YFinanceResult.Error.ErrorType = YFinanceResult.Error.ErrorType.UNKNOWN
) : Exception(message, cause)
