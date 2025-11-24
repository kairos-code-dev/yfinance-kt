package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Earnings history item
 *
 * @property quarter Quarter date
 * @property epsActual Actual EPS reported
 * @property epsEstimate Estimated EPS
 * @property epsDifference Difference between actual and estimate
 * @property surprisePercent Surprise percentage
 */
@Serializable
data class EarningsHistoryItem(
    val quarter: String,
    val epsActual: Double? = null,
    val epsEstimate: Double? = null,
    val epsDifference: Double? = null,
    val surprisePercent: Double? = null
) {
    /**
     * Check if earnings beat estimates
     */
    fun beatEstimates(): Boolean {
        val actual = epsActual ?: return false
        val estimate = epsEstimate ?: return false
        return actual > estimate
    }
}

/**
 * Collection of earnings history
 *
 * @property symbol The ticker symbol
 * @property history List of earnings history items
 */
@Serializable
data class EarningsHistoryData(
    val symbol: String,
    val history: List<EarningsHistoryItem>
) {
    /**
     * Get earnings history sorted by quarter (newest first)
     */
    fun getSortedHistory(): List<EarningsHistoryItem> = history.sortedByDescending { it.quarter }

    /**
     * Get number of times earnings beat estimates
     */
    fun getBeatsCount(): Int = history.count { it.beatEstimates() }

    /**
     * Get number of times earnings missed estimates
     */
    fun getMissesCount(): Int = history.count { !it.beatEstimates() }
}

/**
 * Quarterly earnings data point
 *
 * @property date Quarter date
 * @property actual Actual EPS
 * @property estimate Estimated EPS
 */
@Serializable
data class QuarterlyEarningsData(
    val date: String,
    val actual: Double? = null,
    val estimate: Double? = null
)

/**
 * Full earnings data including chart information
 *
 * @property symbol The ticker symbol
 * @property quarterlyEarnings List of quarterly earnings
 * @property currentQuarterEstimate Current quarter EPS estimate
 * @property yearlyRevenue Yearly revenue data
 * @property quarterlyRevenue Quarterly revenue data
 */
@Serializable
data class FullEarningsData(
    val symbol: String,
    val quarterlyEarnings: List<QuarterlyEarningsData> = emptyList(),
    val currentQuarterEstimate: Double? = null,
    val yearlyRevenue: Map<Int, Long> = emptyMap(),
    val quarterlyRevenue: Map<String, Long> = emptyMap()
) {
    /**
     * Get the latest quarterly earnings
     */
    fun getLatestEarnings(): QuarterlyEarningsData? {
        return quarterlyEarnings.maxByOrNull { it.date }
    }

    /**
     * Get revenue growth year-over-year
     */
    fun getYearlyRevenueGrowth(): Map<Int, Double> {
        val sortedYears = yearlyRevenue.keys.sorted()
        return sortedYears.zipWithNext().associate { (year1, year2) ->
            val rev1 = yearlyRevenue[year1]?.toDouble() ?: return@associate year2 to 0.0
            val rev2 = yearlyRevenue[year2]?.toDouble() ?: return@associate year2 to 0.0
            year2 to ((rev2 - rev1) / rev1 * 100)
        }
    }
}
