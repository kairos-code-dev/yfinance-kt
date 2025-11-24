package io.github.yfinance

import io.github.yfinance.client.YFinanceClient
import io.github.yfinance.model.*

/**
 * Main class for interacting with a single ticker symbol
 *
 * Example usage:
 * ```kotlin
 * val ticker = Ticker("AAPL")
 * val history = ticker.history(period = Period.ONE_MONTH)
 * val info = ticker.info()
 * ```
 *
 * @property symbol The ticker symbol (e.g., "AAPL", "TSLA")
 * @property client Optional custom YFinanceClient instance
 */
class Ticker(
    val symbol: String,
    private val client: YFinanceClient = YFinanceClient()
) {
    /**
     * Get historical price data
     *
     * @param period The time period to fetch (default: 1 month)
     * @param interval The data interval (default: 1 day)
     * @param includeEvents Include dividend and split events (default: true)
     * @return Result containing historical data
     */
    suspend fun history(
        period: Period = Period.ONE_MONTH,
        interval: Interval = Interval.ONE_DAY,
        includeEvents: Boolean = true
    ): YFinanceResult<HistoricalData> {
        return client.getHistoricalData(symbol, period, interval, includeEvents)
    }

    /**
     * Get comprehensive ticker information
     *
     * @return Result containing ticker information
     */
    suspend fun info(): YFinanceResult<TickerInfo> {
        return client.getTickerInfo(symbol)
    }

    /**
     * Get dividend history
     *
     * @param period The time period to fetch (default: max available)
     * @return Result containing dividend data
     */
    suspend fun dividends(period: Period = Period.MAX): YFinanceResult<DividendData> {
        return client.getDividends(symbol, period)
    }

    /**
     * Get stock split history
     *
     * @param period The time period to fetch (default: max available)
     * @return Result containing split data
     */
    suspend fun splits(period: Period = Period.MAX): YFinanceResult<SplitData> {
        return client.getSplits(symbol, period)
    }

    /**
     * Extension function to get history with custom date range
     * This is a convenience method that uses the max period and filters the results
     *
     * @param startTimestamp Start timestamp in epoch seconds
     * @param endTimestamp End timestamp in epoch seconds
     * @param interval The data interval (default: 1 day)
     * @return Result containing filtered historical data
     */
    suspend fun historyByRange(
        startTimestamp: Long,
        endTimestamp: Long,
        interval: Interval = Interval.ONE_DAY
    ): YFinanceResult<HistoricalData> {
        return history(Period.MAX, interval, includeEvents = false).map { data ->
            data.copy(quotes = data.filterByRange(startTimestamp, endTimestamp))
        }
    }
}

/**
 * Convenience function to create a Ticker instance
 *
 * @param symbol The ticker symbol
 * @param enableLogging Enable HTTP logging
 * @return Ticker instance
 */
fun ticker(
    symbol: String,
    enableLogging: Boolean = false
): Ticker {
    return Ticker(symbol, YFinanceClient(enableLogging))
}

/**
 * DSL builder for configuring and using a Ticker
 *
 * Example:
 * ```kotlin
 * val data = ticker("AAPL") {
 *     history(Period.ONE_YEAR, Interval.ONE_DAY)
 * }
 * ```
 */
suspend inline fun <T> ticker(
    symbol: String,
    enableLogging: Boolean = false,
    block: suspend Ticker.() -> YFinanceResult<T>
): YFinanceResult<T> {
    val ticker = Ticker(symbol, YFinanceClient(enableLogging))
    return block(ticker)
}
