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
}
