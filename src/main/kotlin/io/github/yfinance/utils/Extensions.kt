package io.github.yfinance.utils

import io.github.yfinance.model.Quote
import kotlinx.datetime.*

/**
 * Extension functions for common operations
 */

/**
 * Convert epoch seconds to LocalDateTime in UTC
 */
fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.fromEpochSeconds(this)
        .toLocalDateTime(TimeZone.UTC)
}

/**
 * Convert LocalDateTime to epoch seconds
 */
fun LocalDateTime.toEpochSeconds(): Long {
    return this.toInstant(TimeZone.UTC).epochSeconds
}

/**
 * Get the date string from a Quote in ISO format
 */
fun Quote.getDateString(): String {
    return timestamp.toLocalDateTime().toString()
}

/**
 * Calculate percentage change between two values
 */
fun calculatePercentageChange(oldValue: Double, newValue: Double): Double {
    if (oldValue == 0.0) return 0.0
    return ((newValue - oldValue) / oldValue) * 100.0
}

/**
 * Calculate simple moving average
 */
fun List<Quote>.calculateSMA(period: Int): List<Pair<Long, Double>> {
    if (size < period) return emptyList()

    return windowed(period) { window ->
        val avg = window.mapNotNull { it.close }.average()
        window.last().timestamp to avg
    }
}

/**
 * Get the highest price in the list
 */
fun List<Quote>.getHighestPrice(): Quote? {
    return maxByOrNull { it.high ?: Double.MIN_VALUE }
}

/**
 * Get the lowest price in the list
 */
fun List<Quote>.getLowestPrice(): Quote? {
    return minByOrNull { it.low ?: Double.MAX_VALUE }
}

/**
 * Get total volume
 */
fun List<Quote>.getTotalVolume(): Long {
    return mapNotNull { it.volume }.sum()
}

/**
 * Get average volume
 */
fun List<Quote>.getAverageVolume(): Double {
    val volumes = mapNotNull { it.volume }
    return if (volumes.isEmpty()) 0.0 else volumes.average()
}
