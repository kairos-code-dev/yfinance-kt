package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Calendar events for a ticker
 *
 * @property symbol The ticker symbol
 * @property earnings Earnings announcement date (epoch seconds)
 * @property exDividendDate Ex-dividend date (epoch seconds)
 * @property dividendDate Dividend payment date (epoch seconds)
 * @property earningsAverage Average earnings estimate
 * @property earningsLow Low earnings estimate
 * @property earningsHigh High earnings estimate
 * @property revenueAverage Average revenue estimate
 * @property revenueLow Low revenue estimate
 * @property revenueHigh High revenue estimate
 */
@Serializable
data class Calendar(
    val symbol: String,
    val earnings: Long? = null,
    val exDividendDate: Long? = null,
    val dividendDate: Long? = null,
    val earningsAverage: Double? = null,
    val earningsLow: Double? = null,
    val earningsHigh: Double? = null,
    val revenueAverage: Long? = null,
    val revenueLow: Long? = null,
    val revenueHigh: Long? = null
)

/**
 * Sustainability/ESG scores
 *
 * @property symbol The ticker symbol
 * @property environmentScore Environment pillar score
 * @property socialScore Social pillar score
 * @property governanceScore Governance pillar score
 * @property totalEsg Total ESG score
 * @property percentile ESG percentile
 * @property esgPerformance ESG performance level
 * @property peerGroup Peer group
 * @property highestControversy Highest controversy level
 * @property peerCount Number of peers
 * @property peerEnvironmentPerformance Peer environment performance
 * @property peerGovernancePerformance Peer governance performance
 * @property peerSocialPerformance Peer social performance
 * @property peerEsgScorePerformance Peer ESG score performance
 * @property peerHighestControversyPerformance Peer highest controversy performance
 */
@Serializable
data class Sustainability(
    val symbol: String,
    val environmentScore: Double? = null,
    val socialScore: Double? = null,
    val governanceScore: Double? = null,
    val totalEsg: Double? = null,
    val percentile: Double? = null,
    val esgPerformance: String? = null,
    val peerGroup: String? = null,
    val highestControversy: Int? = null,
    val peerCount: Int? = null,
    val peerEnvironmentPerformance: Double? = null,
    val peerGovernancePerformance: Double? = null,
    val peerSocialPerformance: Double? = null,
    val peerEsgScorePerformance: Double? = null,
    val peerHighestControversyPerformance: Double? = null
)

/**
 * SEC filings
 *
 * @property symbol The ticker symbol
 * @property filings List of filing entries
 */
@Serializable
data class SECFilings(
    val symbol: String,
    val filings: List<SECFiling>
)

/**
 * Individual SEC filing
 *
 * @property date Filing date (epoch seconds)
 * @property epochDate Epoch date
 * @property type Filing type (e.g., "10-K", "10-Q", "8-K")
 * @property title Filing title
 * @property edgarUrl URL to EDGAR filing
 */
@Serializable
data class SECFiling(
    val date: Long,
    val epochDate: Long,
    val type: String,
    val title: String,
    val edgarUrl: String
)

/**
 * Shares outstanding over time
 *
 * @property symbol The ticker symbol
 * @property data Map of timestamps to share counts
 */
@Serializable
data class SharesData(
    val symbol: String,
    val data: Map<Long, Long>
) {
    /**
     * Get the most recent share count
     */
    fun getLatestShares(): Long? = data.maxByOrNull { it.key }?.value

    /**
     * Get shares at a specific timestamp
     */
    fun getSharesAt(timestamp: Long): Long? = data[timestamp]
}

/**
 * Fast info - quickly accessible key metrics
 *
 * @property symbol The ticker symbol
 * @property lastPrice Last trade price
 * @property lastVolume Last trade volume
 * @property previousClose Previous close price
 * @property open Open price
 * @property dayHigh Day high
 * @property dayLow Day low
 * @property regularMarketPreviousClose Regular market previous close
 * @property fiftyDayAverage 50-day moving average
 * @property twoHundredDayAverage 200-day moving average
 * @property yearHigh 52-week high
 * @property yearLow 52-week low
 * @property yearChange Year change percentage
 * @property currency Currency
 * @property marketCap Market capitalization
 * @property shares Shares outstanding
 */
@Serializable
data class FastInfo(
    val symbol: String,
    val lastPrice: Double? = null,
    val lastVolume: Long? = null,
    val previousClose: Double? = null,
    val open: Double? = null,
    val dayHigh: Double? = null,
    val dayLow: Double? = null,
    val regularMarketPreviousClose: Double? = null,
    val fiftyDayAverage: Double? = null,
    val twoHundredDayAverage: Double? = null,
    val yearHigh: Double? = null,
    val yearLow: Double? = null,
    val yearChange: Double? = null,
    val currency: String? = null,
    val marketCap: Long? = null,
    val shares: Long? = null
)
