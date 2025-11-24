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
    val calendarEvents: CalendarEvents? = null,
    val incomeStatementHistory: IncomeStatementHistory? = null,
    val incomeStatementHistoryQuarterly: IncomeStatementHistory? = null,
    val balanceSheetHistory: BalanceSheetHistory? = null,
    val balanceSheetHistoryQuarterly: BalanceSheetHistory? = null,
    val cashflowStatementHistory: CashflowStatementHistory? = null,
    val cashflowStatementHistoryQuarterly: CashflowStatementHistory? = null,
    val recommendationTrend: RecommendationTrend? = null,
    val upgradeDowngradeHistory: UpgradeDowngradeHistory? = null,
    val majorHoldersBreakdown: MajorHoldersBreakdown? = null,
    val institutionOwnership: InstitutionOwnership? = null,
    val earningsHistory: EarningsHistory? = null,
    val earnings: EarningsData? = null
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

// Financial statement models
@Serializable
internal data class FinancialStatementItem(
    val raw: Long? = null,
    val fmt: String? = null,
    val longFmt: String? = null
)

@Serializable
internal data class FinancialStatementEntry(
    val endDate: RawValue? = null,
    val totalRevenue: FinancialStatementItem? = null,
    val costOfRevenue: FinancialStatementItem? = null,
    val grossProfit: FinancialStatementItem? = null,
    val operatingExpense: FinancialStatementItem? = null,
    val operatingIncome: FinancialStatementItem? = null,
    val netIncome: FinancialStatementItem? = null,
    val ebitda: FinancialStatementItem? = null,
    val incomeBeforeTax: FinancialStatementItem? = null,
    val incomeTaxExpense: FinancialStatementItem? = null,
    val interestExpense: FinancialStatementItem? = null,
    val totalAssets: FinancialStatementItem? = null,
    val totalLiabilitiesNetMinorityInterest: FinancialStatementItem? = null,
    val totalEquityGrossMinorityInterest: FinancialStatementItem? = null,
    val stockholdersEquity: FinancialStatementItem? = null,
    val totalCapitalization: FinancialStatementItem? = null,
    val commonStock: FinancialStatementItem? = null,
    val retainedEarnings: FinancialStatementItem? = null,
    val totalDebt: FinancialStatementItem? = null,
    val currentAssets: FinancialStatementItem? = null,
    val currentLiabilities: FinancialStatementItem? = null,
    val cashAndCashEquivalents: FinancialStatementItem? = null,
    val operatingCashFlow: FinancialStatementItem? = null,
    val investingCashFlow: FinancialStatementItem? = null,
    val financingCashFlow: FinancialStatementItem? = null,
    val freeCashFlow: FinancialStatementItem? = null,
    val capitalExpenditure: FinancialStatementItem? = null,
    val endCashPosition: FinancialStatementItem? = null,
    val changeInCashSupplementalAsReported: FinancialStatementItem? = null
)

@Serializable
internal data class IncomeStatementHistory(
    val incomeStatementHistory: List<FinancialStatementEntry>? = null
)

@Serializable
internal data class BalanceSheetHistory(
    val balanceSheetStatements: List<FinancialStatementEntry>? = null
)

@Serializable
internal data class CashflowStatementHistory(
    val cashflowStatements: List<FinancialStatementEntry>? = null
)

// News models
@Serializable
internal data class NewsResponse(
    val news: NewsData
)

@Serializable
internal data class NewsData(
    val result: List<NewsItem>? = null,
    val error: ApiError? = null
)

@Serializable
internal data class NewsItem(
    val uuid: String,
    val title: String,
    val publisher: String? = null,
    val link: String,
    val providerPublishTime: Long,
    val type: String? = null,
    val thumbnail: ThumbnailInfo? = null,
    val relatedTickers: List<String>? = null
)

@Serializable
internal data class ThumbnailInfo(
    val resolutions: List<ThumbnailResolution>? = null
)

@Serializable
internal data class ThumbnailResolution(
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
    val tag: String? = null
)

// Recommendations models
@Serializable
internal data class RecommendationTrend(
    val trend: List<RecommendationTrendItem>? = null
)

@Serializable
internal data class RecommendationTrendItem(
    val period: String,
    val strongBuy: Int? = null,
    val buy: Int? = null,
    val hold: Int? = null,
    val sell: Int? = null,
    val strongSell: Int? = null
)

@Serializable
internal data class UpgradeDowngradeHistory(
    val history: List<UpgradeDowngradeItem>? = null
)

@Serializable
internal data class UpgradeDowngradeItem(
    val epochGradeDate: Long,
    val firm: String,
    val toGrade: String,
    val fromGrade: String? = null,
    val action: String? = null
)

// Holdings models
@Serializable
internal data class MajorHoldersBreakdown(
    val insidersPercentHeld: RawValue? = null,
    val institutionsPercentHeld: RawValue? = null,
    val institutionsFloatPercentHeld: RawValue? = null,
    val institutionsCount: RawValue? = null
)

@Serializable
internal data class InstitutionOwnership(
    val ownershipList: List<InstitutionOwner>? = null
)

@Serializable
internal data class InstitutionOwner(
    val organization: String,
    val pctHeld: RawValue? = null,
    val position: RawValue? = null,
    val value: RawValue? = null,
    val reportDate: RawValue? = null
)

// Earnings models
@Serializable
internal data class EarningsHistory(
    val history: List<EarningsHistoryItem>? = null
)

@Serializable
internal data class EarningsHistoryItem(
    val quarter: RawValue? = null,
    val epsActual: RawValue? = null,
    val epsEstimate: RawValue? = null,
    val epsDifference: RawValue? = null,
    val surprisePercent: RawValue? = null
)

@Serializable
internal data class EarningsData(
    val earningsChart: EarningsChart? = null,
    val financialsChart: FinancialsChart? = null
)

@Serializable
internal data class EarningsChart(
    val quarterly: List<QuarterlyEarning>? = null,
    val currentQuarterEstimate: RawValue? = null,
    val currentQuarterEstimateDate: String? = null,
    val currentQuarterEstimateYear: Int? = null
)

@Serializable
internal data class QuarterlyEarning(
    val date: String,
    val actual: RawValue? = null,
    val estimate: RawValue? = null
)

@Serializable
internal data class FinancialsChart(
    val yearly: List<YearlyFinancial>? = null,
    val quarterly: List<QuarterlyFinancial>? = null
)

@Serializable
internal data class YearlyFinancial(
    val date: Int,
    val revenue: RawValue? = null,
    val earnings: RawValue? = null
)

@Serializable
internal data class QuarterlyFinancial(
    val date: String,
    val revenue: RawValue? = null,
    val earnings: RawValue? = null
)
