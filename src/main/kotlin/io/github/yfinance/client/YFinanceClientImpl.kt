package io.github.yfinance.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.yfinance.model.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

private val logger = KotlinLogging.logger {}

/**
 * Implementation of all Yahoo Finance API endpoints
 * These methods make actual HTTP calls to Yahoo Finance API
 */

private const val BASE_URL = "https://query2.finance.yahoo.com"
private const val QUOTE_SUMMARY_URL = "$BASE_URL/v10/finance/quoteSummary"
private const val OPTIONS_URL = "$BASE_URL/v7/finance/options"
private const val NEWS_URL = "$BASE_URL/v1/finance/search"

/**
 * Get financial statements (income statement, balance sheet, cash flow)
 */
suspend fun YFinanceClient.getFinancialStatements(
    symbol: String,
    modules: List<String>
): YFinanceResult<JsonObject> {
    return try {
        val url = "$QUOTE_SUMMARY_URL/$symbol?modules=${modules.joinToString(",")}"
        logger.debug { "Fetching financial statements: $url" }

        val response: HttpResponse = getHttpClient().get(url)

        if (!response.status.isSuccess()) {
            return YFinanceResult.Error(
                "HTTP ${response.status.value}: ${response.status.description}",
                errorType = YFinanceResult.Error.ErrorType.API_ERROR
            )
        }

        val jsonResponse: JsonObject = response.body()

        val quoteSummary = jsonResponse["quoteSummary"]?.jsonObject
        val result = quoteSummary?.get("result")?.jsonArray?.firstOrNull()?.jsonObject

        if (result == null) {
            val error = quoteSummary?.get("error")?.jsonObject
            return YFinanceResult.Error(
                error?.get("description")?.toString() ?: "No data available",
                errorType = YFinanceResult.Error.ErrorType.API_ERROR
            )
        }

        YFinanceResult.Success(result)

    } catch (e: Exception) {
        logger.error(e) { "Error fetching financial statements for $symbol" }
        YFinanceResult.Error(
            "Failed to fetch financial statements: ${e.message}",
            cause = e,
            errorType = YFinanceResult.Error.ErrorType.UNKNOWN
        )
    }
}

/**
 * Get income statement
 */
suspend fun YFinanceClient.getIncomeStatement(
    symbol: String,
    frequency: StatementFrequency
): YFinanceResult<IncomeStatement> {
    val module = when (frequency) {
        StatementFrequency.YEARLY -> "incomeStatementHistory"
        StatementFrequency.QUARTERLY -> "incomeStatementHistoryQuarterly"
        StatementFrequency.TTM -> "incomeStatementHistory" // TTM uses yearly endpoint
    }

    return getFinancialStatements(symbol, listOf(module)).map { jsonData ->
        parseIncomeStatement(symbol, jsonData, module)
    }
}

/**
 * Get balance sheet
 */
suspend fun YFinanceClient.getBalanceSheet(
    symbol: String,
    frequency: StatementFrequency
): YFinanceResult<BalanceSheet> {
    val module = when (frequency) {
        StatementFrequency.YEARLY -> "balanceSheetHistory"
        StatementFrequency.QUARTERLY -> "balanceSheetHistoryQuarterly"
        StatementFrequency.TTM -> "balanceSheetHistory"
    }

    return getFinancialStatements(symbol, listOf(module)).map { jsonData ->
        parseBalanceSheet(symbol, jsonData, module)
    }
}

/**
 * Get cash flow statement
 */
suspend fun YFinanceClient.getCashFlow(
    symbol: String,
    frequency: StatementFrequency
): YFinanceResult<CashFlow> {
    val module = when (frequency) {
        StatementFrequency.YEARLY -> "cashflowStatementHistory"
        StatementFrequency.QUARTERLY -> "cashflowStatementHistoryQuarterly"
        StatementFrequency.TTM -> "cashflowStatementHistory"
    }

    return getFinancialStatements(symbol, listOf(module)).map { jsonData ->
        parseCashFlow(symbol, jsonData, module)
    }
}

/**
 * Get earnings data
 */
suspend fun YFinanceClient.getEarnings(
    symbol: String,
    frequency: StatementFrequency
): YFinanceResult<EarningsData> {
    return getFinancialStatements(symbol, listOf("earnings", "earningsChart")).map { jsonData ->
        parseEarnings(symbol, jsonData, frequency)
    }
}

/**
 * Get calendar events
 */
suspend fun YFinanceClient.getCalendar(symbol: String): YFinanceResult<Calendar> {
    return getFinancialStatements(symbol, listOf("calendarEvents")).map { jsonData ->
        parseCalendar(symbol, jsonData)
    }
}

/**
 * Get earnings dates
 */
suspend fun YFinanceClient.getEarningsDates(
    symbol: String,
    limit: Int
): YFinanceResult<EarningsDatesData> {
    return getFinancialStatements(symbol, listOf("calendarEvents")).map { jsonData ->
        parseEarningsDates(symbol, jsonData, limit)
    }
}

