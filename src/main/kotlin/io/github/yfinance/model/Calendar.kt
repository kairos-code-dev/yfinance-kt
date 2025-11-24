package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents calendar events for a ticker (earnings dates, dividends, etc.)
 *
 * @property symbol The ticker symbol
 * @property earnings Earnings announcement date (epoch seconds)
 * @property exDividendDate Ex-dividend date (epoch seconds)
 * @property dividendDate Dividend payment date (epoch seconds)
 */
@Serializable
data class CalendarData(
    val symbol: String,
    val earnings: Long? = null,
    val exDividendDate: Long? = null,
    val dividendDate: Long? = null
) {
    /**
     * Get earnings date as Instant
     */
    fun getEarningsInstant(): Instant? = earnings?.let { Instant.fromEpochSeconds(it) }

    /**
     * Get ex-dividend date as Instant
     */
    fun getExDividendInstant(): Instant? = exDividendDate?.let { Instant.fromEpochSeconds(it) }

    /**
     * Get dividend payment date as Instant
     */
    fun getDividendInstant(): Instant? = dividendDate?.let { Instant.fromEpochSeconds(it) }

    /**
     * Check if earnings date is available
     */
    fun hasEarnings(): Boolean = earnings != null

    /**
     * Check if dividend information is available
     */
    fun hasDividendInfo(): Boolean = exDividendDate != null || dividendDate != null
}
