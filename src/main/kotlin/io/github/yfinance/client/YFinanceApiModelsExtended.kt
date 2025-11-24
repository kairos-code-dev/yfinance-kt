package io.github.yfinance.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Additional API models for financial statements, earnings, and other data
 */

// Financial Statements Response Models
@Serializable
internal data class FinancialStatementsResponse(
    val incomeStatementHistory: FinancialStatements? = null,
    val incomeStatementHistoryQuarterly: FinancialStatements? = null,
    val balanceSheetHistory: FinancialStatements? = null,
    val balanceSheetHistoryQuarterly: FinancialStatements? = null,
    val cashFlowStatementHistory: FinancialStatements? = null,
    val cashFlowStatementHistoryQuarterly: FinancialStatements? = null
)

@Serializable
internal data class FinancialStatements(
    val financialStatements: List<FinancialStatement>? = null
)

@Serializable
internal data class FinancialStatement(
    val endDate: RawValue? = null,
    val data: Map<String, RawValue>? = null
)

// Earnings Response Models
@Serializable
internal data class EarningsResponse(
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

// Holders Response Models
@Serializable
internal data class HoldersResponse(
    val majorDirectHolders: MajorDirectHolders? = null,
    val majorHoldersBreakdown: MajorHoldersBreakdown? = null
)

@Serializable
internal data class MajorDirectHolders(
    val holders: List<DirectHolder>? = null,
    val maxAge: Int? = null
)

@Serializable
internal data class DirectHolder(
    val name: String? = null,
    val relation: String? = null,
    val url: String? = null,
    val transactionDescription: String? = null,
    val latestTransDate: RawValue? = null,
    val positionDirect: RawValue? = null,
    val positionDirectDate: RawValue? = null
)

@Serializable
internal data class MajorHoldersBreakdown(
    val insidersPercentHeld: RawValue? = null,
    val institutionsPercentHeld: RawValue? = null,
    val institutionsFloatPercentHeld: RawValue? = null,
    val institutionsCount: RawValue? = null
)

@Serializable
internal data class InstitutionOwnership(
    val ownershipList: List<InstitutionOwner>? = null,
    val maxAge: Int? = null
)

@Serializable
internal data class InstitutionOwner(
    val organization: String,
    val pctHeld: RawValue? = null,
    val position: RawValue? = null,
    val value: RawValue? = null,
    val reportDate: RawValue? = null
)

// Recommendations Models
@Serializable
internal data class RecommendationTrend(
    val trend: List<RecommendationPeriod>? = null
)

@Serializable
internal data class RecommendationPeriod(
    val period: String,
    val strongBuy: Int = 0,
    val buy: Int = 0,
    val hold: Int = 0,
    val sell: Int = 0,
    val strongSell: Int = 0
)

@Serializable
internal data class UpgradeDowngradeHistory(
    val history: List<UpgradeDowngrade>? = null
)

@Serializable
internal data class UpgradeDowngrade(
    val epochGradeDate: Long,
    val firm: String,
    val toGrade: String? = null,
    val fromGrade: String? = null,
    val action: String? = null
)

// ESG/Sustainability Models
@Serializable
internal data class EsgScores(
    val totalEsg: RawValue? = null,
    val environmentScore: RawValue? = null,
    val socialScore: RawValue? = null,
    val governanceScore: RawValue? = null,
    val ratingYear: Int? = null,
    val ratingMonth: Int? = null,
    val highestControversy: Int? = null,
    val peerCount: Int? = null,
    val esgPerformance: String? = null,
    val peerGroup: String? = null,
    val peerEsgScorePerformance: RawValue? = null,
    val peerEnvironmentPerformance: RawValue? = null,
    val peerSocialPerformance: RawValue? = null,
    val peerGovernancePerformance: RawValue? = null,
    val peerHighestControversyPerformance: RawValue? = null,
    val percentile: RawValue? = null
)

// SEC Filings Models
@Serializable
internal data class SecFilings(
    val filings: List<SecFiling>? = null,
    val maxAge: Int? = null
)

@Serializable
internal data class SecFiling(
    val date: String,
    val epochDate: Long,
    val type: String,
    val title: String,
    val edgarUrl: String,
    val maxAge: Int? = null
)

// News Models
@Serializable
internal data class NewsResponse(
    val news: List<NewsItem>? = null
)

@Serializable
internal data class NewsItem(
    val uuid: String,
    val title: String,
    val publisher: String,
    val link: String,
    val providerPublishTime: Long,
    val type: String? = null,
    val thumbnail: NewsThumbnail? = null,
    val relatedTickers: List<String>? = null
)

@Serializable
internal data class NewsThumbnail(
    val resolutions: List<ThumbnailResolution>? = null
)

@Serializable
internal data class ThumbnailResolution(
    val url: String,
    val width: Int,
    val height: Int,
    val tag: String? = null
)

// Options Models (already partially defined, extending)
@Serializable
internal data class OptionsResponse(
    val optionChain: OptionsChainData
)

@Serializable
internal data class OptionsChainData(
    val result: List<OptionsChainResult>? = null,
    val error: ApiError? = null
)

@Serializable
internal data class OptionsChainResult(
    val underlyingSymbol: String,
    val expirationDates: List<Long>,
    val strikes: List<Double>,
    val hasMiniOptions: Boolean? = null,
    val quote: OptionsQuote? = null,
    val options: List<OptionsData>? = null
)

@Serializable
internal data class OptionsQuote(
    val language: String? = null,
    val region: String? = null,
    val quoteType: String? = null,
    val typeDisp: String? = null,
    val quoteSourceName: String? = null,
    val triggerable: Boolean? = null,
    val currency: String? = null,
    val regularMarketPrice: RawValue? = null,
    val regularMarketTime: Long? = null,
    val regularMarketChange: RawValue? = null,
    val regularMarketOpen: RawValue? = null,
    val regularMarketDayHigh: RawValue? = null,
    val regularMarketDayLow: RawValue? = null,
    val regularMarketVolume: RawValue? = null
)

@Serializable
internal data class OptionsData(
    val expirationDate: Long,
    val hasMiniOptions: Boolean? = null,
    val calls: List<OptionContractData>? = null,
    val puts: List<OptionContractData>? = null
)

@Serializable
internal data class OptionContractData(
    val contractSymbol: String,
    val strike: RawValue,
    val currency: String,
    val lastPrice: RawValue? = null,
    val change: RawValue? = null,
    val percentChange: RawValue? = null,
    val volume: RawValue? = null,
    val openInterest: RawValue? = null,
    val bid: RawValue? = null,
    val ask: RawValue? = null,
    val contractSize: String,
    val expiration: Long,
    val lastTradeDate: Long? = null,
    val impliedVolatility: RawValue? = null,
    val inTheMoney: Boolean
)