/**
 * Get major holders
 */
suspend fun YFinanceClient.getMajorHolders(symbol: String): YFinanceResult<MajorHoldersData> {
    return getFinancialStatements(symbol, listOf("majorHoldersBreakdown")).map { jsonData ->
        parseMajorHolders(symbol, jsonData)
    }
}

/**
 * Get institutional holders
 */
suspend fun YFinanceClient.getInstitutionalHolders(symbol: String): YFinanceResult<InstitutionalHoldersData> {
    return getFinancialStatements(symbol, listOf("institutionOwnership")).map { jsonData ->
        parseInstitutionalHolders(symbol, jsonData)
    }
}

/**
 * Get mutual fund holders
 */
suspend fun YFinanceClient.getMutualFundHolders(symbol: String): YFinanceResult<MutualFundHoldersData> {
    return getFinancialStatements(symbol, listOf("fundOwnership")).map { jsonData ->
        parseMutualFundHolders(symbol, jsonData)
    }
}

/**
 * Get insider transactions
 */
suspend fun YFinanceClient.getInsiderTransactions(symbol: String): YFinanceResult<InsiderTransactionsData> {
    return getFinancialStatements(symbol, listOf("insiderTransactions")).map { jsonData ->
        parseInsiderTransactions(symbol, jsonData)
    }
}

/**
 * Get insider roster holders
 */
suspend fun YFinanceClient.getInsiderRosterHolders(symbol: String): YFinanceResult<InsiderRosterHoldersData> {
    return getFinancialStatements(symbol, listOf("insiderHolders")).map { jsonData ->
        parseInsiderRosterHolders(symbol, jsonData)
    }
}

/**
 * Get recommendations
 */
suspend fun YFinanceClient.getRecommendations(symbol: String): YFinanceResult<RecommendationsData> {
    return getFinancialStatements(symbol, listOf("upgradeDowngradeHistory")).map { jsonData ->
        parseRecommendations(symbol, jsonData)
    }
}

/**
 * Get recommendations summary
 */
suspend fun YFinanceClient.getRecommendationsSummary(symbol: String): YFinanceResult<RecommendationsSummaryData> {
    return getFinancialStatements(symbol, listOf("recommendationTrend")).map { jsonData ->
        parseRecommendationsSummary(symbol, jsonData)
    }
}

/**
 * Get analyst price targets
 */
suspend fun YFinanceClient.getAnalystPriceTargets(symbol: String): YFinanceResult<AnalystPriceTargets> {
    return getFinancialStatements(symbol, listOf("financialData")).map { jsonData ->
        parseAnalystPriceTargets(symbol, jsonData)
    }
}

/**
 * Get sustainability/ESG scores
 */
suspend fun YFinanceClient.getSustainability(symbol: String): YFinanceResult<Sustainability> {
    return getFinancialStatements(symbol, listOf("esgScores")).map { jsonData ->
        parseSustainability(symbol, jsonData)
    }
}

/**
 * Get SEC filings
 */
suspend fun YFinanceClient.getSECFilings(symbol: String): YFinanceResult<SECFilings> {
    return getFinancialStatements(symbol, listOf("secFilings")).map { jsonData ->
        parseSECFilings(symbol, jsonData)
    }
}

/**
 * Get options expirations
 */
suspend fun YFinanceClient.getOptionsExpirations(symbol: String): YFinanceResult<OptionsExpirations> {
    return try {
        val url = "$OPTIONS_URL/$symbol"
        logger.debug { "Fetching options expirations: $url" }

        val response: HttpResponse = getHttpClient().get(url)

        if (!response.status.isSuccess()) {
            return YFinanceResult.Error(
                "HTTP ${response.status.value}",
                errorType = YFinanceResult.Error.ErrorType.API_ERROR
            )
        }

        val jsonResponse: JsonObject = response.body()
        val optionChain = jsonResponse["optionChain"]?.jsonObject
        val result = optionChain?.get("result")?.jsonArray?.firstOrNull()?.jsonObject

        if (result == null) {
            return YFinanceResult.Error("No options data available")
        }

        val expirations = result["expirationDates"]?.jsonArray?.map {
            it.jsonPrimitive.long
        } ?: emptyList()

        YFinanceResult.Success(OptionsExpirations(symbol, expirations))

    } catch (e: Exception) {
        logger.error(e) { "Error fetching options expirations for $symbol" }
        YFinanceResult.Error(
            "Failed to fetch options: ${e.message}",
            cause = e
        )
    }
}

/**
 * Get option chain for specific expiration
 */
