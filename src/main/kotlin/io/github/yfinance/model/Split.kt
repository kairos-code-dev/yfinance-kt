package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a stock split event
 *
 * @property date The date of the split (epoch seconds)
 * @property ratio The split ratio (e.g., 2.0 for a 2-for-1 split)
 */
@Serializable
data class Split(
    val date: Long,
    val ratio: Double
) {
    /**
     * Get the date as an Instant
     */
    fun getInstant(): Instant = Instant.fromEpochSeconds(date)

    /**
     * Check if this is a forward split (ratio > 1)
     */
    fun isForwardSplit(): Boolean = ratio > 1.0

    /**
     * Check if this is a reverse split (ratio < 1)
     */
    fun isReverseSplit(): Boolean = ratio < 1.0
}

/**
 * Represents a collection of stock splits
 *
 * @property symbol The ticker symbol
 * @property splits List of stock splits
 */
@Serializable
data class SplitData(
    val symbol: String,
    val splits: List<Split>
) {
    /**
     * Get splits sorted by date (oldest first)
     */
    fun getSortedSplits(): List<Split> = splits.sortedBy { it.date }
}
