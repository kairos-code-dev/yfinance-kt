package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Represents a single option contract
 *
 * @property contractSymbol The option contract symbol
 * @property lastTradeDate Last trade date (epoch seconds)
 * @property strike Strike price
 * @property lastPrice Last traded price
 * @property bid Bid price
 * @property ask Ask price
 * @property change Price change
 * @property percentChange Percentage change
 * @property volume Trading volume
 * @property openInterest Open interest
 * @property impliedVolatility Implied volatility
 * @property inTheMoney Whether the option is in the money
 * @property contractSize Contract size (usually 100)
 * @property currency Currency
 */
@Serializable
data class OptionContract(
    val contractSymbol: String,
    val lastTradeDate: Long? = null,
    val strike: Double,
    val lastPrice: Double? = null,
    val bid: Double? = null,
    val ask: Double? = null,
    val change: Double? = null,
    val percentChange: Double? = null,
    val volume: Long? = null,
    val openInterest: Long? = null,
    val impliedVolatility: Double? = null,
    val inTheMoney: Boolean = false,
    val contractSize: String = "REGULAR",
    val currency: String = "USD"
) {
    /**
     * Get the mid price (average of bid and ask)
     */
    fun getMidPrice(): Double? {
        return if (bid != null && ask != null) {
            (bid + ask) / 2.0
        } else null
    }

    /**
     * Get the intrinsic value for a call option
     */
    fun getIntrinsicValueCall(spotPrice: Double): Double {
        return maxOf(spotPrice - strike, 0.0)
    }

    /**
     * Get the intrinsic value for a put option
     */
    fun getIntrinsicValuePut(spotPrice: Double): Double {
        return maxOf(strike - spotPrice, 0.0)
    }
}

/**
 * Option chain data for a specific expiration date
 *
 * @property expirationDate Expiration date (epoch seconds)
 * @property calls List of call option contracts
 * @property puts List of put option contracts
 * @property underlyingPrice Price of the underlying asset
 * @property underlyingSymbol Symbol of the underlying asset
 */
@Serializable
data class OptionChain(
    val expirationDate: Long,
    val calls: List<OptionContract>,
    val puts: List<OptionContract>,
    val underlyingPrice: Double? = null,
    val underlyingSymbol: String? = null
) {
    /**
     * Get call options by strike price
     */
    fun getCallsByStrike(strike: Double): List<OptionContract> =
        calls.filter { it.strike == strike }

    /**
     * Get put options by strike price
     */
    fun getPutsByStrike(strike: Double): List<OptionContract> =
        puts.filter { it.strike == strike }

    /**
     * Get all strikes (sorted)
     */
    fun getAllStrikes(): List<Double> =
        (calls.map { it.strike } + puts.map { it.strike }).distinct().sorted()

    /**
     * Get at-the-money strike
     */
    fun getATMStrike(): Double? {
        if (underlyingPrice == null) return null
        val strikes = getAllStrikes()
        return strikes.minByOrNull { kotlin.math.abs(it - underlyingPrice) }
    }
}

/**
 * Collection of option expiration dates
 *
 * @property symbol The ticker symbol
 * @property expirations List of expiration dates (epoch seconds)
 */
@Serializable
data class OptionsExpirations(
    val symbol: String,
    val expirations: List<Long>
) {
    /**
     * Get expirations sorted by date
     */
    fun getSortedExpirations(): List<Long> = expirations.sorted()
}
