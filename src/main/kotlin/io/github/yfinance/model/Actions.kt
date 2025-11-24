package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents all corporate actions (dividends, splits, capital gains)
 *
 * @property date The date of the action (epoch seconds)
 * @property dividends Dividend amount (if applicable)
 * @property splits Split ratio (if applicable)
 * @property capitalGains Capital gains amount (if applicable)
 */
@Serializable
data class Action(
    val date: Long,
    val dividends: Double? = null,
    val splits: Double? = null,
    val capitalGains: Double? = null
) {
    /**
     * Get the date as an Instant
     */
    fun getInstant(): Instant = Instant.fromEpochSeconds(date)

    /**
     * Check if this is a dividend action
     */
    fun isDividend(): Boolean = dividends != null && dividends > 0

    /**
     * Check if this is a split action
     */
    fun isSplit(): Boolean = splits != null && splits != 0.0

    /**
     * Check if this is a capital gains action
     */
    fun isCapitalGains(): Boolean = capitalGains != null && capitalGains > 0
}

/**
 * Represents a collection of corporate actions
 *
 * @property symbol The ticker symbol
 * @property actions List of actions
 */
@Serializable
data class ActionsData(
    val symbol: String,
    val actions: List<Action>
) {
    /**
     * Get actions sorted by date (oldest first)
     */
    fun getSortedActions(): List<Action> = actions.sortedBy { it.date }

    /**
     * Get only dividend actions
     */
    fun getDividends(): List<Action> = actions.filter { it.isDividend() }

    /**
     * Get only split actions
     */
    fun getSplits(): List<Action> = actions.filter { it.isSplit() }

    /**
     * Get only capital gains actions
     */
    fun getCapitalGains(): List<Action> = actions.filter { it.isCapitalGains() }
}

/**
 * Represents capital gains distribution (primarily for funds)
 *
 * @property date The date of the distribution (epoch seconds)
 * @property amount The capital gains amount per share
 */
@Serializable
data class CapitalGain(
    val date: Long,
    val amount: Double
) {
    fun getInstant(): Instant = Instant.fromEpochSeconds(date)
}

/**
 * Represents a collection of capital gains
 *
 * @property symbol The ticker symbol
 * @property capitalGains List of capital gains
 */
@Serializable
data class CapitalGainsData(
    val symbol: String,
    val capitalGains: List<CapitalGain>
) {
    fun getSortedCapitalGains(): List<CapitalGain> = capitalGains.sortedBy { it.date }
    fun getTotalAmount(): Double = capitalGains.sumOf { it.amount }
}
