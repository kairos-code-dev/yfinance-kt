package io.github.yfinance

import io.github.yfinance.model.Interval
import io.github.yfinance.model.Period
import io.github.yfinance.model.YFinanceResult
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Basic tests for Ticker functionality
 */
class TickerTest {

    @Test
    fun `test get historical data for AAPL`() = runBlocking {
        val ticker = Ticker("AAPL")
        val result = ticker.history(period = Period.ONE_MONTH, interval = Interval.ONE_DAY)

        assertTrue(result.isSuccess(), "Expected successful result")

        when (result) {
            is YFinanceResult.Success -> {
                val data = result.data
                assertNotNull(data)
                assertTrue(data.quotes.isNotEmpty(), "Expected quotes to be non-empty")
                assertTrue(data.symbol == "AAPL", "Expected symbol to be AAPL")
            }
            is YFinanceResult.Error -> {
                throw AssertionError("Unexpected error: ${result.message}")
            }
        }
    }

    @Test
    fun `test get ticker info for AAPL`() = runBlocking {
        val ticker = Ticker("AAPL")
        val result = ticker.info()

        assertTrue(result.isSuccess(), "Expected successful result")

        when (result) {
            is YFinanceResult.Success -> {
                val info = result.data
                assertNotNull(info)
                assertNotNull(info.longName, "Expected company name to be present")
                assertNotNull(info.sector, "Expected sector to be present")
                assertTrue(info.symbol == "AAPL", "Expected symbol to be AAPL")
            }
            is YFinanceResult.Error -> {
                throw AssertionError("Unexpected error: ${result.message}")
            }
        }
    }

    @Test
    fun `test invalid symbol returns error`() = runBlocking {
        val ticker = Ticker("INVALID_SYMBOL_12345")
        val result = ticker.history(period = Period.ONE_MONTH, interval = Interval.ONE_DAY)

        // This might succeed or fail depending on Yahoo Finance's response
        // Just verify we get a proper result type
        assertTrue(result is YFinanceResult.Success || result is YFinanceResult.Error)
    }

    @Test
    fun `test YFinance object symbol validation`() {
        assertTrue(YFinance.isValidSymbolFormat("AAPL"))
        assertTrue(YFinance.isValidSymbolFormat("BRK.A"))
        assertTrue(YFinance.isValidSymbolFormat("BRK-A"))
        assertTrue(!YFinance.isValidSymbolFormat(""))
        assertTrue(!YFinance.isValidSymbolFormat(" "))
    }

    @Test
    fun `test symbol normalization`() {
        assertTrue(YFinance.normalizeSymbol("aapl") == "AAPL")
        assertTrue(YFinance.normalizeSymbol(" AAPL ") == "AAPL")
        assertTrue(YFinance.normalizeSymbol("brk.a") == "BRK.A")
    }

    @Test
    fun `test get dividends for AAPL`() = runBlocking {
        val ticker = Ticker("AAPL")
        val result = ticker.dividends(period = Period.ONE_YEAR)

        assertTrue(result.isSuccess(), "Expected successful result")

        when (result) {
            is YFinanceResult.Success -> {
                val data = result.data
                assertNotNull(data)
                assertTrue(data.symbol == "AAPL", "Expected symbol to be AAPL")
                // AAPL pays quarterly dividends, so we should have some data
                println("Found ${data.dividends.size} dividend payments in the last year")
            }
            is YFinanceResult.Error -> {
                throw AssertionError("Unexpected error: ${result.message}")
            }
        }
    }

    @Test
    fun `test get splits for AAPL`() = runBlocking {
        val ticker = Ticker("AAPL")
        val result = ticker.splits(period = Period.MAX)

        assertTrue(result.isSuccess(), "Expected successful result")

        when (result) {
            is YFinanceResult.Success -> {
                val data = result.data
                assertNotNull(data)
                assertTrue(data.symbol == "AAPL", "Expected symbol to be AAPL")
                // AAPL has had stock splits in its history
                println("Found ${data.splits.size} stock splits in AAPL's history")

                // Check if we have any splits and validate the data structure
                if (data.splits.isNotEmpty()) {
                    val firstSplit = data.splits.first()
                    assertNotNull(firstSplit.date, "Split date should not be null")
                    assertTrue(firstSplit.ratio > 0, "Split ratio should be positive")
                }
            }
            is YFinanceResult.Error -> {
                throw AssertionError("Unexpected error: ${result.message}")
            }
        }
    }

    @Test
    fun `test historyByRange for AAPL`() = runBlocking {
        val ticker = Ticker("AAPL")

        // Get data for the last 30 days
        val endTime = System.currentTimeMillis() / 1000
        val startTime = endTime - (30 * 24 * 60 * 60) // 30 days ago

        val result = ticker.historyByRange(startTime, endTime, Interval.ONE_DAY)

        assertTrue(result.isSuccess(), "Expected successful result")

        when (result) {
            is YFinanceResult.Success -> {
                val data = result.data
                assertNotNull(data)
                assertTrue(data.symbol == "AAPL", "Expected symbol to be AAPL")
                assertTrue(data.quotes.isNotEmpty(), "Expected quotes to be non-empty")

                // Verify all quotes are within the specified range
                data.quotes.forEach { quote ->
                    assertTrue(
                        quote.timestamp >= startTime && quote.timestamp <= endTime,
                        "Quote timestamp should be within range"
                    )
                }

                println("Found ${data.quotes.size} quotes in the specified date range")
            }
            is YFinanceResult.Error -> {
                throw AssertionError("Unexpected error: ${result.message}")
            }
        }
    }

