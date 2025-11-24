package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Major holders breakdown
 *
 * @property insidersPercentHeld Percentage held by insiders
 * @property institutionsPercentHeld Percentage held by institutions
 * @property institutionsFloatPercentHeld Percentage of float held by institutions
 * @property institutionsCount Number of institutions holding shares
 */
@Serializable
data class MajorHoldersData(
    val symbol: String,
    val insidersPercentHeld: Double? = null,
    val institutionsPercentHeld: Double? = null,
    val institutionsFloatPercentHeld: Double? = null,
    val institutionsCount: Int? = null
)

/**
 * Represents an institutional holder
 *
 * @property holder Name of the institution
 * @property shares Number of shares held
 * @property dateReported Date reported (epoch seconds)
 * @property percentOut Percentage of outstanding shares
 * @property value Value of holdings
 */
@Serializable
data class InstitutionalHolder(
    val holder: String,
    val shares: Long,
    val dateReported: Long? = null,
    val percentOut: Double? = null,
    val value: Long? = null
)

/**
 * Collection of institutional holders
 */
@Serializable
data class InstitutionalHoldersData(
    val symbol: String,
    val holders: List<InstitutionalHolder>
)

/**
 * Represents a mutual fund holder
 *
 * @property holder Name of the mutual fund
 * @property shares Number of shares held
 * @property dateReported Date reported (epoch seconds)
 * @property percentOut Percentage of outstanding shares
 * @property value Value of holdings
 */
@Serializable
data class MutualFundHolder(
    val holder: String,
    val shares: Long,
    val dateReported: Long? = null,
    val percentOut: Double? = null,
    val value: Long? = null
)

/**
 * Collection of mutual fund holders
 */
@Serializable
data class MutualFundHoldersData(
    val symbol: String,
    val holders: List<MutualFundHolder>
)

/**
 * Represents an insider transaction
 *
 * @property insider Name of the insider
 * @property relation Relationship to company
 * @property lastDate Date of last transaction (epoch seconds)
 * @property transactionType Type of transaction (Buy/Sale)
 * @property ownerType Type of owner
 * @property sharesTraded Number of shares traded
 * @property sharesHeld Number of shares held after transaction
 * @property value Value of transaction
 */
@Serializable
data class InsiderTransaction(
    val insider: String,
    val relation: String? = null,
    val lastDate: Long? = null,
    val transactionType: String? = null,
    val ownerType: String? = null,
    val sharesTraded: Long? = null,
    val sharesHeld: Long? = null,
    val value: Long? = null
)

/**
 * Collection of insider transactions
 */
@Serializable
data class InsiderTransactionsData(
    val symbol: String,
    val transactions: List<InsiderTransaction>
) {
    /**
     * Get only purchase transactions
     */
    fun getPurchases(): List<InsiderTransaction> =
        transactions.filter { it.transactionType?.contains("Buy", ignoreCase = true) == true }

    /**
     * Get only sale transactions
     */
    fun getSales(): List<InsiderTransaction> =
        transactions.filter { it.transactionType?.contains("Sale", ignoreCase = true) == true }
}

/**
 * Represents an insider with their position
 *
 * @property name Name of the insider
 * @property position Position in the company
 * @property url Profile URL
 * @property recentTransaction Recent transaction date
 * @property positionDirectDate Position direct date
 * @property shares Number of shares held
 */
@Serializable
data class InsiderRosterHolder(
    val name: String,
    val position: String? = null,
    val url: String? = null,
    val recentTransaction: Long? = null,
    val positionDirectDate: Long? = null,
    val shares: Long? = null
)

/**
 * Collection of insider roster holders
 */
@Serializable
data class InsiderRosterHoldersData(
    val symbol: String,
    val holders: List<InsiderRosterHolder>
)
