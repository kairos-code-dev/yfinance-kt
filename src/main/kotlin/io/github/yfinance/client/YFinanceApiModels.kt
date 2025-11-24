package io.github.yfinance.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Internal models for Yahoo Finance API responses
 * These models map directly to the API response structure
 */

@Serializable
internal data class ChartResponse(
    val chart: ChartData
)

@Serializable
internal data class ChartData(
    val result: List<ChartResult>? = null,
    val error: ApiError? = null
)

@Serializable
internal data class ChartResult(
    val meta: ChartMeta,
    val timestamp: List<Long>? = null,
    val indicators: Indicators,
    val events: Events? = null
)

@Serializable
internal data class ChartMeta(
    val currency: String? = null,
    val symbol: String,
    val exchangeName: String? = null,
    val instrumentType: String? = null,
    val regularMarketPrice: Double? = null,
    val chartPreviousClose: Double? = null,
    val previousClose: Double? = null,
    val scale: Int? = null,
    val priceHint: Int? = null,
    val currentTradingPeriod: TradingPeriod? = null,
    val tradingPeriods: List<List<TradingPeriodInfo>>? = null,
    val dataGranularity: String? = null,
    val range: String? = null,
    val validRanges: List<String>? = null
)

@Serializable
internal data class TradingPeriod(
    val pre: TradingPeriodInfo? = null,
    val regular: TradingPeriodInfo? = null,
    val post: TradingPeriodInfo? = null
)

@Serializable
internal data class TradingPeriodInfo(
    val timezone: String? = null,
    val start: Long? = null,
    val end: Long? = null,
    val gmtoffset: Int? = null
)

@Serializable
internal data class Indicators(
    val quote: List<QuoteData>? = null,
    val adjclose: List<AdjCloseData>? = null
)

@Serializable
internal data class QuoteData(
    val open: List<Double?>? = null,
    val high: List<Double?>? = null,
    val low: List<Double?>? = null,
    val close: List<Double?>? = null,
    val volume: List<Long?>? = null
)

@Serializable
internal data class AdjCloseData(
    val adjclose: List<Double?>? = null
)

@Serializable
internal data class Events(
    val dividends: Map<String, DividendEvent>? = null,
    val splits: Map<String, SplitEvent>? = null
)

@Serializable
internal data class DividendEvent(
    val amount: Double,
    val date: Long
)

@Serializable
internal data class SplitEvent(
    val date: Long,
    val numerator: Double,
    val denominator: Double,
    val splitRatio: String
)

@Serializable
internal data class QuoteSummaryResponse(
    val quoteSummary: QuoteSummaryData
)

@Serializable
internal data class QuoteSummaryData(
    val result: List<QuoteSummaryResult>? = null,
    val error: ApiError? = null
)

@Serializable
internal data class QuoteSummaryResult(
    val assetProfile: AssetProfile? = null,
    val price: PriceData? = null,
    val summaryDetail: SummaryDetail? = null,
    val defaultKeyStatistics: DefaultKeyStatistics? = null,
    val financialData: FinancialData? = null,
    val calendarEvents: CalendarEvents? = null
)

@Serializable
internal data class AssetProfile(
    val address1: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val country: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val industry: String? = null,
    val sector: String? = null,
    val longBusinessSummary: String? = null,
    val fullTimeEmployees: Int? = null,
    val companyOfficers: List<CompanyOfficer>? = null
)

@Serializable
internal data class CompanyOfficer(
    val name: String? = null,
    val title: String? = null,
    val age: Int? = null
)

@Serializable
internal data class PriceData(
    val shortName: RawValue? = null,
    val longName: RawValue? = null,
    val regularMarketPrice: RawValue? = null,
    val regularMarketOpen: RawValue? = null,
    val regularMarketDayHigh: RawValue? = null,
    val regularMarketDayLow: RawValue? = null,
    val regularMarketVolume: RawValue? = null,
    val averageVolume: RawValue? = null,
    val marketCap: RawValue? = null,
    val currency: String? = null,
    val exchange: String? = null,
    val quoteType: String? = null
)

@Serializable
internal data class SummaryDetail(
    val previousClose: RawValue? = null,
    val open: RawValue? = null,
    val dayLow: RawValue? = null,
    val dayHigh: RawValue? = null,
    val regularMarketVolume: RawValue? = null,
    val averageVolume: RawValue? = null,
    val fiftyTwoWeekLow: RawValue? = null,
    val fiftyTwoWeekHigh: RawValue? = null,
    val dividendRate: RawValue? = null,
    val dividendYield: RawValue? = null,
    val exDividendDate: RawValue? = null,
    val beta: RawValue? = null,
    val trailingPE: RawValue? = null,
    val forwardPE: RawValue? = null
)

@Serializable
internal data class DefaultKeyStatistics(
    val enterpriseValue: RawValue? = null,
    val forwardPE: RawValue? = null,
    val profitMargins: RawValue? = null,
    val floatShares: RawValue? = null,
    val sharesOutstanding: RawValue? = null,
    val bookValue: RawValue? = null,
    val priceToBook: RawValue? = null,
    val earningsQuarterlyGrowth: RawValue? = null,
    val netIncomeToCommon: RawValue? = null,
    val trailingEps: RawValue? = null,
    val forwardEps: RawValue? = null,
    val lastDividendValue: RawValue? = null,
    val lastDividendDate: RawValue? = null
)

@Serializable
internal data class FinancialData(
    val currentPrice: RawValue? = null,
    val targetHighPrice: RawValue? = null,
    val targetLowPrice: RawValue? = null,
    val targetMeanPrice: RawValue? = null,
    val targetMedianPrice: RawValue? = null,
    val recommendationMean: RawValue? = null,
    val recommendationKey: String? = null,
    val numberOfAnalystOpinions: RawValue? = null,
    val totalCash: RawValue? = null,
    val totalCashPerShare: RawValue? = null,
    val ebitda: RawValue? = null,
    val totalDebt: RawValue? = null,
    val totalRevenue: RawValue? = null,
    val revenuePerShare: RawValue? = null,
    val returnOnAssets: RawValue? = null,
    val returnOnEquity: RawValue? = null,
    val freeCashflow: RawValue? = null,
    val operatingCashflow: RawValue? = null,
    val earningsGrowth: RawValue? = null,
    val revenueGrowth: RawValue? = null,
    val grossMargins: RawValue? = null,
    val operatingMargins: RawValue? = null,
    val profitMargins: RawValue? = null
)

@Serializable
internal data class CalendarEvents(
    val earnings: EarningsCalendar? = null,
    val dividendDate: RawValue? = null,
    val exDividendDate: RawValue? = null
)

@Serializable
internal data class EarningsCalendar(
    val earningsDate: List<RawValue>? = null,
    val earningsAverage: RawValue? = null,
    val earningsLow: RawValue? = null,
    val earningsHigh: RawValue? = null,
    val revenueAverage: RawValue? = null,
    val revenueLow: RawValue? = null,
    val revenueHigh: RawValue? = null
)

@Serializable
internal data class RawValue(
    val raw: Double? = null,
    val fmt: String? = null
)

@Serializable
internal data class ApiError(
    val code: String? = null,
    val description: String? = null
)
