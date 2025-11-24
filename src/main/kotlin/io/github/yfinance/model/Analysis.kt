package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Analyst recommendation
 *
 * @property date Recommendation date (epoch seconds)
 * @property firm Analyst firm name
 * @property toGrade New rating
 * @property fromGrade Previous rating
 * @property action Action taken (upgrade, downgrade, init, main, reit)
 */
@Serializable
data class Recommendation(
    val date: Long,
    val firm: String,
    val toGrade: String? = null,
    val fromGrade: String? = null,
    val action: String? = null
)

/**
 * Collection of analyst recommendations
 */
@Serializable
data class RecommendationsData(
    val symbol: String,
    val recommendations: List<Recommendation>
) {
    /**
     * Get recommendations sorted by date (newest first)
     */
    fun getSortedRecommendations(): List<Recommendation> =
        recommendations.sortedByDescending { it.date }

    /**
     * Get only upgrades
     */
    fun getUpgrades(): List<Recommendation> =
        recommendations.filter { it.action?.contains("up", ignoreCase = true) == true }

    /**
     * Get only downgrades
     */
    fun getDowngrades(): List<Recommendation> =
        recommendations.filter { it.action?.contains("down", ignoreCase = true) == true }
}

/**
 * Summary of analyst recommendations
 *
 * @property period Time period (e.g., "0m", "-1m", "-2m", "-3m")
 * @property strongBuy Number of strong buy ratings
 * @property buy Number of buy ratings
 * @property hold Number of hold ratings
 * @property sell Number of sell ratings
 * @property strongSell Number of strong sell ratings
 */
@Serializable
data class RecommendationSummary(
    val period: String,
    val strongBuy: Int = 0,
    val buy: Int = 0,
    val hold: Int = 0,
    val sell: Int = 0,
    val strongSell: Int = 0
) {
    /**
     * Get total number of recommendations
     */
    fun getTotal(): Int = strongBuy + buy + hold + sell + strongSell

    /**
     * Get average recommendation (1=Strong Buy, 5=Strong Sell)
     */
    fun getAverage(): Double {
        val total = getTotal()
        if (total == 0) return 0.0
        return (strongBuy * 1.0 + buy * 2.0 + hold * 3.0 + sell * 4.0 + strongSell * 5.0) / total
    }
}

/**
 * Collection of recommendation summaries
 */
@Serializable
data class RecommendationsSummaryData(
    val symbol: String,
    val summaries: List<RecommendationSummary>
)

/**
 * Analyst price targets
 *
 * @property current Current price
 * @property targetHigh Highest target
 * @property targetLow Lowest target
 * @property targetMean Mean target
 * @property targetMedian Median target
 */
@Serializable
data class AnalystPriceTargets(
    val symbol: String,
    val current: Double? = null,
    val targetHigh: Double? = null,
    val targetLow: Double? = null,
    val targetMean: Double? = null,
    val targetMedian: Double? = null
) {
    /**
     * Calculate potential upside/downside from mean target
     */
    fun getUpsideFromMean(): Double? {
        if (current == null || targetMean == null) return null
        return ((targetMean - current) / current) * 100.0
    }
}

/**
 * Earnings data
 *
 * @property date Earnings date (epoch seconds)
 * @property epsEstimate Estimated EPS
 * @property epsActual Actual EPS
 * @property epsDifference Difference between actual and estimate
 * @property surprisePercent Surprise percentage
 */
@Serializable
data class Earnings(
    val date: Long,
    val epsEstimate: Double? = null,
    val epsActual: Double? = null,
    val epsDifference: Double? = null,
    val surprisePercent: Double? = null
)

/**
 * Collection of earnings data
 */
@Serializable
data class EarningsData(
    val symbol: String,
    val earnings: List<Earnings>,
    val frequency: StatementFrequency = StatementFrequency.YEARLY
) {
    /**
     * Get earnings sorted by date (newest first)
     */
    fun getSortedEarnings(): List<Earnings> = earnings.sortedByDescending { it.date }
}

/**
 * Earnings dates
 *
 * @property symbol The ticker symbol
 * @property earningsDate Earnings announcement date (epoch seconds)
 * @property epsEstimate Estimated EPS
 * @property revenueEstimate Estimated revenue
 * @property earningsCallTime Time of earnings call
 */
@Serializable
data class EarningsDate(
    val symbol: String,
    val earningsDate: Long,
    val epsEstimate: Double? = null,
    val revenueEstimate: Long? = null,
    val earningsCallTime: String? = null
)

/**
 * Collection of earnings dates
 */
@Serializable
data class EarningsDatesData(
    val symbol: String,
    val dates: List<EarningsDate>
)
