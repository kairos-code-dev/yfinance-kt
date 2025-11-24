package io.github.yfinance

import io.github.yfinance.model.*
import io.github.yfinance.utils.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for data models and utility functions
 */
class ModelTest {

    @Test
    fun `test Period enum values`() {
        assertEquals("1d", Period.ONE_DAY.value)
        assertEquals("5d", Period.FIVE_DAYS.value)
        assertEquals("1mo", Period.ONE_MONTH.value)
        assertEquals("3mo", Period.THREE_MONTHS.value)
        assertEquals("6mo", Period.SIX_MONTHS.value)
        assertEquals("1y", Period.ONE_YEAR.value)
        assertEquals("2y", Period.TWO_YEARS.value)
        assertEquals("5y", Period.FIVE_YEARS.value)
        assertEquals("10y", Period.TEN_YEARS.value)
        assertEquals("ytd", Period.YEAR_TO_DATE.value)
        assertEquals("max", Period.MAX.value)

        println("✓ Period enum test passed")
    }

    @Test
    fun `test Interval enum values`() {
        assertEquals("1m", Interval.ONE_MINUTE.value)
        assertEquals("2m", Interval.TWO_MINUTES.value)
        assertEquals("5m", Interval.FIVE_MINUTES.value)
        assertEquals("15m", Interval.FIFTEEN_MINUTES.value)
        assertEquals("30m", Interval.THIRTY_MINUTES.value)
        assertEquals("1h", Interval.ONE_HOUR.value)
        assertEquals("1d", Interval.ONE_DAY.value)
        assertEquals("1wk", Interval.ONE_WEEK.value)
        assertEquals("1mo", Interval.ONE_MONTH.value)

        println("✓ Interval enum test passed")
    }

    @Test
    fun `test Quote model`() {
        val quote = Quote(
            timestamp = 1609459200L, // 2021-01-01
            open = 100.0,
            high = 105.0,
            low = 99.0,
            close = 103.0,
            adjClose = 102.5,
            volume = 1000000L
        )

        assertEquals(1609459200L, quote.timestamp)
        assertEquals(100.0, quote.open)
        assertEquals(105.0, quote.high)
        assertEquals(99.0, quote.low)
        assertEquals(103.0, quote.close)

        println("✓ Quote model test passed")
    }

    @Test
    fun `test HistoricalData sorting`() {
        val quotes = listOf(
            Quote(timestamp = 3, close = 103.0),
            Quote(timestamp = 1, close = 101.0),
            Quote(timestamp = 2, close = 102.0)
        )

        val historicalData = HistoricalData(
            symbol = "AAPL",
            quotes = quotes,
            currency = "USD"
        )

        val sortedAsc = historicalData.getSortedQuotes()
        assertEquals(1L, sortedAsc[0].timestamp)
        assertEquals(2L, sortedAsc[1].timestamp)
        assertEquals(3L, sortedAsc[2].timestamp)

        val sortedDesc = historicalData.getSortedQuotesDesc()
        assertEquals(3L, sortedDesc[0].timestamp)
        assertEquals(2L, sortedDesc[1].timestamp)
        assertEquals(1L, sortedDesc[2].timestamp)

        println("✓ HistoricalData sorting test passed")
    }

    @Test
    fun `test HistoricalData filtering by range`() {
        val quotes = listOf(
            Quote(timestamp = 1, close = 101.0),
            Quote(timestamp = 2, close = 102.0),
            Quote(timestamp = 3, close = 103.0),
            Quote(timestamp = 4, close = 104.0),
            Quote(timestamp = 5, close = 105.0)
        )

        val historicalData = HistoricalData("AAPL", quotes)
        val filtered = historicalData.filterByRange(2, 4)

        assertEquals(3, filtered.size)
        assertEquals(2L, filtered[0].timestamp)
        assertEquals(4L, filtered[2].timestamp)

        println("✓ HistoricalData filtering test passed")
    }

    @Test
    fun `test Dividend model`() {
        val dividend = Dividend(
            date = 1609459200L,
            amount = 0.82
        )

        assertEquals(1609459200L, dividend.date)
        assertEquals(0.82, dividend.amount)

        println("✓ Dividend model test passed")
    }

    @Test
    fun `test DividendData aggregation`() {
        val dividends = listOf(
            Dividend(date = 3, amount = 0.80),
            Dividend(date = 1, amount = 0.82),
            Dividend(date = 2, amount = 0.85)
        )

        val dividendData = DividendData("AAPL", dividends)

        val sorted = dividendData.getSortedDividends()
        assertEquals(1L, sorted[0].date)

        val total = dividendData.getTotalAmount()
        assertEquals(2.47, total, 0.01)

        println("✓ DividendData test passed")
    }

    @Test
    fun `test Split model`() {
        val split = Split(
            date = 1609459200L,
            ratio = 4.0 // 4-for-1 split
        )

        assertTrue(split.isForwardSplit())
        assertFalse(split.isReverseSplit())

        val reverseSplit = Split(
            date = 1609459200L,
            ratio = 0.5 // 1-for-2 reverse split
        )

        assertFalse(reverseSplit.isForwardSplit())
        assertTrue(reverseSplit.isReverseSplit())

        println("✓ Split model test passed")
    }

    @Test
    fun `test Action model types`() {
        val dividendAction = Action(
            date = 1L,
            dividends = 0.82
        )
        assertTrue(dividendAction.isDividend())
        assertFalse(dividendAction.isSplit())
        assertFalse(dividendAction.isCapitalGains())

        val splitAction = Action(
            date = 1L,
            splits = 2.0
        )
        assertFalse(splitAction.isDividend())
        assertTrue(splitAction.isSplit())

        val capitalGainsAction = Action(
            date = 1L,
            capitalGains = 1.5
        )
        assertTrue(capitalGainsAction.isCapitalGains())

        println("✓ Action types test passed")
    }