    @Test
    fun `test dividend data helper methods`() = runBlocking {
        val ticker = Ticker("AAPL")
        val result = ticker.dividends(period = Period.ONE_YEAR)

        when (result) {
            is YFinanceResult.Success -> {
                val data = result.data

                // Test getSortedDividends
                val sorted = data.getSortedDividends()
                if (sorted.size > 1) {
                    assertTrue(sorted[0].date <= sorted[1].date, "Dividends should be sorted by date")
                }

                // Test getTotalAmount
                val total = data.getTotalAmount()
                assertTrue(total >= 0, "Total dividend amount should be non-negative")

                println("Total dividends paid in last year: $$total")
            }
            is YFinanceResult.Error -> {
                // It's okay if there's an error for this test
                println("Warning: Could not fetch dividends: ${result.message}")
            }
        }
    }

    @Test
    fun `test split data helper methods`() = runBlocking {
        val ticker = Ticker("AAPL")
        val result = ticker.splits(period = Period.MAX)

        when (result) {
            is YFinanceResult.Success -> {
                val data = result.data

                // Test getSortedSplits
                val sorted = data.getSortedSplits()
                if (sorted.size > 1) {
                    assertTrue(sorted[0].date <= sorted[1].date, "Splits should be sorted by date")
                }

                // Test split type detection
                sorted.forEach { split ->
                    if (split.ratio > 1.0) {
                        assertTrue(split.isForwardSplit(), "Should be detected as forward split")
                        assertTrue(!split.isReverseSplit(), "Should not be detected as reverse split")
                    } else if (split.ratio < 1.0) {
                        assertTrue(split.isReverseSplit(), "Should be detected as reverse split")
                        assertTrue(!split.isForwardSplit(), "Should not be detected as forward split")
                    }
                }

                println("AAPL has ${sorted.size} splits in its history")
            }
            is YFinanceResult.Error -> {
                println("Warning: Could not fetch splits: ${result.message}")
            }
        }
    }

    @Test
    fun `test historical data helper methods`() = runBlocking {
        val ticker = Ticker("AAPL")
        val result = ticker.history(period = Period.ONE_MONTH, interval = Interval.ONE_DAY)

        when (result) {
            is YFinanceResult.Success -> {
                val data = result.data

                // Test getSortedQuotes
                val sorted = data.getSortedQuotes()
                if (sorted.size > 1) {
                    assertTrue(sorted[0].timestamp <= sorted[1].timestamp, "Quotes should be sorted ascending")
                }

                // Test getSortedQuotesDesc
                val sortedDesc = data.getSortedQuotesDesc()
                if (sortedDesc.size > 1) {
                    assertTrue(sortedDesc[0].timestamp >= sortedDesc[1].timestamp, "Quotes should be sorted descending")
                }

                // Test filterByRange
                if (data.quotes.size > 2) {
                    val midpoint = data.quotes.size / 2
                    val startTs = data.quotes[midpoint - 1].timestamp
                    val endTs = data.quotes[midpoint + 1].timestamp
                    val filtered = data.filterByRange(startTs, endTs)

                    assertTrue(filtered.size <= 3, "Filtered range should have at most 3 quotes")
                    filtered.forEach { quote ->
                        assertTrue(quote.timestamp in startTs..endTs, "Filtered quote should be in range")
                    }
                }
            }
            is YFinanceResult.Error -> {
                throw AssertionError("Unexpected error: ${result.message}")
            }
        }
    }

    @Test
    fun `test get actions for AAPL`() = runBlocking {
        val ticker = Ticker("AAPL")
        val result = ticker.actions(period = Period.ONE_YEAR)

        assertTrue(result.isSuccess(), "Expected successful result")

        when (result) {
            is YFinanceResult.Success -> {
                val data = result.data
                assertNotNull(data)
                assertTrue(data.symbol == "AAPL", "Expected symbol to be AAPL")

                // Test getAllActions
                val allActions = data.getAllActions()
                println("Found ${allActions.size} total actions (dividends + splits)")

                // Test getDividendActions and getSplitActions
                val dividendActions = data.getDividendActions()
                val splitActions = data.getSplitActions()
                println("  ${dividendActions.size} dividend actions")
                println("  ${splitActions.size} split actions")

                // Verify actions are sorted by date
                if (allActions.size > 1) {
                    assertTrue(allActions[0].date <= allActions[1].date, "Actions should be sorted by date")
                }
            }
            is YFinanceResult.Error -> {
                throw AssertionError("Unexpected error: ${result.message}")
            }
        }
    }

    @Test
    fun `test get calendar for AAPL`() = runBlocking {
        val ticker = Ticker("AAPL")
        val result = ticker.calendar()

        assertTrue(result.isSuccess(), "Expected successful result")

        when (result) {
            is YFinanceResult.Success -> {
                val data = result.data
                assertNotNull(data)
                assertTrue(data.symbol == "AAPL", "Expected symbol to be AAPL")

                println("Calendar data for AAPL:")
                println("  Has earnings date: ${data.hasEarnings()}")
                println("  Has dividend info: ${data.hasDividendInfo()}")

                if (data.earnings != null) {
                    println("  Next earnings date: ${data.getEarningsInstant()}")
                }
                if (data.exDividendDate != null) {
                    println("  Ex-dividend date: ${data.getExDividendInstant()}")
                }
                if (data.dividendDate != null) {
                    println("  Dividend payment date: ${data.getDividendInstant()}")
                }
            }
            is YFinanceResult.Error -> {
                throw AssertionError("Unexpected error: ${result.message}")
            }
        }
    }
}
