package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Financial statement frequency
 */
enum class Frequency(val value: String) {
    ANNUAL("annual"),
    QUARTERLY("quarterly"),
    TRAILING("trailing")
}

/**
 * Represents a financial statement (income statement, balance sheet, or cash flow)
 *
 * @property symbol The ticker symbol
 * @property currency The currency of the financial data
 * @property data Map of periods (dates) to line items
 */
@Serializable
data class FinancialStatement(
    val symbol: String,
    val currency: String? = null,
    val data: Map<String, Map<String, Long>> = emptyMap()
) {
    /**
     * Get data for a specific period
     */
    fun getDataForPeriod(period: String): Map<String, Long>? = data[period]

    /**
     * Get all available periods sorted
     */
    fun getAvailablePeriods(): List<String> = data.keys.sorted()

    /**
     * Get a specific line item across all periods
     */
    fun getLineItem(itemName: String): Map<String, Long> {
        return data.mapNotNull { (period, items) ->
            items[itemName]?.let { period to it }
        }.toMap()
    }

    /**
     * Get the latest period data
     */
    fun getLatestData(): Pair<String, Map<String, Long>>? {
        val latestPeriod = getAvailablePeriods().lastOrNull() ?: return null
        val periodData = data[latestPeriod] ?: return null
        return latestPeriod to periodData
    }
}

/**
 * Common financial line items for income statement
 */
object IncomeStatementItems {
    const val TOTAL_REVENUE = "Total Revenue"
    const val COST_OF_REVENUE = "Cost Of Revenue"
    const val GROSS_PROFIT = "Gross Profit"
    const val OPERATING_EXPENSE = "Operating Expense"
    const val OPERATING_INCOME = "Operating Income"
    const val NET_INCOME = "Net Income"
    const val EBITDA = "EBITDA"
    const val DILUTED_EPS = "Diluted EPS"
    const val BASIC_EPS = "Basic EPS"
}

/**
 * Common financial line items for balance sheet
 */
object BalanceSheetItems {
    const val TOTAL_ASSETS = "Total Assets"
    const val CURRENT_ASSETS = "Current Assets"
    const val CASH_AND_EQUIVALENTS = "Cash And Cash Equivalents"
    const val TOTAL_LIABILITIES = "Total Liabilities Net Minority Interest"
    const val CURRENT_LIABILITIES = "Current Liabilities"
    const val STOCKHOLDERS_EQUITY = "Stockholders Equity"
    const val TOTAL_DEBT = "Total Debt"
    const val NET_DEBT = "Net Debt"
}

/**
 * Common financial line items for cash flow statement
 */
object CashFlowItems {
    const val OPERATING_CASH_FLOW = "Operating Cash Flow"
    const val INVESTING_CASH_FLOW = "Investing Cash Flow"
    const val FINANCING_CASH_FLOW = "Financing Cash Flow"
    const val FREE_CASH_FLOW = "Free Cash Flow"
    const val CAPITAL_EXPENDITURE = "Capital Expenditure"
    const val END_CASH_POSITION = "End Cash Position"
}