    @Test
    fun `test ActionsData filtering`() {
        val actions = listOf(
            Action(date = 1, dividends = 0.82),
            Action(date = 2, splits = 2.0),
            Action(date = 3, dividends = 0.85),
            Action(date = 4, capitalGains = 1.0)
        )

        val actionsData = ActionsData("FUND", actions)

        assertEquals(2, actionsData.getDividends().size)
        assertEquals(1, actionsData.getSplits().size)
        assertEquals(1, actionsData.getCapitalGains().size)

        println("✓ ActionsData filtering test passed")
    }

    @Test
    fun `test RecommendationSummary calculations`() {
        val summary = RecommendationSummary(
            period = "0m",
            strongBuy = 5,
            buy = 10,
            hold = 8,
            sell = 2,
            strongSell = 0
        )

        assertEquals(25, summary.getTotal())

        val average = summary.getAverage()
        assertTrue(average > 1.0 && average < 3.0) // Should be between strong buy and hold

        println("✓ RecommendationSummary test passed")
    }

    @Test
    fun `test AnalystPriceTargets upside calculation`() {
        val targets = AnalystPriceTargets(
            symbol = "AAPL",
            current = 150.0,
            targetMean = 180.0
        )

        val upside = targets.getUpsideFromMean()
        assertEquals(20.0, upside, 0.1) // 20% upside

        println("✓ AnalystPriceTargets test passed")
    }

    @Test
    fun `test InsiderTransactionsData filtering`() {
        val transactions = listOf(
            InsiderTransaction(
                insider = "John Doe",
                transactionType = "Buy",
                sharesTraded = 1000
            ),
            InsiderTransaction(
                insider = "Jane Smith",
                transactionType = "Sale",
                sharesTraded = 500
            ),
            InsiderTransaction(
                insider = "Bob Johnson",
                transactionType = "Buy",
                sharesTraded = 2000
            )
        )

        val data = InsiderTransactionsData("AAPL", transactions)

        assertEquals(2, data.getPurchases().size)
        assertEquals(1, data.getSales().size)

        println("✓ InsiderTransactionsData test passed")
    }

    @Test
    fun `test OptionContract calculations`() {
        val option = OptionContract(
            contractSymbol = "AAPL210101C00150000",
            strike = 150.0,
            lastPrice = 5.0,
            bid = 4.90,
            ask = 5.10
        )

        val midPrice = option.getMidPrice()
        assertEquals(5.0, midPrice, 0.01)

        val callIntrinsic = option.getIntrinsicValueCall(160.0)
        assertEquals(10.0, callIntrinsic, 0.01)

        val putIntrinsic = option.getIntrinsicValuePut(140.0)
        assertEquals(10.0, putIntrinsic, 0.01)

        println("✓ OptionContract test passed")
    }

    @Test
    fun `test OptionChain strike methods`() {
        val calls = listOf(
            OptionContract("AAPL210101C00150000", strike = 150.0),
            OptionContract("AAPL210101C00155000", strike = 155.0),
            OptionContract("AAPL210101C00160000", strike = 160.0)
        )

        val puts = listOf(
            OptionContract("AAPL210101P00150000", strike = 150.0),
            OptionContract("AAPL210101P00155000", strike = 155.0)
        )

        val chain = OptionChain(
            expirationDate = 1609459200L,
            calls = calls,
            puts = puts,
            underlyingPrice = 157.0
        )

        val strikes = chain.getAllStrikes()
        assertEquals(3, strikes.size)
        assertTrue(strikes.contains(150.0))
        assertTrue(strikes.contains(155.0))
        assertTrue(strikes.contains(160.0))

        val atmStrike = chain.getATMStrike()
        assertEquals(155.0, atmStrike) // Closest to 157

        println("✓ OptionChain test passed")
    }

    @Test
    fun `test utility extension functions`() {
        val quotes = listOf(
            Quote(timestamp = 1, close = 100.0, high = 102.0, low = 98.0, volume = 1000),
            Quote(timestamp = 2, close = 105.0, high = 107.0, low = 103.0, volume = 1500),
            Quote(timestamp = 3, close = 103.0, high = 106.0, low = 101.0, volume = 1200)
        )

        val highest = quotes.getHighestPrice()
        assertEquals(107.0, highest?.high)

        val lowest = quotes.getLowestPrice()
        assertEquals(98.0, lowest?.low)

        val totalVolume = quotes.getTotalVolume()
        assertEquals(3700L, totalVolume)

        val avgVolume = quotes.getAverageVolume()
        assertEquals(1233.33, avgVolume, 0.1)

        println("✓ Utility functions test passed")
    }

    @Test
    fun `test percentage change calculation`() {
        val change = calculatePercentageChange(100.0, 110.0)
        assertEquals(10.0, change, 0.01)

        val negChange = calculatePercentageChange(100.0, 90.0)
        assertEquals(-10.0, negChange, 0.01)

        println("✓ Percentage change test passed")
    }

    @Test
    fun `test SMA calculation`() {
        val quotes = listOf(
            Quote(timestamp = 1, close = 100.0),
            Quote(timestamp = 2, close = 102.0),
            Quote(timestamp = 3, close = 104.0),
            Quote(timestamp = 4, close = 103.0),
            Quote(timestamp = 5, close = 105.0)
        )

        val sma3 = quotes.calculateSMA(3)
        assertEquals(3, sma3.size)

        // First SMA(3) = (100 + 102 + 104) / 3 = 102
        assertEquals(102.0, sma3[0].second, 0.01)

        println("✓ SMA calculation test passed")
    }
}
