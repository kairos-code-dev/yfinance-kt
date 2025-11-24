package io.github.yfinance

import io.github.yfinance.client.YFinanceClient
import io.github.yfinance.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Download historical data for multiple tickers simultaneously
 *
 * Example:
 * ```kotlin
 * val data = download(
 *     tickers = listOf("AAPL", "GOOGL", "MSFT"),
 *     period = Period.ONE_MONTH,
 *     interval = Interval.ONE_DAY
 * )
 * ```
 *
 * @param tickers List of ticker symbols
 * @param period The time period to fetch
 * @param interval The data interval
 * @param includeEvents Include dividend and split events
 * @param client Optional custom client
 * @return Map of symbol to result
 */
suspend fun download(
    tickers: List<String>,
    period: Period = Period.ONE_MONTH,
    interval: Interval = Interval.ONE_DAY,
    includeEvents: Boolean = true,
    client: YFinanceClient = YFinanceClient()
): Map<String, YFinanceResult<HistoricalData>> = coroutineScope {
    tickers.map { symbol ->
        async {
            symbol to client.getHistoricalData(symbol, period, interval, includeEvents)
        }
    }.awaitAll().toMap()
}

/**
 * Download historical data for multiple tickers (vararg version)
 */
suspend fun download(
    vararg tickers: String,
    period: Period = Period.ONE_MONTH,
    interval: Interval = Interval.ONE_DAY,
    includeEvents: Boolean = true,
    client: YFinanceClient = YFinanceClient()
): Map<String, YFinanceResult<HistoricalData>> =
    download(tickers.toList(), period, interval, includeEvents, client)

/**
 * Class for handling multiple tickers
 *
 * Example:
 * ```kotlin
 * val tickers = Tickers("AAPL", "GOOGL", "MSFT")
 * val data = tickers.history(Period.ONE_MONTH)
 * ```
 */
class Tickers(
    val symbols: List<String>,
    private val client: YFinanceClient = YFinanceClient()
) {
    constructor(vararg symbols: String, client: YFinanceClient = YFinanceClient()) : this(
        symbols.toList(),
        client
    )

    /**
     * Get historical data for all tickers
     */
    suspend fun history(
        period: Period = Period.ONE_MONTH,
        interval: Interval = Interval.ONE_DAY,
        includeEvents: Boolean = true
    ): Map<String, YFinanceResult<HistoricalData>> =
        download(symbols, period, interval, includeEvents, client)

    /**
     * Get ticker info for all tickers
     */
    suspend fun info(): Map<String, YFinanceResult<TickerInfo>> = coroutineScope {
        symbols.map { symbol ->
            async {
                symbol to client.getTickerInfo(symbol)
            }
        }.awaitAll().toMap()
    }

    /**
     * Get dividends for all tickers
     */
    suspend fun dividends(period: Period = Period.MAX): Map<String, YFinanceResult<DividendData>> =
        coroutineScope {
            symbols.map { symbol ->
                async {
                    symbol to client.getDividends(symbol, period)
                }
            }.awaitAll().toMap()
        }

    /**
     * Get individual ticker
     */
    fun ticker(symbol: String): Ticker {
        require(symbol in symbols) { "Symbol $symbol not in tickers list" }
        return Ticker(symbol, client)
    }

    /**
     * Get individual ticker by index
     */
    operator fun get(index: Int): Ticker {
        return Ticker(symbols[index], client)
    }

    /**
     * Get individual ticker by symbol
     */
    operator fun get(symbol: String): Ticker {
        return ticker(symbol)
    }
}

/**
 * Convenience function to create Tickers instance
 */
fun tickers(vararg symbols: String, enableLogging: Boolean = false): Tickers {
    return Tickers(symbols.toList(), YFinanceClient(enableLogging))
}

/**
 * Convenience function to create Tickers instance from list
 */
fun tickers(symbols: List<String>, enableLogging: Boolean = false): Tickers {
    return Tickers(symbols, YFinanceClient(enableLogging))
}