suspend fun YFinanceClient.getOptionChain(
    symbol: String,
    expirationDate: Long?
): YFinanceResult<OptionChain> {
    return try {
        val url = if (expirationDate != null) {
            "$OPTIONS_URL/$symbol?date=$expirationDate"
        } else {
            "$OPTIONS_URL/$symbol"
        }

        logger.debug { "Fetching option chain: $url" }

        val response: HttpResponse = getHttpClient().get(url)

        if (!response.status.isSuccess()) {
            return YFinanceResult.Error("HTTP ${response.status.value}")
        }

        val jsonResponse: JsonObject = response.body()
        parseOptionChain(jsonResponse)

    } catch (e: Exception) {
        logger.error(e) { "Error fetching option chain for $symbol" }
        YFinanceResult.Error(
            "Failed to fetch option chain: ${e.message}",
            cause = e
        )
    }
}

/**
 * Get news articles
 */
suspend fun YFinanceClient.getNews(
    symbol: String,
    count: Int
): YFinanceResult<NewsData> {
    return try {
        val url = "$BASE_URL/v1/finance/search?q=$symbol&quotesCount=0&newsCount=$count"
        logger.debug { "Fetching news: $url" }

        val response: HttpResponse = getHttpClient().get(url)

        if (!response.status.isSuccess()) {
            return YFinanceResult.Error("HTTP ${response.status.value}")
        }

        val jsonResponse: JsonObject = response.body()
        parseNews(symbol, jsonResponse)

    } catch (e: Exception) {
        logger.error(e) { "Error fetching news for $symbol" }
        YFinanceResult.Error(
            "Failed to fetch news: ${e.message}",
            cause = e
        )
    }
}

/**
 * Get shares outstanding
 */
suspend fun YFinanceClient.getShares(symbol: String): YFinanceResult<SharesData> {
    return getFinancialStatements(symbol, listOf("defaultKeyStatistics")).map { jsonData ->
        parseShares(symbol, jsonData)
    }
}

/**
 * Get detailed shares data
 */
suspend fun YFinanceClient.getSharesFull(
    symbol: String,
    startTimestamp: Long?,
    endTimestamp: Long?
): YFinanceResult<SharesData> {
    // This would require the shares history endpoint which may need special handling
    return getShares(symbol)
}

/**
 * Get ISIN
 */
suspend fun YFinanceClient.getISIN(symbol: String): YFinanceResult<String> {
    return getFinancialStatements(symbol, listOf("summaryDetail")).map { jsonData ->
        val summaryDetail = jsonData["summaryDetail"]?.jsonObject
        summaryDetail?.get("isin")?.jsonPrimitive?.content
            ?: throw Exception("ISIN not found")
    }
}

/**
 * Get fast info by reusing existing ticker info
 */
suspend fun YFinanceClient.getFastInfo(symbol: String): YFinanceResult<FastInfo> {
    return getTickerInfo(symbol).map { info ->
        FastInfo(
            symbol = symbol,
            lastPrice = info.previousClose,
            previousClose = info.previousClose,
            open = info.open,
            dayHigh = info.dayHigh,
            dayLow = info.dayLow,
            yearHigh = info.fiftyTwoWeekHigh,
            yearLow = info.fiftyTwoWeekLow,
            currency = info.currency,
            marketCap = info.marketCap
        )
    }
}

/**
 * Get all corporate actions (already implemented)
 */
suspend fun YFinanceClient.getActions(
    symbol: String,
    period: Period = Period.MAX
): YFinanceResult<ActionsData> {
    val dividendsResult = getDividends(symbol, period)
    val splitsResult = getSplits(symbol, period)

    return when {
        dividendsResult is YFinanceResult.Success && splitsResult is YFinanceResult.Success -> {
            val actions = mutableListOf<Action>()

            dividendsResult.data.dividends.forEach { dividend ->
                actions.add(Action(
                    date = dividend.date,
                    dividends = dividend.amount
                ))
            }

            splitsResult.data.splits.forEach { split ->
                actions.add(Action(
                    date = split.date,
                    splits = split.ratio
                ))
            }

            YFinanceResult.Success(ActionsData(symbol, actions.sortedBy { it.date }))
        }
        dividendsResult is YFinanceResult.Error -> dividendsResult
        splitsResult is YFinanceResult.Error -> splitsResult
        else -> YFinanceResult.Error("Failed to fetch actions")
    }
}

/**
 * Get capital gains (primarily for funds)
 */
suspend fun YFinanceClient.getCapitalGains(
    symbol: String,
    period: Period = Period.MAX
): YFinanceResult<CapitalGainsData> {
    // Capital gains are included in the chart API events
    return try {
        val histResult = getHistoricalData(symbol, period, Interval.ONE_DAY, includeEvents = true)

        // For now, return empty as capital gains need special handling
        YFinanceResult.Success(CapitalGainsData(symbol, emptyList()))
    } catch (e: Exception) {
        YFinanceResult.Error("Failed to fetch capital gains: ${e.message}", cause = e)
    }
}

// Helper function to access HTTP client
internal fun YFinanceClient.getHttpClient() = this.client
