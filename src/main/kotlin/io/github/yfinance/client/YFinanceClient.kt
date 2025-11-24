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

    private fun buildQuoteSummaryUrl(symbol: String): String {
        val modules = listOf(
            "assetProfile",
            "price",
            "summaryDetail",
            "defaultKeyStatistics",
            "financialData",
            "calendarEvents"
        ).joinToString(",")

        return "https://query2.finance.yahoo.com/v10/finance/quoteSummary/$symbol" +
                "?modules=$modules"
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
     * Close the HTTP client
     */
    fun close() {
        client.close()
    }
}
