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
     * Get all corporate actions (dividends and splits combined)
     *
     * @param period The time period to fetch (default: max available)
     * @return Result containing action data
     */
    suspend fun actions(period: Period = Period.MAX): YFinanceResult<ActionData> {
        return client.getActions(symbol, period)
    }

    /**
     * Get calendar events (earnings dates, dividend dates)
     *
     * @return Result containing calendar data
     */
    suspend fun calendar(): YFinanceResult<CalendarData> {
        return client.getCalendar(symbol)
    }

    /**
     * Get income statement
     *
     * @param frequency The frequency (annual, quarterly, or trailing)
     * @return Result containing financial statement
     */
    suspend fun incomeStatement(frequency: Frequency = Frequency.ANNUAL): YFinanceResult<FinancialStatement> {
        return client.getIncomeStatement(symbol, frequency)
    }

    /**
     * Get balance sheet
     *
     * @param frequency The frequency (annual, quarterly, or trailing)
     * @return Result containing financial statement
     */
    suspend fun balanceSheet(frequency: Frequency = Frequency.ANNUAL): YFinanceResult<FinancialStatement> {
        return client.getBalanceSheet(symbol, frequency)
    }

    /**
     * Get cash flow statement
     *
     * @param frequency The frequency (annual, quarterly, or trailing)
     * @return Result containing financial statement
     */
    suspend fun cashFlow(frequency: Frequency = Frequency.ANNUAL): YFinanceResult<FinancialStatement> {
        return client.getCashFlow(symbol, frequency)
    }

    /**
     * Get news articles
     *
     * @return Result containing news data
     */
    suspend fun news(): YFinanceResult<NewsData> {
        return client.getNews(symbol)
    }

    /**
     * Get analyst recommendations
     *
     * @return Result containing recommendation data
     */
    suspend fun recommendations(): YFinanceResult<RecommendationData> {
        return client.getRecommendations(symbol)
    }

    /**
     * Get major holders breakdown
     *
     * @return Result containing major holders data
     */
    suspend fun majorHolders(): YFinanceResult<MajorHolders> {
        return client.getMajorHolders(symbol)
    }

    /**
     * Get institutional holders
     *
     * @return Result containing institutional holders data
     */
    suspend fun institutionalHolders(): YFinanceResult<InstitutionalHoldersData> {
        return client.getInstitutionalHolders(symbol)
    }

    /**
     * Get earnings history
     *
     * @return Result containing earnings history data
     */
    suspend fun earningsHistory(): YFinanceResult<EarningsHistoryData> {
        return client.getEarningsHistory(symbol)
    }

    /**
     * Get full earnings data
     *
     * @return Result containing full earnings data
     */
    suspend fun earnings(): YFinanceResult<FullEarningsData> {
        return client.getEarnings(symbol)
    }

    /**
     * Get available option expiration dates
     *
     * @return Result containing list of expiration dates (epoch seconds)
     */
    suspend fun options(): YFinanceResult<List<Long>> {
        return client.getOptions(symbol)
    }

    /**
     * Get option chain for specific expiration date
     *
     * @param expiration Expiration date (epoch seconds)
     * @return Result containing option chain with calls and puts
     */
    suspend fun optionChain(expiration: Long): YFinanceResult<OptionChain> {
        return client.getOptionChain(symbol, expiration)
    }

    /**
     * Get fast info (quick access to key ticker data)
     *
     * @return Result containing fast info
     */
    suspend fun fastInfo(): YFinanceResult<FastInfo> {
        return client.getFastInfo(symbol)
    }

    /**
     * Get sustainability/ESG scores
     *
     * @return Result containing sustainability data
     */
    suspend fun sustainability(): YFinanceResult<Sustainability> {
        return client.getSustainability(symbol)
    }

    /**
     * Get capital gains distributions
     *
     * @param period The time period to fetch (default: max available)
     * @return Result containing capital gains data
     */
    suspend fun capitalGains(period: Period = Period.MAX): YFinanceResult<CapitalGainsData> {
        return client.getCapitalGains(symbol, period)
    }

    /**
     * Get shares outstanding
     *
     * @return Result containing shares data
     */
    suspend fun shares(): YFinanceResult<SharesData> {
        return client.getShares(symbol)
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
