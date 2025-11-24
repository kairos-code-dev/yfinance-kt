package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Comprehensive information about a ticker/security
 *
 * @property symbol The ticker symbol
 * @property shortName Short name of the company
 * @property longName Full name of the company
 * @property currency Currency of the stock price
 * @property exchange Stock exchange where it's traded
 * @property quoteType Type of security (EQUITY, ETF, etc.)
 * @property marketCap Market capitalization
 * @property sector Business sector
 * @property industry Industry classification
 * @property website Company website
 * @property description Company description
 * @property employees Number of full-time employees
 * @property ceo CEO name
 * @property city Headquarters city
 * @property state Headquarters state
 * @property country Headquarters country
 * @property phone Company phone number
 * @property previousClose Previous closing price
 * @property open Today's opening price
 * @property dayLow Today's lowest price
 * @property dayHigh Today's highest price
 * @property regularMarketVolume Regular market volume
 * @property averageVolume Average volume
 * @property fiftyTwoWeekLow 52-week low
 * @property fiftyTwoWeekHigh 52-week high
 * @property dividendRate Annual dividend rate
 * @property dividendYield Dividend yield
 * @property exDividendDate Ex-dividend date
 * @property beta Beta coefficient
 * @property trailingPE Trailing P/E ratio
 * @property forwardPE Forward P/E ratio
 * @property bookValue Book value per share
 * @property priceToBook Price to book ratio
 * @property earningsPerShare Earnings per share
 * @property revenuePerShare Revenue per share
 * @property returnOnAssets Return on assets
 * @property returnOnEquity Return on equity
 * @property freeCashflow Free cash flow
 * @property operatingCashflow Operating cash flow
 * @property revenueGrowth Revenue growth rate
 * @property earningsGrowth Earnings growth rate
 */
@Serializable
data class TickerInfo(
    val symbol: String,
    val shortName: String? = null,
    val longName: String? = null,
    val currency: String? = null,
    val exchange: String? = null,
    val quoteType: String? = null,
    val marketCap: Long? = null,
    val sector: String? = null,
    val industry: String? = null,
    val website: String? = null,
    val description: String? = null,
    val employees: Int? = null,
    val ceo: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val phone: String? = null,
    val previousClose: Double? = null,
    val open: Double? = null,
    val dayLow: Double? = null,
    val dayHigh: Double? = null,
    val regularMarketVolume: Long? = null,
    val averageVolume: Long? = null,
    val fiftyTwoWeekLow: Double? = null,
    val fiftyTwoWeekHigh: Double? = null,
    val dividendRate: Double? = null,
    val dividendYield: Double? = null,
    val exDividendDate: Long? = null,
    val beta: Double? = null,
    val trailingPE: Double? = null,
    val forwardPE: Double? = null,
    val bookValue: Double? = null,
    val priceToBook: Double? = null,
    val earningsPerShare: Double? = null,
    val revenuePerShare: Double? = null,
    val returnOnAssets: Double? = null,
    val returnOnEquity: Double? = null,
    val freeCashflow: Long? = null,
    val operatingCashflow: Long? = null,
    val revenueGrowth: Double? = null,
    val earningsGrowth: Double? = null
)
