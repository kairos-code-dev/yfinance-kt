package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Major holders breakdown information
 *
 * @property symbol The ticker symbol
 * @property insidersPercent Percentage held by insiders
 * @property institutionsPercent Percentage held by institutions
 * @property institutionsFloatPercent Percentage of float held by institutions
 * @property institutionsCount Number of institutional holders
 */
@Serializable
data class MajorHolders(
    val symbol: String,
    val insidersPercent: Double? = null,
    val institutionsPercent: Double? = null,
    val institutionsFloatPercent: Double? = null,
    val institutionsCount: Long? = null
)

/**
 * Institutional holder information
 *
 * @property organization Name of the institution
 * @property percentHeld Percentage of shares held
 * @property shares Number of shares held
 * @property value Value of holdings
 * @property reportDate Date of the report (epoch seconds)
 */
@Serializable
data class InstitutionalHolder(
    val organization: String,
    val percentHeld: Double? = null,
    val shares: Long? = null,
    val value: Long? = null,
    val reportDate: Long? = null
)

/**
 * Collection of institutional holders
 *
 * @property symbol The ticker symbol
 * @property holders List of institutional holders
 */
@Serializable
data class InstitutionalHoldersData(
    val symbol: String,
    val holders: List<InstitutionalHolder>
) {
    /**
     * Get top N holders by percentage
     */
    fun getTopHolders(n: Int = 10): List<InstitutionalHolder> {
        return holders
            .sortedByDescending { it.percentHeld ?: 0.0 }
            .take(n)
    }

    /**
     * Get total percentage held by all institutional holders
     */
    fun getTotalPercentageHeld(): Double {
        return holders.mapNotNull { it.percentHeld }.sum()
    }
}
