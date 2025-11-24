package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a dividend payment
 *
 * @property date The date of the dividend (epoch seconds)
 * @property amount The dividend amount per share
 */
@Serializable
data class Dividend(
    val date: Long,
    val amount: Double
) {
    /**
     * Get the date as an Instant
     */
    fun getInstant(): Instant = Instant.fromEpochSeconds(date)
}

/**
 * Represents a collection of dividend payments
 *
 * @property symbol The ticker symbol
 * @property dividends List of dividend payments
 */
@Serializable
data class DividendData(
    val symbol: String,
    val dividends: List<Dividend>
) {
    /**
     * Get dividends sorted by date (oldest first)
     */
    fun getSortedDividends(): List<Dividend> = dividends.sortedBy { it.date }

    /**
     * Get total dividends paid in the period
     */
    fun getTotalAmount(): Double = dividends.sumOf { it.amount }
}
