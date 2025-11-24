package io.github.yfinance.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.yfinance.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

/**
 * Client for interacting with Yahoo Finance API
 *
 * @property enableLogging Enable HTTP request/response logging
 * @property timeout Request timeout in milliseconds
 */
class YFinanceClient(
    private val enableLogging: Boolean = false,
    private val timeout: Long = 30_000
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = timeout
            connectTimeoutMillis = timeout
            socketTimeoutMillis = timeout
        }

        if (enableLogging) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
        }

        defaultRequest {
            header(HttpHeaders.UserAgent, "Mozilla/5.0")
        }
    }

    /**
     * Fetch historical data for a symbol
     *
     * @param symbol The ticker symbol
     * @param period The time period
     * @param interval The data interval
     * @param includeEvents Include dividend and split events
     * @return Result containing historical data
     */
    suspend fun getHistoricalData(
        symbol: String,
        period: Period = Period.ONE_MONTH,
        interval: Interval = Interval.ONE_DAY,
        includeEvents: Boolean = true
    ): YFinanceResult<HistoricalData> {
        return try {
            val url = buildChartUrl(symbol, period, interval, includeEvents)
            logger.debug { "Fetching historical data: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val chartResponse: ChartResponse = response.body()

            if (chartResponse.chart.error != null) {
                return YFinanceResult.Error(
                    chartResponse.chart.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = chartResponse.chart.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            YFinanceResult.Success(parseHistoricalData(result))

        } catch (e: Exception) {
            logger.error(e) { "Error fetching historical data for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch historical data: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch ticker information
     *
     * @param symbol The ticker symbol
     * @return Result containing ticker information
     */
    suspend fun getTickerInfo(symbol: String): YFinanceResult<TickerInfo> {
        return try {
            val url = buildQuoteSummaryUrl(symbol)
            logger.debug { "Fetching ticker info: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            YFinanceResult.Success(parseTickerInfo(symbol, result))

        } catch (e: Exception) {
            logger.error(e) { "Error fetching ticker info for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch ticker info: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch dividend data for a symbol
     *
     * @param symbol The ticker symbol
     * @param period The time period
     * @return Result containing dividend data
     */
    suspend fun getDividends(
        symbol: String,
        period: Period = Period.MAX
    ): YFinanceResult<DividendData> {
        return try {
            val url = buildChartUrl(symbol, period, Interval.ONE_DAY, includeEvents = true)
            logger.debug { "Fetching dividends: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val chartResponse: ChartResponse = response.body()

            if (chartResponse.chart.error != null) {
                return YFinanceResult.Error(
                    chartResponse.chart.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = chartResponse.chart.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            YFinanceResult.Success(parseDividendData(symbol, result))

        } catch (e: Exception) {
            logger.error(e) { "Error fetching dividends for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch dividends: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch stock split data for a symbol
     *
     * @param symbol The ticker symbol
     * @param period The time period
     * @return Result containing split data
     */
    suspend fun getSplits(
        symbol: String,
        period: Period = Period.MAX
    ): YFinanceResult<SplitData> {
        return try {
            val url = buildChartUrl(symbol, period, Interval.ONE_DAY, includeEvents = true)
            logger.debug { "Fetching splits: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val chartResponse: ChartResponse = response.body()

            if (chartResponse.chart.error != null) {
                return YFinanceResult.Error(
                    chartResponse.chart.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = chartResponse.chart.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            YFinanceResult.Success(parseSplitData(symbol, result))

        } catch (e: Exception) {
            logger.error(e) { "Error fetching splits for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch splits: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch all corporate actions (dividends and splits) for a symbol
     *
     * @param symbol The ticker symbol
     * @param period The time period
     * @return Result containing action data
     */
    suspend fun getActions(
        symbol: String,
        period: Period = Period.MAX
    ): YFinanceResult<ActionData> {
        return try {
            val url = buildChartUrl(symbol, period, Interval.ONE_DAY, includeEvents = true)
            logger.debug { "Fetching actions: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val chartResponse: ChartResponse = response.body()

            if (chartResponse.chart.error != null) {
                return YFinanceResult.Error(
                    chartResponse.chart.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = chartResponse.chart.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val dividends = result.events?.dividends?.values?.map { event ->
                Dividend(
                    date = event.date,
                    amount = event.amount
                )
            } ?: emptyList()

            val splits = result.events?.splits?.values?.map { event ->
                Split(
                    date = event.date,
                    ratio = event.numerator / event.denominator
                )
            } ?: emptyList()

            YFinanceResult.Success(
                ActionData(
                    symbol = symbol,
                    dividends = dividends,
                    splits = splits
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching actions for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch actions: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    private fun buildChartUrl(
        symbol: String,
        period: Period,
        interval: Interval,
        includeEvents: Boolean
    ): String {
        val events = if (includeEvents) "div,splits" else ""
        return "https://query2.finance.yahoo.com/v8/finance/chart/$symbol" +
                "?range=${period.value}" +
                "&interval=${interval.value}" +
                "&events=$events" +
                "&includeAdjustedClose=true"
    }

    private fun buildQuoteSummaryUrl(symbol: String, modules: List<String>? = null): String {
        val defaultModules = modules ?: listOf(
            "assetProfile",
            "price",
            "summaryDetail",
            "defaultKeyStatistics",
            "financialData",
            "calendarEvents"
        )

        return "https://query2.finance.yahoo.com/v10/finance/quoteSummary/$symbol" +
                "?modules=${defaultModules.joinToString(",")}"
    }

    private fun buildFinancialsUrl(symbol: String, modules: List<String>): String {
        return buildQuoteSummaryUrl(symbol, modules)
    }

    private fun parseHistoricalData(result: ChartResult): HistoricalData {
        val timestamps = result.timestamp ?: emptyList()
        val quoteData = result.indicators.quote?.firstOrNull()
        val adjCloseData = result.indicators.adjclose?.firstOrNull()

        val quotes = timestamps.mapIndexedNotNull { index, timestamp ->
            Quote(
                timestamp = timestamp,
                open = quoteData?.open?.getOrNull(index),
                high = quoteData?.high?.getOrNull(index),
                low = quoteData?.low?.getOrNull(index),
                close = quoteData?.close?.getOrNull(index),
                adjClose = adjCloseData?.adjclose?.getOrNull(index),
                volume = quoteData?.volume?.getOrNull(index)
            )
        }

        return HistoricalData(
            symbol = result.meta.symbol,
            quotes = quotes,
            currency = result.meta.currency
        )
    }

    private fun parseDividendData(symbol: String, result: ChartResult): DividendData {
        val dividends = result.events?.dividends?.values?.map { event ->
            Dividend(
                date = event.date,
                amount = event.amount
            )
        } ?: emptyList()

        return DividendData(symbol = symbol, dividends = dividends)
    }

    private fun parseSplitData(symbol: String, result: ChartResult): SplitData {
        val splits = result.events?.splits?.values?.map { event ->
            Split(
                date = event.date,
                ratio = event.numerator / event.denominator
            )
        } ?: emptyList()

        return SplitData(symbol = symbol, splits = splits)
    }

    /**
     * Fetch calendar events for a symbol
     *
     * @param symbol The ticker symbol
     * @return Result containing calendar data
     */
    suspend fun getCalendar(symbol: String): YFinanceResult<CalendarData> {
        return try {
            val url = buildQuoteSummaryUrl(symbol)
            logger.debug { "Fetching calendar: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val calendar = result.calendarEvents
            val earningsDate = calendar?.earnings?.earningsDate?.firstOrNull()?.raw?.toLong()
            val exDivDate = calendar?.exDividendDate?.raw?.toLong()
            val divDate = calendar?.dividendDate?.raw?.toLong()

            YFinanceResult.Success(
                CalendarData(
                    symbol = symbol,
                    earnings = earningsDate,
                    exDividendDate = exDivDate,
                    dividendDate = divDate
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching calendar for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch calendar: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    private fun parseTickerInfo(symbol: String, result: QuoteSummaryResult): TickerInfo {
        val profile = result.assetProfile
        val price = result.price
        val summary = result.summaryDetail
        val keyStats = result.defaultKeyStatistics
        val financial = result.financialData

        val ceo = profile?.companyOfficers
            ?.firstOrNull { it.title?.contains("CEO", ignoreCase = true) == true }
            ?.name

        return TickerInfo(
            symbol = symbol,
            shortName = price?.shortName?.raw?.toString(),
            longName = price?.longName?.raw?.toString(),
            currency = price?.currency,
            exchange = price?.exchange,
            quoteType = price?.quoteType,
            marketCap = price?.marketCap?.raw?.toLong(),
            sector = profile?.sector,
            industry = profile?.industry,
            website = profile?.website,
            description = profile?.longBusinessSummary,
            employees = profile?.fullTimeEmployees,
            ceo = ceo,
            city = profile?.city,
            state = profile?.state,
            country = profile?.country,
            phone = profile?.phone,
            previousClose = summary?.previousClose?.raw,
            open = summary?.open?.raw,
            dayLow = summary?.dayLow?.raw,
            dayHigh = summary?.dayHigh?.raw,
            regularMarketVolume = summary?.regularMarketVolume?.raw?.toLong(),
            averageVolume = summary?.averageVolume?.raw?.toLong(),
            fiftyTwoWeekLow = summary?.fiftyTwoWeekLow?.raw,
            fiftyTwoWeekHigh = summary?.fiftyTwoWeekHigh?.raw,
            dividendRate = summary?.dividendRate?.raw,
            dividendYield = summary?.dividendYield?.raw,
            exDividendDate = summary?.exDividendDate?.raw?.toLong(),
            beta = summary?.beta?.raw,
            trailingPE = summary?.trailingPE?.raw,
            forwardPE = summary?.forwardPE?.raw ?: keyStats?.forwardPE?.raw,
            bookValue = keyStats?.bookValue?.raw,
            priceToBook = keyStats?.priceToBook?.raw,
            earningsPerShare = keyStats?.trailingEps?.raw,
            revenuePerShare = financial?.revenuePerShare?.raw,
            returnOnAssets = financial?.returnOnAssets?.raw,
            returnOnEquity = financial?.returnOnEquity?.raw,
            freeCashflow = financial?.freeCashflow?.raw?.toLong(),
            operatingCashflow = financial?.operatingCashflow?.raw?.toLong(),
            revenueGrowth = financial?.revenueGrowth?.raw,
            earningsGrowth = financial?.earningsGrowth?.raw
        )
    }

    /**
     * Fetch income statement (financials)
     *
     * @param symbol The ticker symbol
     * @param frequency The frequency (annual, quarterly, or trailing)
     * @return Result containing financial statement
     */
    suspend fun getIncomeStatement(
        symbol: String,
        frequency: Frequency = Frequency.ANNUAL
    ): YFinanceResult<FinancialStatement> {
        return getFinancialStatement(symbol, "incomeStatement", frequency)
    }

    /**
     * Fetch balance sheet
     *
     * @param symbol The ticker symbol
     * @param frequency The frequency (annual, quarterly, or trailing)
     * @return Result containing financial statement
     */
    suspend fun getBalanceSheet(
        symbol: String,
        frequency: Frequency = Frequency.ANNUAL
    ): YFinanceResult<FinancialStatement> {
        return getFinancialStatement(symbol, "balanceSheet", frequency)
    }

    /**
     * Fetch cash flow statement
     *
     * @param symbol The ticker symbol
     * @param frequency The frequency (annual, quarterly, or trailing)
     * @return Result containing financial statement
     */
    suspend fun getCashFlow(
        symbol: String,
        frequency: Frequency = Frequency.ANNUAL
    ): YFinanceResult<FinancialStatement> {
        return getFinancialStatement(symbol, "cashflowStatement", frequency)
    }

    private suspend fun getFinancialStatement(
        symbol: String,
        statementType: String,
        frequency: Frequency
    ): YFinanceResult<FinancialStatement> {
        return try {
            val moduleKey = when (frequency) {
                Frequency.ANNUAL -> "${statementType}History"
                Frequency.QUARTERLY -> "${statementType}HistoryQuarterly"
                Frequency.TRAILING -> "${statementType}History"  // Use annual for trailing
            }

            val url = buildFinancialsUrl(symbol, listOf(moduleKey))
            logger.debug { "Fetching financial statement: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            YFinanceResult.Success(parseFinancialStatement(symbol, result, statementType, frequency))

        } catch (e: Exception) {
            logger.error(e) { "Error fetching financial statement for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch financial statement: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    private fun parseFinancialStatement(
        symbol: String,
        result: QuoteSummaryResult,
        statementType: String,
        frequency: Frequency
    ): FinancialStatement {
        val statements = when (statementType) {
            "incomeStatement" -> {
                if (frequency == Frequency.QUARTERLY) {
                    result.incomeStatementHistoryQuarterly?.incomeStatementHistory
                } else {
                    result.incomeStatementHistory?.incomeStatementHistory
                }
            }
            "balanceSheet" -> {
                if (frequency == Frequency.QUARTERLY) {
                    result.balanceSheetHistoryQuarterly?.balanceSheetStatements
                } else {
                    result.balanceSheetHistory?.balanceSheetStatements
                }
            }
            "cashflowStatement" -> {
                if (frequency == Frequency.QUARTERLY) {
                    result.cashflowStatementHistoryQuarterly?.cashflowStatements
                } else {
                    result.cashflowStatementHistory?.cashflowStatements
                }
            }
            else -> emptyList()
        } ?: emptyList()

        val data = statements.associate { entry ->
            val date = entry.endDate?.fmt ?: entry.endDate?.raw?.toLong()?.toString() ?: "Unknown"
            val lineItems = mutableMapOf<String, Long>()

            // Add all non-null items
            entry.totalRevenue?.raw?.let { lineItems["totalRevenue"] = it }
            entry.costOfRevenue?.raw?.let { lineItems["costOfRevenue"] = it }
            entry.grossProfit?.raw?.let { lineItems["grossProfit"] = it }
            entry.operatingExpense?.raw?.let { lineItems["operatingExpense"] = it }
            entry.operatingIncome?.raw?.let { lineItems["operatingIncome"] = it }
            entry.netIncome?.raw?.let { lineItems["netIncome"] = it }
            entry.ebitda?.raw?.let { lineItems["ebitda"] = it }
            entry.totalAssets?.raw?.let { lineItems["totalAssets"] = it }
            entry.totalLiabilitiesNetMinorityInterest?.raw?.let { lineItems["totalLiabilities"] = it }
            entry.stockholdersEquity?.raw?.let { lineItems["stockholdersEquity"] = it }
            entry.totalDebt?.raw?.let { lineItems["totalDebt"] = it }
            entry.currentAssets?.raw?.let { lineItems["currentAssets"] = it }
            entry.currentLiabilities?.raw?.let { lineItems["currentLiabilities"] = it }
            entry.cashAndCashEquivalents?.raw?.let { lineItems["cash"] = it }
            entry.operatingCashFlow?.raw?.let { lineItems["operatingCashFlow"] = it }
            entry.investingCashFlow?.raw?.let { lineItems["investingCashFlow"] = it }
            entry.financingCashFlow?.raw?.let { lineItems["financingCashFlow"] = it }
            entry.freeCashFlow?.raw?.let { lineItems["freeCashFlow"] = it }
            entry.capitalExpenditure?.raw?.let { lineItems["capitalExpenditure"] = it }

            date to lineItems
        }

        return FinancialStatement(
            symbol = symbol,
            data = data
        )
    }

    /**
     * Fetch news articles
     *
     * @param symbol The ticker symbol
     * @return Result containing news data
     */
    suspend fun getNews(symbol: String): YFinanceResult<NewsData> {
        return try {
            val url = "https://query2.finance.yahoo.com/v1/finance/search?q=$symbol"
            logger.debug { "Fetching news: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            // Parse news from search results
            val articles = emptyList<NewsArticle>()  // Placeholder - actual implementation would parse response

            YFinanceResult.Success(
                NewsData(
                    symbol = symbol,
                    articles = articles
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching news for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch news: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch recommendations
     *
     * @param symbol The ticker symbol
     * @return Result containing recommendation data
     */
    suspend fun getRecommendations(symbol: String): YFinanceResult<RecommendationData> {
        return try {
            val url = buildFinancialsUrl(symbol, listOf("upgradeDowngradeHistory"))
            logger.debug { "Fetching recommendations: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val history = result.upgradeDowngradeHistory?.history ?: emptyList()
            val recommendations = history.map { item ->
                Recommendation(
                    date = item.epochGradeDate,
                    firm = item.firm,
                    toGrade = item.toGrade,
                    fromGrade = item.fromGrade,
                    action = item.action
                )
            }

            YFinanceResult.Success(
                RecommendationData(
                    symbol = symbol,
                    recommendations = recommendations
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching recommendations for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch recommendations: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch major holders
     *
     * @param symbol The ticker symbol
     * @return Result containing major holders data
     */
    suspend fun getMajorHolders(symbol: String): YFinanceResult<MajorHolders> {
        return try {
            val url = buildFinancialsUrl(symbol, listOf("majorHoldersBreakdown"))
            logger.debug { "Fetching major holders: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val breakdown = result.majorHoldersBreakdown

            YFinanceResult.Success(
                MajorHolders(
                    symbol = symbol,
                    insidersPercent = breakdown?.insidersPercentHeld?.raw,
                    institutionsPercent = breakdown?.institutionsPercentHeld?.raw,
                    institutionsFloatPercent = breakdown?.institutionsFloatPercentHeld?.raw,
                    institutionsCount = breakdown?.institutionsCount?.raw?.toLong()
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching major holders for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch major holders: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch institutional holders
     *
     * @param symbol The ticker symbol
     * @return Result containing institutional holders data
     */
    suspend fun getInstitutionalHolders(symbol: String): YFinanceResult<InstitutionalHoldersData> {
        return try {
            val url = buildFinancialsUrl(symbol, listOf("institutionOwnership"))
            logger.debug { "Fetching institutional holders: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val ownershipList = result.institutionOwnership?.ownershipList ?: emptyList()
            val holders = ownershipList.map { owner ->
                InstitutionalHolder(
                    organization = owner.organization,
                    percentHeld = owner.pctHeld?.raw,
                    shares = owner.position?.raw?.toLong(),
                    value = owner.value?.raw?.toLong(),
                    reportDate = owner.reportDate?.raw?.toLong()
                )
            }

            YFinanceResult.Success(
                InstitutionalHoldersData(
                    symbol = symbol,
                    holders = holders
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching institutional holders for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch institutional holders: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch earnings history
     *
     * @param symbol The ticker symbol
     * @return Result containing earnings history data
     */
    suspend fun getEarningsHistory(symbol: String): YFinanceResult<EarningsHistoryData> {
        return try {
            val url = buildFinancialsUrl(symbol, listOf("earningsHistory"))
            logger.debug { "Fetching earnings history: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val history = result.earningsHistory?.history ?: emptyList()
            val items = history.map { item ->
                EarningsHistoryItem(
                    quarter = item.quarter?.fmt ?: "",
                    epsActual = item.epsActual?.raw,
                    epsEstimate = item.epsEstimate?.raw,
                    epsDifference = item.epsDifference?.raw,
                    surprisePercent = item.surprisePercent?.raw
                )
            }

            YFinanceResult.Success(
                EarningsHistoryData(
                    symbol = symbol,
                    history = items
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching earnings history for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch earnings history: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch full earnings data
     *
     * @param symbol The ticker symbol
     * @return Result containing full earnings data
     */
    suspend fun getEarnings(symbol: String): YFinanceResult<FullEarningsData> {
        return try {
            val url = buildFinancialsUrl(symbol, listOf("earnings"))
            logger.debug { "Fetching earnings: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val earningsData = result.earnings
            val quarterlyEarnings = earningsData?.earningsChart?.quarterly?.map { q ->
                QuarterlyEarningsData(
                    date = q.date,
                    actual = q.actual?.raw,
                    estimate = q.estimate?.raw
                )
            } ?: emptyList()

            val yearlyRevenue = earningsData?.financialsChart?.yearly?.associate { y ->
                y.date to (y.revenue?.raw?.toLong() ?: 0L)
            } ?: emptyMap()

            val quarterlyRevenue = earningsData?.financialsChart?.quarterly?.associate { q ->
                q.date to (q.revenue?.raw?.toLong() ?: 0L)
            } ?: emptyMap()

            YFinanceResult.Success(
                FullEarningsData(
                    symbol = symbol,
                    quarterlyEarnings = quarterlyEarnings,
                    currentQuarterEstimate = earningsData?.earningsChart?.currentQuarterEstimate?.raw,
                    yearlyRevenue = yearlyRevenue,
                    quarterlyRevenue = quarterlyRevenue
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching earnings for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch earnings: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch available option expiration dates
     *
     * @param symbol The ticker symbol
     * @return Result containing list of expiration dates (epoch seconds)
     */
    suspend fun getOptions(symbol: String): YFinanceResult<List<Long>> {
        return try {
            val url = "https://query2.finance.yahoo.com/v7/finance/options/$symbol"
            logger.debug { "Fetching option expirations: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val optionsResponse: OptionsResponse = response.body()

            if (optionsResponse.optionChain.error != null) {
                return YFinanceResult.Error(
                    optionsResponse.optionChain.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = optionsResponse.optionChain.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No options data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            YFinanceResult.Success(result.expirationDates ?: emptyList())

        } catch (e: Exception) {
            logger.error(e) { "Error fetching options for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch options: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch option chain for specific expiration date
     *
     * @param symbol The ticker symbol
     * @param expiration Expiration date (epoch seconds)
     * @return Result containing option chain
     */
    suspend fun getOptionChain(symbol: String, expiration: Long): YFinanceResult<OptionChain> {
        return try {
            val url = "https://query2.finance.yahoo.com/v7/finance/options/$symbol?date=$expiration"
            logger.debug { "Fetching option chain: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val optionsResponse: OptionsResponse = response.body()

            if (optionsResponse.optionChain.error != null) {
                return YFinanceResult.Error(
                    optionsResponse.optionChain.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = optionsResponse.optionChain.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No options data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val optionData = result.options?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No option chain data for expiration: $expiration",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )

            val calls = optionData.calls?.map { contract ->
                io.github.yfinance.model.OptionContract(
                    contractSymbol = contract.contractSymbol,
                    strike = contract.strike,
                    currency = contract.currency,
                    lastPrice = contract.lastPrice,
                    change = contract.change,
                    percentChange = contract.percentChange,
                    volume = contract.volume,
                    openInterest = contract.openInterest,
                    bid = contract.bid,
                    ask = contract.ask,
                    contractSize = contract.contractSize,
                    expiration = contract.expiration,
                    lastTradeDate = contract.lastTradeDate,
                    impliedVolatility = contract.impliedVolatility,
                    inTheMoney = contract.inTheMoney
                )
            } ?: emptyList()

            val puts = optionData.puts?.map { contract ->
                io.github.yfinance.model.OptionContract(
                    contractSymbol = contract.contractSymbol,
                    strike = contract.strike,
                    currency = contract.currency,
                    lastPrice = contract.lastPrice,
                    change = contract.change,
                    percentChange = contract.percentChange,
                    volume = contract.volume,
                    openInterest = contract.openInterest,
                    bid = contract.bid,
                    ask = contract.ask,
                    contractSize = contract.contractSize,
                    expiration = contract.expiration,
                    lastTradeDate = contract.lastTradeDate,
                    impliedVolatility = contract.impliedVolatility,
                    inTheMoney = contract.inTheMoney
                )
            } ?: emptyList()

            YFinanceResult.Success(
                OptionChain(
                    symbol = symbol,
                    expirationDate = expiration,
                    calls = calls,
                    puts = puts
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching option chain for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch option chain: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch fast info (quick access to key data)
     *
     * @param symbol The ticker symbol
     * @return Result containing fast info
     */
    suspend fun getFastInfo(symbol: String): YFinanceResult<FastInfo> {
        return try {
            val url = buildFinancialsUrl(symbol, listOf("summaryDetail", "price", "defaultKeyStatistics"))
            logger.debug { "Fetching fast info: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val summary = result.summaryDetail
            val price = result.price
            val stats = result.defaultKeyStatistics

            YFinanceResult.Success(
                FastInfo(
                    symbol = symbol,
                    lastPrice = price?.regularMarketPrice?.raw,
                    open = summary?.open?.raw,
                    dayHigh = summary?.dayHigh?.raw,
                    dayLow = summary?.dayLow?.raw,
                    previousClose = summary?.previousClose?.raw,
                    marketCap = price?.marketCap?.raw?.toLong(),
                    volume = summary?.regularMarketVolume?.raw?.toLong(),
                    fiftyTwoWeekHigh = summary?.fiftyTwoWeekHigh?.raw,
                    fiftyTwoWeekLow = summary?.fiftyTwoWeekLow?.raw,
                    shares = stats?.sharesOutstanding?.raw?.toLong(),
                    currency = price?.currency
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching fast info for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch fast info: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch sustainability/ESG scores
     *
     * @param symbol The ticker symbol
     * @return Result containing sustainability data
     */
    suspend fun getSustainability(symbol: String): YFinanceResult<Sustainability> {
        return try {
            val url = buildFinancialsUrl(symbol, listOf("esgScores"))
            logger.debug { "Fetching sustainability: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val esg = result.esgScores

            YFinanceResult.Success(
                Sustainability(
                    symbol = symbol,
                    totalEsg = esg?.totalEsg?.raw,
                    environmentScore = esg?.environmentScore?.raw,
                    socialScore = esg?.socialScore?.raw,
                    governanceScore = esg?.governanceScore?.raw,
                    controversyLevel = esg?.highestControversy,
                    esgPerformance = esg?.esgPerformance,
                    percentile = esg?.percentile?.raw
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching sustainability for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch sustainability: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch capital gains distributions
     *
     * @param symbol The ticker symbol
     * @param period The time period
     * @return Result containing capital gains data
     */
    suspend fun getCapitalGains(
        symbol: String,
        period: Period = Period.MAX
    ): YFinanceResult<CapitalGainsData> {
        return try {
            val url = buildChartUrl(symbol, period, Interval.ONE_DAY, includeEvents = true)
                .replace("events=div,splits", "events=capitalGains")
            logger.debug { "Fetching capital gains: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val chartResponse: ChartResponse = response.body()

            if (chartResponse.chart.error != null) {
                return YFinanceResult.Error(
                    chartResponse.chart.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = chartResponse.chart.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            // Parse capital gains from events
            // Note: Yahoo Finance may not return capital gains in the standard events field
            // This is a best-effort implementation
            val gains = mutableListOf<CapitalGain>()

            YFinanceResult.Success(
                CapitalGainsData(
                    symbol = symbol,
                    gains = gains
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching capital gains for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch capital gains: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Fetch shares outstanding over time
     *
     * @param symbol The ticker symbol
     * @return Result containing shares data
     */
    suspend fun getShares(symbol: String): YFinanceResult<SharesData> {
        return try {
            // Yahoo Finance doesn't provide historical shares outstanding via API
            // We can only get the current value from defaultKeyStatistics
            val url = buildFinancialsUrl(symbol, listOf("defaultKeyStatistics"))
            logger.debug { "Fetching shares: $url" }

            val response: HttpResponse = client.get(url)

            if (!response.status.isSuccess()) {
                return YFinanceResult.Error(
                    "HTTP ${response.status.value}: ${response.status.description}",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val quoteSummaryResponse: QuoteSummaryResponse = response.body()

            if (quoteSummaryResponse.quoteSummary.error != null) {
                return YFinanceResult.Error(
                    quoteSummaryResponse.quoteSummary.error.description ?: "Unknown API error",
                    errorType = YFinanceResult.Error.ErrorType.API_ERROR
                )
            }

            val result = quoteSummaryResponse.quoteSummary.result?.firstOrNull()
                ?: return YFinanceResult.Error(
                    "No data returned for symbol: $symbol",
                    errorType = YFinanceResult.Error.ErrorType.INVALID_SYMBOL
                )

            val stats = result.defaultKeyStatistics
            val shares = stats?.sharesOutstanding?.raw?.toLong()

            val sharesMap = if (shares != null) {
                mapOf(System.currentTimeMillis() / 1000 to shares)
            } else {
                emptyMap()
            }

            YFinanceResult.Success(
                SharesData(
                    symbol = symbol,
                    data = sharesMap
                )
            )

        } catch (e: Exception) {
            logger.error(e) { "Error fetching shares for $symbol" }
            YFinanceResult.Error(
                "Failed to fetch shares: ${e.message}",
                cause = e,
                errorType = when (e) {
                    is HttpRequestTimeoutException -> YFinanceResult.Error.ErrorType.NETWORK_ERROR
                    else -> YFinanceResult.Error.ErrorType.UNKNOWN
                }
            )
        }
    }

    /**
     * Close the HTTP client
     */
    fun close() {
        client.close()
    }
}
