package io.github.yfinance

import io.github.yfinance.model.Interval
import io.github.yfinance.model.Period
import io.github.yfinance.model.YFinanceResult
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for download() function and Tickers class
 */
class DownloadTest {

    @Test
    fun `test download multiple tickers`() = runBlocking {
        val symbols = listOf("AAPL", "GOOGL", "MSFT")
        val results = download(symbols, period = Period.FIVE_DAYS, interval = Interval.ONE_DAY)

        assertNotNull(results)
        assertEquals(symbols.size, results.size)

        var successCount = 0
        results.forEach { (symbol, result) ->
            when (result) {
                is YFinanceResult.Success -> {
                    assertEquals(symbol, result.data.symbol)
                    successCount++
                    println("✓ Downloaded $symbol: ${result.data.quotes.size} quotes")
                }
                is YFinanceResult.Error -> {
                    println("⚠ Failed to download $symbol: ${result.message}")
                }
            }
        }

        println("✓ Download test passed: $successCount/${ symbols.size} successful")
    }

    @Test
    fun `test download with vararg`() = runBlocking {
        val results = download("AAPL", "GOOGL", period = Period.FIVE_DAYS)

        assertNotNull(results)
        assertTrue(results.containsKey("AAPL"))
        assertTrue(results.containsKey("GOOGL"))

        println("✓ Vararg download test passed")
    }

    @Test
    fun `test Tickers class`() = runBlocking {
        val tickers = Tickers("AAPL", "GOOGL", "MSFT")

        assertEquals(3, tickers.symbols.size)
        assertTrue("AAPL" in tickers.symbols)

        val historyResults = tickers.history(period = Period.FIVE_DAYS)
        assertNotNull(historyResults)
        assertEquals(3, historyResults.size)

        println("✓ Tickers class test passed")
    }

    @Test
    fun `test Tickers info method`() = runBlocking {
        val tickers = Tickers("AAPL", "MSFT")
        val infoResults = tickers.info()

        assertNotNull(infoResults)
        assertEquals(2, infoResults.size)

        infoResults.forEach { (symbol, result) ->
            when (result) {
                is YFinanceResult.Success -> {
                    println("✓ Got info for $symbol: ${result.data.longName}")
                }
                is YFinanceResult.Error -> {
                    println("⚠ Failed to get info for $symbol: ${result.message}")
                }
            }
        }

        println("✓ Tickers info test passed")
    }

    @Test
    fun `test Tickers dividends method`() = runBlocking {
        val tickers = Tickers("AAPL", "MSFT")
        val dividendResults = tickers.dividends(Period.FIVE_YEARS)

        assertNotNull(dividendResults)
        assertEquals(2, dividendResults.size)

        println("✓ Tickers dividends test passed")
    }

    @Test
    fun `test Tickers individual ticker access`() {
        val tickers = Tickers("AAPL", "GOOGL", "MSFT")

        val applTicker = tickers["AAPL"]
        assertEquals("AAPL", applTicker.symbol)

        val indexTicker = tickers[0]
        assertEquals("AAPL", indexTicker.symbol)

        println("✓ Tickers access test passed")
    }

    @Test
    fun `test tickers convenience function`() {
        val tickers1 = tickers("AAPL", "GOOGL")
        assertEquals(2, tickers1.symbols.size)

        val tickers2 = tickers(listOf("AAPL", "GOOGL", "MSFT"))
        assertEquals(3, tickers2.symbols.size)

        println("✓ Convenience function test passed")
    }

    @Test
    fun `test download handles errors gracefully`() = runBlocking {
        val symbols = listOf("AAPL", "INVALID_SYMBOL_XYZ", "GOOGL")
        val results = download(symbols, period = Period.ONE_DAY)

        assertNotNull(results)
        assertEquals(symbols.size, results.size)

        // All symbols should have results (either success or error)
        symbols.forEach { symbol ->
            assertTrue(results.containsKey(symbol))
        }

        println("✓ Error handling test passed")
    }

    @Test
    fun `test empty ticker list`() = runBlocking {
        val results = download(emptyList<String>())

        assertNotNull(results)
        assertTrue(results.isEmpty())

        println("✓ Empty list test passed")
    }

    @Test
    fun `test single ticker download`() = runBlocking {
        val results = download(listOf("AAPL"), period = Period.ONE_DAY)

        assertNotNull(results)
        assertEquals(1, results.size)
        assertTrue(results.containsKey("AAPL"))

        println("✓ Single ticker test passed")
    }
}
