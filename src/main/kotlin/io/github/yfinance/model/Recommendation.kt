package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents an analyst recommendation
 *
 * @property date The date of the recommendation (epoch seconds)
 * @property firm The analyst firm name
 * @property toGrade The new recommendation grade (e.g., "Buy", "Hold", "Sell")
 * @property fromGrade The previous recommendation grade
 * @property action The action taken (e.g., "upgrade", "downgrade", "init", "main")
 */
@Serializable
data class Recommendation(
    val date: Long,
    val firm: String,
    val toGrade: String,
    val fromGrade: String? = null,
    val action: String? = null
) {
    /**
     * Get the date as an Instant
     */
    fun getInstant(): Instant = Instant.fromEpochSeconds(date)

    /**
     * Check if this is an upgrade
     */
    fun isUpgrade(): Boolean = action?.equals("upgrade", ignoreCase = true) == true

    /**
     * Check if this is a downgrade
     */
    fun isDowngrade(): Boolean = action?.equals("downgrade", ignoreCase = true) == true

    /**
     * Check if this is an initial coverage
     */
    fun isInitCoverage(): Boolean = action?.equals("init", ignoreCase = true) == true
}

/**
 * Represents a collection of analyst recommendations
 *
 * @property symbol The ticker symbol
 * @property recommendations List of recommendations
 */
@Serializable
data class RecommendationData(
    val symbol: String,
    val recommendations: List<Recommendation>
) {
    /**
     * Get recommendations sorted by date (newest first)
     */
    fun getSortedRecommendations(): List<Recommendation> =
        recommendations.sortedByDescending { it.date }

    /**
     * Filter recommendations by firm
     */
    fun filterByFirm(firm: String): List<Recommendation> =
        recommendations.filter { it.firm.equals(firm, ignoreCase = true) }

    /**
     * Get only upgrades
     */
    fun getUpgrades(): List<Recommendation> = recommendations.filter { it.isUpgrade() }

    /**
     * Get only downgrades
     */
    fun getDowngrades(): List<Recommendation> = recommendations.filter { it.isDowngrade() }

    /**
     * Get recommendation summary (count by grade)
     */
    fun getSummary(): Map<String, Int> {
        return recommendations.groupingBy { it.toGrade }.eachCount()
    }
}
