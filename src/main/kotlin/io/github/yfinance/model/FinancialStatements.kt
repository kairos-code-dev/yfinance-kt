package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Represents a financial statement (Income Statement, Balance Sheet, or Cash Flow)
 *
 * @property symbol The ticker symbol
 * @property data Map of dates to financial metrics
 * @property currency The currency of the financial data
 */
@Serializable
data class FinancialStatement(
    val symbol: String,
    val data: Map<String, Map<String, Double?>>,
    val currency: String? = null
)

/**
 * Income statement data
 */
typealias IncomeStatement = FinancialStatement

/**
 * Balance sheet data
 */
typealias BalanceSheet = FinancialStatement

/**
 * Cash flow statement data
 */
typealias CashFlow = FinancialStatement

/**
 * Frequency for financial statements
 */
enum class StatementFrequency(val value: String) {
    YEARLY("yearly"),
    QUARTERLY("quarterly"),
    TTM("ttm"); // Trailing Twelve Months

    override fun toString(): String = value
}
