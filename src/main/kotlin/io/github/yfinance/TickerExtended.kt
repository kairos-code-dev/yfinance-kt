package io.github.yfinance

import io.github.yfinance.client.YFinanceClient
import io.github.yfinance.model.*

/**
 * Extended Ticker class with all yfinance features
 *
 * This class provides access to all data available for a ticker symbol,
 * matching the functionality of the Python yfinance library.
 *
 * @property symbol The ticker symbol
 * @property client The API client
 */
class TickerExtended(
    val symbol: String,
    private val client: YFinanceClient = YFinanceClient()
) {
    // ==================== Price Data ====================

    /**
     * Get historical price data
     */
    suspend fun history(
        period: Period = Period.ONE_MONTH,
        interval: Interval = Interval.ONE_DAY,
        includeEvents: Boolean = true
    ): YFinanceResult<HistoricalData> = client.getHistoricalData(symbol, period, interval, includeEvents)

    /**
     * Get historical data by custom date range
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

    // ==================== Corporate Actions ====================

    /**
     * Get all corporate actions (dividends, splits, capital gains)
     */
    suspend fun actions(period: Period = Period.MAX): YFinanceResult<ActionsData> =
        client.getActions(symbol, period)

    /**
     * Get dividend history
     */
    suspend fun dividends(period: Period = Period.MAX): YFinanceResult<DividendData> =
        client.getDividends(symbol, period)

    /**
     * Get stock split history
     */
    suspend fun splits(period: Period = Period.MAX): YFinanceResult<SplitData> =
        client.getSplits(symbol, period)

    /**
     * Get capital gains history (primarily for funds)
     */
    suspend fun capitalGains(period: Period = Period.MAX): YFinanceResult<CapitalGainsData> =
        client.getCapitalGains(symbol, period)

    // ==================== Company Information ====================

    /**
     * Get comprehensive ticker information
     */
    suspend fun info(): YFinanceResult<TickerInfo> = client.getTickerInfo(symbol)

    /**
     * Get fast info (quickly accessible key metrics)
     */
    suspend fun fastInfo(): YFinanceResult<FastInfo> = client.getFastInfo(symbol)

    /**
     * Get ISIN identifier
     */
    suspend fun isin(): YFinanceResult<String> = client.getISIN(symbol)

    // ==================== Financial Statements ====================

    /**
     * Get income statement
     */
    suspend fun incomeStmt(frequency: StatementFrequency = StatementFrequency.YEARLY): YFinanceResult<IncomeStatement> =
        client.getIncomeStatement(symbol, frequency)

    /**
     * Get quarterly income statement
     */
    suspend fun quarterlyIncomeStmt(): YFinanceResult<IncomeStatement> =
        incomeStmt(StatementFrequency.QUARTERLY)

    /**
     * Get trailing twelve months income statement
     */
    suspend fun ttmIncomeStmt(): YFinanceResult<IncomeStatement> =
        incomeStmt(StatementFrequency.TTM)

    /**
     * Alias for incomeStmt
     */
    suspend fun financials(frequency: StatementFrequency = StatementFrequency.YEARLY): YFinanceResult<IncomeStatement> =
        incomeStmt(frequency)

    /**
     * Get quarterly financials (alias)
     */
    suspend fun quarterlyFinancials(): YFinanceResult<IncomeStatement> =
        quarterlyIncomeStmt()

    /**
     * Get balance sheet
     */
    suspend fun balanceSheet(frequency: StatementFrequency = StatementFrequency.YEARLY): YFinanceResult<BalanceSheet> =
        client.getBalanceSheet(symbol, frequency)

    /**
     * Get quarterly balance sheet
     */
    suspend fun quarterlyBalanceSheet(): YFinanceResult<BalanceSheet> =
        balanceSheet(StatementFrequency.QUARTERLY)

    /**
     * Get cash flow statement
     */
    suspend fun cashFlow(frequency: StatementFrequency = StatementFrequency.YEARLY): YFinanceResult<CashFlow> =
        client.getCashFlow(symbol, frequency)

    /**
     * Get quarterly cash flow statement
     */
    suspend fun quarterlyCashFlow(): YFinanceResult<CashFlow> =
        cashFlow(StatementFrequency.QUARTERLY)

    /**
     * Get TTM cash flow statement
     */
    suspend fun ttmCashFlow(): YFinanceResult<CashFlow> =
        cashFlow(StatementFrequency.TTM)

    // ==================== Earnings ====================

    /**
     * Get earnings data
     */
    suspend fun earnings(frequency: StatementFrequency = StatementFrequency.YEARLY): YFinanceResult<EarningsData> =
        client.getEarnings(symbol, frequency)

    /**
     * Get quarterly earnings
     */
    suspend fun quarterlyEarnings(): YFinanceResult<EarningsData> =
        earnings(StatementFrequency.QUARTERLY)

    /**
     * Get earnings dates
     */
    suspend fun earningsDates(limit: Int = 12): YFinanceResult<EarningsDatesData> =
        client.getEarningsDates(symbol, limit)

    /**
     * Get earnings calendar
     */
    suspend fun calendar(): YFinanceResult<Calendar> = client.getCalendar(symbol)

    // ==================== Holders ====================

    /**
     * Get major holders breakdown
     */
    suspend fun majorHolders(): YFinanceResult<MajorHoldersData> =
        client.getMajorHolders(symbol)

    /**
     * Get institutional holders
     */
    suspend fun institutionalHolders(): YFinanceResult<InstitutionalHoldersData> =
        client.getInstitutionalHolders(symbol)

    /**
     * Get mutual fund holders
     */
    suspend fun mutualFundHolders(): YFinanceResult<MutualFundHoldersData> =
        client.getMutualFundHolders(symbol)

    /**
     * Get insider transactions
     */
    suspend fun insiderTransactions(): YFinanceResult<InsiderTransactionsData> =
        client.getInsiderTransactions(symbol)

    /**
     * Get insider purchases
     */
    suspend fun insiderPurchases(): YFinanceResult<InsiderTransactionsData> {
        return insiderTransactions().map { data ->
            data.copy(transactions = data.getPurchases())
        }
    }

    /**
     * Get insider roster holders
     */
    suspend fun insiderRosterHolders(): YFinanceResult<InsiderRosterHoldersData> =
        client.getInsiderRosterHolders(symbol)

    // ==================== Analysis ====================

    /**
     * Get analyst recommendations
     */
    suspend fun recommendations(): YFinanceResult<RecommendationsData> =
        client.getRecommendations(symbol)

    /**
     * Get recommendations summary
     */
    suspend fun recommendationsSummary(): YFinanceResult<RecommendationsSummaryData> =
        client.getRecommendationsSummary(symbol)

    /**
     * Get upgrades and downgrades
     */
    suspend fun upgradesDowngrades(): YFinanceResult<RecommendationsData> =
        recommendations()

    /**
     * Get analyst price targets
     */
    suspend fun analystPriceTargets(): YFinanceResult<AnalystPriceTargets> =
        client.getAnalystPriceTargets(symbol)

    // ==================== Options ====================

    /**
     * Get available option expiration dates
     */
    suspend fun options(): YFinanceResult<OptionsExpirations> =
        client.getOptionsExpirations(symbol)

    /**
     * Get option chain for a specific expiration date
     *
     * @param expirationDate Expiration date in epoch seconds (null for nearest expiration)
     */
    suspend fun optionChain(expirationDate: Long? = null): YFinanceResult<OptionChain> =
        client.getOptionChain(symbol, expirationDate)

    // ==================== News & ESG ====================

    /**
     * Get news articles
     *
     * @param count Number of articles to fetch (default: 10)
     */
    suspend fun news(count: Int = 10): YFinanceResult<NewsData> =
        client.getNews(symbol, count)

    /**
     * Get sustainability/ESG scores
     */
    suspend fun sustainability(): YFinanceResult<Sustainability> =
        client.getSustainability(symbol)

    /**
     * Get SEC filings
     */
    suspend fun secFilings(): YFinanceResult<SECFilings> =
        client.getSECFilings(symbol)

    // ==================== Shares ====================

    /**
     * Get shares outstanding over time
     */
    suspend fun shares(): YFinanceResult<SharesData> =
        client.getShares(symbol)

    /**
     * Get detailed shares data for a specific date range
     */
    suspend fun sharesFull(
        startTimestamp: Long? = null,
        endTimestamp: Long? = null
    ): YFinanceResult<SharesData> =
        client.getSharesFull(symbol, startTimestamp, endTimestamp)
}

/**
 * Create an extended ticker instance
 */
fun extendedTicker(
    symbol: String,
    enableLogging: Boolean = false
): TickerExtended {
    return TickerExtended(symbol, YFinanceClient(enableLogging))
}

/**
 * DSL style for extended ticker
 */
suspend inline fun <T> extendedTicker(
    symbol: String,
    enableLogging: Boolean = false,
    block: suspend TickerExtended.() -> YFinanceResult<T>
): YFinanceResult<T> {
    val ticker = TickerExtended(symbol, YFinanceClient(enableLogging))
    return block(ticker)
}
