package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a single price quote/bar for a security
 *
 * @property timestamp The timestamp of the quote
 * @property open Opening price
 * @property high Highest price during the period
 * @property low Lowest price during the period
 * @property close Closing price
 * @property adjClose Adjusted closing price (accounts for splits, dividends)
 * @property volume Trading volume
 */
@Serializable
data class Quote(
    val timestamp: Long,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val close: Double? = null,
    val adjClose: Double? = null,
    val volume: Long? = null
) {
    /**
     * Get the timestamp as an Instant
     */
    fun getInstant(): Instant = Instant.fromEpochSeconds(timestamp)
}
