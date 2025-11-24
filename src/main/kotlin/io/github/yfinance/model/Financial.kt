package io.github.yfinance.model

import kotlinx.serialization.Serializable

/**
 * Represents a financial statement (income statement, balance sheet, or cash flow)
 *
 * @property symbol The ticker symbol
 * @property currency The currency of the financial data
 * @property annual Annual financial data (years as keys, line items as map)
 * @property quarterly Quarterly financial data (quarters as keys, line items as map)
 */
@Serializable
data class FinancialStatement(
    val symbol: String,
    val currency: String? = null,
    val annual: Map<String, Map<String, Double>> = emptyMap(),
    val quarterly: Map<String, Map<String, Double>> = emptyMap()
) {
    /**
     * Get annual data for a specific year
     */
    fun getAnnualData(year: String): Map<String, Double>? = annual[year]

    /**
     * Get quarterly data for a specific quarter
     */
    fun getQuarterlyData(quarter: String): Map<String, Double>? = quarterly[quarter]

    /**
     * Get all years with annual data
     */
    fun getAvailableYears(): List<String> = annual.keys.sorted()

    /**
     * Get all quarters with quarterly data
     */
    fun getAvailableQuarters(): List<String> = quarterly.keys.sorted()

    /**
     * Get a specific line item across all annual periods
     */
    fun getAnnualLineItem(itemName: String): Map<String, Double> {
        return annual.mapNotNull { (year, items) ->
            items[itemName]?.let { year to it }
        }.toMap()
    }

    /**
     * Get a specific line item across all quarterly periods
     */
    fun getQuarterlyLineItem(itemName: String): Map<String, Double> {
        return quarterly.mapNotNull { (quarter, items) ->
            items[quarter]?.let { quarter to it }
        }.toMap()
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
