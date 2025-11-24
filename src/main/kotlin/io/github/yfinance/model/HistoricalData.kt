package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Represents historical price data for a security
 *
 * @property symbol The ticker symbol
 * @property quotes List of historical quotes
 * @property currency The currency of the prices
 */
@Serializable
data class HistoricalData(
    val symbol: String,
    val quotes: List<Quote>,
    val currency: String? = null
) {
    /**
     * Get quotes sorted by timestamp (oldest first)
     */
    fun getSortedQuotes(): List<Quote> = quotes.sortedBy { it.timestamp }

    /**
     * Get quotes sorted by timestamp (newest first)
     */
    fun getSortedQuotesDesc(): List<Quote> = quotes.sortedByDescending { it.timestamp }

    /**
     * Filter quotes by timestamp range
     */
    fun filterByRange(startTimestamp: Long, endTimestamp: Long): List<Quote> =
        quotes.filter { it.timestamp in startTimestamp..endTimestamp }
}
