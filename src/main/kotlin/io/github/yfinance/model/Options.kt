package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Option contract data
 *
 * @property contractSymbol The option contract symbol
 * @property strike Strike price
 * @property currency Currency
 * @property lastPrice Last traded price
 * @property change Price change
 * @property percentChange Percent change
 * @property volume Trading volume
 * @property openInterest Open interest
 * @property bid Bid price
 * @property ask Ask price
 * @property contractSize Contract size (usually 100)
 * @property expiration Expiration date (epoch seconds)
 * @property lastTradeDate Last trade date (epoch seconds)
 * @property impliedVolatility Implied volatility
 * @property inTheMoney Whether the option is in the money
 */
@Serializable
data class OptionContract(
    val contractSymbol: String,
    val strike: Double,
    val currency: String,
    val lastPrice: Double? = null,
    val change: Double? = null,
    val percentChange: Double? = null,
    val volume: Long? = null,
    val openInterest: Long? = null,
    val bid: Double? = null,
    val ask: Double? = null,
    val contractSize: String? = null,
    val expiration: Long,
    val lastTradeDate: Long? = null,
    val impliedVolatility: Double? = null,
    val inTheMoney: Boolean? = null
) {
    /**
     * Get expiration as Instant
     */
    fun getExpirationInstant(): Instant = Instant.fromEpochSeconds(expiration)

    /**
     * Get last trade date as Instant
     */
    fun getLastTradeInstant(): Instant? = lastTradeDate?.let { Instant.fromEpochSeconds(it) }

    /**
     * Calculate bid-ask spread
     */
    fun getBidAskSpread(): Double? {
        val b = bid ?: return null
        val a = ask ?: return null
        return a - b
    }
}

/**
 * Option chain data for a specific expiration date
 *
 * @property symbol The ticker symbol
 * @property expirationDate Expiration date (epoch seconds)
 * @property calls List of call options
 * @property puts List of put options
 */
@Serializable
data class OptionChain(
    val symbol: String,
    val expirationDate: Long,
    val calls: List<OptionContract>,
    val puts: List<OptionContract>
) {
    /**
     * Get all strikes available
     */
    fun getAllStrikes(): List<Double> {
        return (calls.map { it.strike } + puts.map { it.strike }).distinct().sorted()
    }

    /**
     * Get call option for specific strike
     */
    fun getCall(strike: Double): OptionContract? {
        return calls.firstOrNull { it.strike == strike }
    }

    /**
     * Get put option for specific strike
     */
    fun getPut(strike: Double): OptionContract? {
        return puts.firstOrNull { it.strike == strike }
    }

    /**
     * Get in-the-money calls
     */
    fun getInTheMoneyCall(): List<OptionContract> {
        return calls.filter { it.inTheMoney == true }
    }

    /**
     * Get in-the-money puts
     */
    fun getInTheMoneyPuts(): List<OptionContract> {
        return puts.filter { it.inTheMoney == true }
    }
}

/**
 * Fast info - quick access to key ticker data
 *
 * @property symbol The ticker symbol
 * @property lastPrice Last traded price
 * @property open Opening price
 * @property dayHigh Day's high
 * @property dayLow Day's low
 * @property previousClose Previous close
 * @property marketCap Market capitalization
 * @property volume Trading volume
 * @property fiftyTwoWeekHigh 52-week high
 * @property fiftyTwoWeekLow 52-week low
 * @property shares Shares outstanding
 * @property currency Currency
 */
@Serializable
data class FastInfo(
    val symbol: String,
    val lastPrice: Double? = null,
    val open: Double? = null,
    val dayHigh: Double? = null,
    val dayLow: Double? = null,
    val previousClose: Double? = null,
    val marketCap: Long? = null,
    val volume: Long? = null,
    val fiftyTwoWeekHigh: Double? = null,
    val fiftyTwoWeekLow: Double? = null,
    val shares: Long? = null,
    val currency: String? = null
) {
    /**
     * Calculate day's range
     */
    fun getDayRange(): Pair<Double, Double>? {
        val low = dayLow ?: return null
        val high = dayHigh ?: return null
        return low to high
    }

    /**
     * Calculate price change
     */
    fun getPriceChange(): Double? {
        val last = lastPrice ?: return null
        val prev = previousClose ?: return null
        return last - prev
    }

    /**
     * Calculate percent change
     */
    fun getPercentChange(): Double? {
        val change = getPriceChange() ?: return null
        val prev = previousClose ?: return null
        return (change / prev) * 100
    }
}

/**
 * Sustainability/ESG scores
 *
 * @property symbol The ticker symbol
 * @property totalEsg Total ESG score
 * @property environmentScore Environment score
 * @property socialScore Social score
 * @property governanceScore Governance score
 * @property controversyLevel Controversy level (1-5, 5 being worst)
 * @property esgPerformance ESG performance rating
 * @property percentile Percentile ranking in peer group
 */
@Serializable
data class Sustainability(
    val symbol: String,
    val totalEsg: Double? = null,
    val environmentScore: Double? = null,
    val socialScore: Double? = null,
    val governanceScore: Double? = null,
    val controversyLevel: Int? = null,
    val esgPerformance: String? = null,
    val percentile: Double? = null
) {
    /**
     * Check if has high controversy
     */
    fun hasHighControversy(): Boolean = (controversyLevel ?: 0) >= 4

    /**
     * Get ESG rating category
     */
    fun getRatingCategory(): String {
        val score = totalEsg ?: return "N/A"
        return when {
            score >= 80 -> "Excellent"
            score >= 60 -> "Good"
            score >= 40 -> "Average"
            score >= 20 -> "Below Average"
            else -> "Poor"
        }
    }
}

/**
 * Capital gains distribution
 *
 * @property date Distribution date (epoch seconds)
 * @property amount Distribution amount per share
 */
@Serializable
data class CapitalGain(
    val date: Long,
    val amount: Double
) {
    /**
     * Get date as Instant
     */
    fun getInstant(): Instant = Instant.fromEpochSeconds(date)
}

/**
 * Capital gains data
 *
 * @property symbol The ticker symbol
 * @property gains List of capital gains distributions
 */
@Serializable
data class CapitalGainsData(
    val symbol: String,
    val gains: List<CapitalGain>
) {
    /**
     * Get gains sorted by date
     */
    fun getSortedGains(): List<CapitalGain> = gains.sortedBy { it.date }

    /**
     * Get total distributions
     */
    fun getTotalAmount(): Double = gains.sumOf { it.amount }
}

/**
 * Shares outstanding over time
 *
 * @property symbol The ticker symbol
 * @property data Map of timestamp to shares outstanding
 */
@Serializable
data class SharesData(
    val symbol: String,
    val data: Map<Long, Long>
) {
    /**
     * Get latest shares count
     */
    fun getLatestShares(): Long? {
        return data.maxByOrNull { it.key }?.value
    }

    /**
     * Get shares at specific timestamp
     */
    fun getSharesAt(timestamp: Long): Long? {
        return data[timestamp]
    }

    /**
     * Calculate share growth
     */
    fun getShareGrowth(): Map<Long, Double> {
        val sorted = data.entries.sortedBy { it.key }
        return sorted.zipWithNext().associate { (prev, curr) ->
            val growth = ((curr.value - prev.value).toDouble() / prev.value) * 100
            curr.key to growth
        }
    }
}
