package io.github.yfinance.examples

import io.github.yfinance.Ticker
import io.github.yfinance.model.Interval
import io.github.yfinance.model.Period
import io.github.yfinance.model.YFinanceResult
import kotlinx.coroutines.runBlocking

/**
 * Basic usage examples for YFinance-KT
 */
fun main() = runBlocking {
    // Example 1: Get historical data
    println("=== Example 1: Historical Data ===")
    val ticker = Ticker("AAPL")

    when (val result = ticker.history(period = Period.ONE_MONTH, interval = Interval.ONE_DAY)) {
        is YFinanceResult.Success -> {
            val data = result.data
            println("Symbol: ${data.symbol}")
            println("Currency: ${data.currency}")
            println("Number of quotes: ${data.quotes.size}")
            println("\nLatest quote:")
            data.quotes.lastOrNull()?.let { quote ->
                println("  Date: ${quote.getInstant()}")
                println("  Open: ${quote.open}")
                println("  High: ${quote.high}")
                println("  Low: ${quote.low}")
                println("  Close: ${quote.close}")
                println("  Volume: ${quote.volume}")
            }
        }
        is YFinanceResult.Error -> {
            println("Error: ${result.message}")
        }
    }

    // Example 2: Get company information
    println("\n=== Example 2: Company Information ===")
    when (val result = ticker.info()) {
        is YFinanceResult.Success -> {
            val info = result.data
            println("Company: ${info.longName}")
            println("Sector: ${info.sector}")
            println("Industry: ${info.industry}")
            println("Website: ${info.website}")
            println("Market Cap: ${info.marketCap}")
            println("CEO: ${info.ceo}")
            println("Employees: ${info.employees}")
            println("\nPrice Information:")
            println("  Previous Close: ${info.previousClose}")
            println("  52 Week High: ${info.fiftyTwoWeekHigh}")
            println("  52 Week Low: ${info.fiftyTwoWeekLow}")
            println("  P/E Ratio: ${info.trailingPE}")
        }
        is YFinanceResult.Error -> {
            println("Error: ${result.message}")
        }
    }

    // Example 3: Get dividends
    println("\n=== Example 3: Dividend History ===")
    when (val result = ticker.dividends(Period.FIVE_YEARS)) {
        is YFinanceResult.Success -> {
            val dividends = result.data
            println("Symbol: ${dividends.symbol}")
            println("Total dividends in period: ${dividends.dividends.size}")
            println("Total amount: $${dividends.getTotalAmount()}")
            println("\nRecent dividends:")
            dividends.getSortedDividends().takeLast(5).forEach { dividend ->
                println("  ${dividend.getInstant()} - $${dividend.amount}")
            }
        }
        is YFinanceResult.Error -> {
            println("Error: ${result.message}")
        }
    }

    // Example 4: Get stock splits
    println("\n=== Example 4: Stock Splits ===")
    when (val result = ticker.splits()) {
        is YFinanceResult.Success -> {
            val splits = result.data
            if (splits.splits.isEmpty()) {
                println("No stock splits found")
            } else {
                println("Symbol: ${splits.symbol}")
                println("Total splits: ${splits.splits.size}")
                splits.getSortedSplits().forEach { split ->
                    val type = if (split.isForwardSplit()) "Forward" else "Reverse"
                    println("  ${split.getInstant()} - $type split: ${split.ratio}")
                }
            }
        }
        is YFinanceResult.Error -> {
            println("Error: ${result.message}")
        }
    }

    // Example 5: Using DSL-style
    println("\n=== Example 5: DSL Style ===")
    io.github.yfinance.ticker("TSLA") {
        history(Period.ONE_WEEK, Interval.ONE_DAY)
    }.onSuccess { data ->
        println("TSLA - ${data.quotes.size} quotes retrieved")
        println("Latest close: ${data.quotes.lastOrNull()?.close}")
    }.onError { error ->
        println("Error: ${error.message}")
    }
}
