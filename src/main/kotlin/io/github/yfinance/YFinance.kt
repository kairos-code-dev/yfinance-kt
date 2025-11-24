package io.github.yfinance

/**
 * Main entry point for the YFinance-KT library
 *
 * This object provides convenient access to library functionality
 */
object YFinance {
    /**
     * Library version
     */
    const val VERSION = "1.0.0"

    /**
     * Create a new Ticker instance
     *
     * @param symbol The ticker symbol
     * @param enableLogging Enable HTTP logging
     * @return Ticker instance
     */
    fun ticker(symbol: String, enableLogging: Boolean = false): Ticker {
        return io.github.yfinance.ticker(symbol, enableLogging)
    }

    /**
     * Validate a ticker symbol format
     *
     * @param symbol The ticker symbol to validate
     * @return true if the format is valid
     */
    fun isValidSymbolFormat(symbol: String): Boolean {
        if (symbol.isBlank()) return false
        // Basic validation: alphanumeric, dots, and hyphens allowed
        return symbol.matches(Regex("^[A-Z0-9.-]+$"))
    }

    /**
     * Normalize a ticker symbol (convert to uppercase, trim)
     *
     * @param symbol The ticker symbol to normalize
     * @return Normalized symbol
     */
    fun normalizeSymbol(symbol: String): String {
        return symbol.trim().uppercase()
    }
}
