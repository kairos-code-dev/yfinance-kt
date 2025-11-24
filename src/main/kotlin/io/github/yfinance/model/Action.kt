package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a corporate action (dividend or split)
 */
sealed class Action {
    abstract val date: Long

    /**
     * Get the date as an Instant
     */
    fun getInstant(): Instant = Instant.fromEpochSeconds(date)

    /**
     * Dividend action
     */
    data class DividendAction(
        override val date: Long,
        val amount: Double
    ) : Action()

    /**
     * Split action
     */
    data class SplitAction(
        override val date: Long,
        val ratio: Double
    ) : Action() {
        /**
         * Check if this is a forward split (ratio > 1)
         */
        fun isForwardSplit(): Boolean = ratio > 1.0

        /**
         * Check if this is a reverse split (ratio < 1)
         */
        fun isReverseSplit(): Boolean = ratio < 1.0
    }
}

/**
 * Represents a collection of corporate actions (dividends and splits combined)
 *
 * @property symbol The ticker symbol
 * @property actions List of all corporate actions
 */
@Serializable
data class ActionData(
    val symbol: String,
    val dividends: List<Dividend>,
    val splits: List<Split>
) {
    /**
     * Get all actions as a unified list sorted by date
     */
    fun getAllActions(): List<Action> {
        val dividendActions = dividends.map { Action.DividendAction(it.date, it.amount) }
        val splitActions = splits.map { Action.SplitAction(it.date, it.ratio) }
        return (dividendActions + splitActions).sortedBy { it.date }
    }

    /**
     * Get only dividend actions
     */
    fun getDividendActions(): List<Action.DividendAction> =
        dividends.map { Action.DividendAction(it.date, it.amount) }.sortedBy { it.date }

    /**
     * Get only split actions
     */
    fun getSplitActions(): List<Action.SplitAction> =
        splits.map { Action.SplitAction(it.date, it.ratio) }.sortedBy { it.date }

    /**
     * Filter actions by date range
     */
    fun filterByRange(startTimestamp: Long, endTimestamp: Long): ActionData {
        return ActionData(
            symbol = symbol,
            dividends = dividends.filter { it.date in startTimestamp..endTimestamp },
            splits = splits.filter { it.date in startTimestamp..endTimestamp }
        )
    }
}
