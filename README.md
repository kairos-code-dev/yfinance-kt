# YFinance-KT

[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Kotlin library for fetching financial data from Yahoo Finance. This is a modern, idiomatic Kotlin implementation inspired by the popular Python library [yfinance](https://github.com/ranaroussi/yfinance).

## Features

### Core Data
- 📈 **Historical Price Data** - Fetch OHLCV data for any period and interval
- 📊 **Company Information** - Get comprehensive ticker information including fundamentals
- 💰 **Dividends & Splits** - Access dividend and stock split history
- 📅 **Corporate Actions** - Combined view of dividends and splits
- 📆 **Calendar Events** - Upcoming earnings dates and dividend schedules

### Financial Statements
- 📄 **Income Statements** - Annual, quarterly, and trailing income statements
- 💼 **Balance Sheets** - Complete balance sheet data
- 💵 **Cash Flow Statements** - Operating, investing, and financing cash flows

### Analyst Coverage & Ownership
- 📊 **Earnings Data** - Historical earnings, EPS estimates, and actuals
- 🎯 **Analyst Recommendations** - Upgrades, downgrades, and price targets
- 👥 **Institutional Holdings** - Major holders and institutional ownership
- 📰 **News Articles** - Latest news and market updates

### Options & Derivatives
- 📉 **Options Data** - Available expiration dates and option chains
- 🎲 **Option Contracts** - Calls, puts, Greeks, and open interest
- 💹 **Fast Info** - Quick access to key market data

### ESG & Additional Data
- 🌱 **Sustainability** - ESG scores (Environmental, Social, Governance)
- 💵 **Capital Gains** - Distribution history for funds
- 📊 **Shares Outstanding** - Share count over time

### Technical Features
- 🚀 **Kotlin Coroutines** - Fully async/await support with coroutines
- 🔒 **Type-Safe** - Strongly typed data models with sealed classes for error handling
- 🎯 **DSL-Style API** - Clean and intuitive Kotlin DSL
- 🌐 **Ktor Client** - Modern HTTP client with connection pooling and retry logic
- ⚡ **Lightweight** - Minimal dependencies, fast and efficient

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.yfinance:yfinance-kt:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.yfinance:yfinance-kt:1.0.0'
}
```

## Quick Start

```kotlin
import io.github.yfinance.Ticker
import io.github.yfinance.model.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Create a ticker instance
    val ticker = Ticker("AAPL")

    // Get historical data
    ticker.history(period = Period.ONE_MONTH).onSuccess { data ->
        println("Symbol: ${data.symbol}")
        println("Latest close: ${data.quotes.last().close}")
    }

    // Get company info
    ticker.info().onSuccess { info ->
        println("Company: ${info.longName}")
        println("Sector: ${info.sector}")
        println("Market Cap: ${info.marketCap}")
    }
}
```

## Usage Examples

### Historical Data

```kotlin
val ticker = Ticker("AAPL")

// Get 1 year of daily data
val result = ticker.history(
    period = Period.ONE_YEAR,
    interval = Interval.ONE_DAY
)

result.onSuccess { data ->
    data.quotes.forEach { quote ->
        println("Date: ${quote.getInstant()}")
        println("Open: ${quote.open}, Close: ${quote.close}")
        println("Volume: ${quote.volume}")
    }
}
```

### Available Periods

- `Period.ONE_DAY`, `Period.FIVE_DAYS`
- `Period.ONE_MONTH`, `Period.THREE_MONTHS`, `Period.SIX_MONTHS`
- `Period.ONE_YEAR`, `Period.TWO_YEARS`, `Period.FIVE_YEARS`, `Period.TEN_YEARS`
- `Period.YEAR_TO_DATE`, `Period.MAX`

### Available Intervals

- `Interval.ONE_MINUTE`, `Interval.TWO_MINUTES`, `Interval.FIVE_MINUTES`, `Interval.FIFTEEN_MINUTES`
- `Interval.THIRTY_MINUTES`, `Interval.SIXTY_MINUTES`, `Interval.NINETY_MINUTES`
- `Interval.ONE_HOUR`, `Interval.ONE_DAY`, `Interval.FIVE_DAYS`, `Interval.ONE_WEEK`
- `Interval.ONE_MONTH`, `Interval.THREE_MONTHS`

### Company Information

```kotlin
val ticker = Ticker("TSLA")

ticker.info().onSuccess { info ->
    println("Company: ${info.longName}")
    println("Sector: ${info.sector}")
    println("Industry: ${info.industry}")
    println("Website: ${info.website}")
    println("CEO: ${info.ceo}")
    println("Employees: ${info.employees}")
    println("Market Cap: ${info.marketCap}")
    println("P/E Ratio: ${info.trailingPE}")
    println("52W High: ${info.fiftyTwoWeekHigh}")
    println("52W Low: ${info.fiftyTwoWeekLow}")
}
```

### Dividends

```kotlin
val ticker = Ticker("AAPL")

ticker.dividends(Period.FIVE_YEARS).onSuccess { dividendData ->
    println("Total dividends: ${dividendData.dividends.size}")
    println("Total amount: ${dividendData.getTotalAmount()}")

    dividendData.getSortedDividends().forEach { dividend ->
        println("${dividend.getInstant()}: $${dividend.amount}")
    }
}
```

### Stock Splits

```kotlin
val ticker = Ticker("AAPL")

ticker.splits().onSuccess { splitData ->
    splitData.getSortedSplits().forEach { split ->
        val type = if (split.isForwardSplit()) "Forward" else "Reverse"
        println("${split.getInstant()}: $type split ${split.ratio}")
    }
}
```

### Corporate Actions (Combined Dividends & Splits)

```kotlin
val ticker = Ticker("AAPL")

ticker.actions(Period.ONE_YEAR).onSuccess { actionData ->
    // Get all actions sorted by date
    val allActions = actionData.getAllActions()

    allActions.forEach { action ->
        when (action) {
            is Action.DividendAction ->
                println("${action.getInstant()}: Dividend $${action.amount}")
            is Action.SplitAction ->
                println("${action.getInstant()}: Split ${action.ratio}")
        }
    }

    // Or get them separately
    val dividends = actionData.getDividendActions()
    val splits = actionData.getSplitActions()
    println("Total: ${dividends.size} dividends, ${splits.size} splits")
}
```

### Calendar Events

```kotlin
val ticker = Ticker("AAPL")

ticker.calendar().onSuccess { calendar ->
    if (calendar.hasEarnings()) {
        println("Next earnings: ${calendar.getEarningsInstant()}")
    }

    if (calendar.hasDividendInfo()) {
        println("Ex-dividend date: ${calendar.getExDividendInstant()}")
        println("Dividend payment date: ${calendar.getDividendInstant()}")
    }
}
```

### History by Date Range

```kotlin
val ticker = Ticker("AAPL")

// Get data for a specific date range
val startTime = System.currentTimeMillis() / 1000 - (60 * 24 * 60 * 60) // 60 days ago
val endTime = System.currentTimeMillis() / 1000

ticker.historyByRange(startTime, endTime, Interval.ONE_DAY).onSuccess { data ->
    println("Got ${data.quotes.size} quotes for the specified range")

    // Helper methods for data manipulation
    val sortedQuotes = data.getSortedQuotes() // Oldest first
    val sortedDesc = data.getSortedQuotesDesc() // Newest first
}
```

### Financial Statements

```kotlin
val ticker = Ticker("AAPL")

// Get annual income statement
ticker.incomeStatement(Frequency.ANNUAL).onSuccess { statement ->
    val latest = statement.getLatestData()
    if (latest != null) {
        println("Period: ${latest.first}")
        println("Total Revenue: ${latest.second["totalRevenue"]}")
        println("Net Income: ${latest.second["netIncome"]}")
        println("Gross Profit: ${latest.second["grossProfit"]}")
    }
}

// Get quarterly balance sheet
ticker.balanceSheet(Frequency.QUARTERLY).onSuccess { balanceSheet ->
    val periods = balanceSheet.getAvailablePeriods()
    println("Available periods: $periods")

    // Get specific line item across all periods
    val totalAssets = balanceSheet.getLineItem("totalAssets")
    totalAssets.forEach { (period, value) ->
        println("$period: Total Assets = $$value")
    }
}

// Get cash flow statement
ticker.cashFlow(Frequency.ANNUAL).onSuccess { cashFlow ->
    val latest = cashFlow.getLatestData()
    if (latest != null) {
        println("Operating Cash Flow: ${latest.second["operatingCashFlow"]}")
        println("Free Cash Flow: ${latest.second["freeCashFlow"]}")
    }
}
```

### Earnings Data

```kotlin
val ticker = Ticker("AAPL")

// Get full earnings data
ticker.earnings().onSuccess { earnings ->
    println("Current Quarter Estimate: ${earnings.currentQuarterEstimate}")

    // Latest quarterly earnings
    val latest = earnings.getLatestEarnings()
    println("Latest EPS: ${latest?.actual} (estimate: ${latest?.estimate})")

    // Revenue growth year-over-year
    val revenueGrowth = earnings.getYearlyRevenueGrowth()
    revenueGrowth.forEach { (year, growth) ->
        println("$year: ${String.format("%.2f", growth)}% growth")
    }
}

// Get earnings history
ticker.earningsHistory().onSuccess { history ->
    val sorted = history.getSortedHistory()
    sorted.take(4).forEach { item ->
        val beat = if (item.beatEstimates()) "BEAT" else "MISS"
        println("${item.quarter}: EPS ${item.epsActual} ($beat)")
    }

    println("Total beats: ${history.getBeatsCount()}")
    println("Total misses: ${history.getMissesCount()}")
}
```

### Analyst Recommendations & Holdings

```kotlin
val ticker = Ticker("AAPL")

// Get analyst recommendations
ticker.recommendations().onSuccess { recommendations ->
    val sorted = recommendations.getSortedRecommendations()
    sorted.take(5).forEach { rec ->
        println("${rec.firm}: ${rec.toGrade} (${rec.action})")
    }

    val summary = recommendations.getSummary()
    println("Recommendation summary: $summary")
}

// Get major holders
ticker.majorHolders().onSuccess { holders ->
    println("Insiders: ${holders.insidersPercent}%")
    println("Institutions: ${holders.institutionsPercent}%")
    println("Number of institutions: ${holders.institutionsCount}")
}

// Get institutional holders
ticker.institutionalHolders().onSuccess { institutions ->
    val topHolders = institutions.getTopHolders(10)
    topHolders.forEach { holder ->
        println("${holder.organization}: ${holder.percentHeld}%")
    }

    val total = institutions.getTotalPercentageHeld()
    println("Total institutional ownership: $total%")
}
```

### Options Data

```kotlin
val ticker = Ticker("AAPL")

// Get available option expiration dates
ticker.options().onSuccess { expirations ->
    println("Available expirations: ${expirations.size}")
    expirations.take(5).forEach { exp ->
        val date = Instant.fromEpochSeconds(exp)
        println("  $date")
    }
}

// Get option chain for specific expiration
val expiration = expirations.first()
ticker.optionChain(expiration).onSuccess { chain ->
    println("Calls: ${chain.calls.size}, Puts: ${chain.puts.size}")

    // Get all available strikes
    val strikes = chain.getAllStrikes()

    // Get in-the-money options
    val itmCalls = chain.getInTheMoneyCall()
    val itmPuts = chain.getInTheMoneyPuts()

    // Get specific strike
    val call = chain.getCall(150.0)
    val put = chain.getPut(150.0)

    // Analyze option contract
    call?.let {
        println("Call at $150:")
        println("  Last Price: ${it.lastPrice}")
        println("  Volume: ${it.volume}")
        println("  Open Interest: ${it.openInterest}")
        println("  Implied Volatility: ${it.impliedVolatility}")
        println("  Bid-Ask Spread: ${it.getBidAskSpread()}")
    }
}
```

### Fast Info

```kotlin
val ticker = Ticker("AAPL")

ticker.fastInfo().onSuccess { info ->
    println("Last Price: ${info.lastPrice}")
    println("Market Cap: ${info.marketCap}")
    println("Volume: ${info.volume}")
    println("52W High/Low: ${info.fiftyTwoWeekHigh}/${info.fiftyTwoWeekLow}")

    // Helper methods
    val dayRange = info.getDayRange()
    val priceChange = info.getPriceChange()
    val percentChange = info.getPercentChange()

    println("Day Range: ${dayRange?.first} - ${dayRange?.second}")
    println("Change: $priceChange (${percentChange}%)")
}
```

### Sustainability/ESG

```kotlin
val ticker = Ticker("AAPL")

ticker.sustainability().onSuccess { esg ->
    println("Total ESG Score: ${esg.totalEsg}")
    println("Environment: ${esg.environmentScore}")
    println("Social: ${esg.socialScore}")
    println("Governance: ${esg.governanceScore}")
    println("Controversy Level: ${esg.controversyLevel}")
    println("ESG Performance: ${esg.esgPerformance}")

    // Helper methods
    println("Has High Controversy: ${esg.hasHighControversy()}")
    println("Rating Category: ${esg.getRatingCategory()}")
}
```

### Capital Gains & Shares

```kotlin
val ticker = Ticker("SPY")

// Get capital gains distributions (for funds/ETFs)
ticker.capitalGains().onSuccess { gains ->
    println("Total distributions: ${gains.gains.size}")
    gains.getSortedGains().forEach { gain ->
        println("${gain.getInstant()}: ${gain.amount}")
    }
    println("Total amount: ${gains.getTotalAmount()}")
}

// Get shares outstanding
ticker.shares().onSuccess { shares ->
    val latest = shares.getLatestShares()
    println("Latest shares outstanding: $latest")

    // Get share growth over time
    val growth = shares.getShareGrowth()
    growth.forEach { (timestamp, percent) ->
        println("${Instant.fromEpochSeconds(timestamp)}: ${percent}%")
    }
}
```

### DSL-Style Usage

```kotlin
// Concise DSL style
ticker("AAPL") {
    history(Period.ONE_WEEK, Interval.ONE_DAY)
}.onSuccess { data ->
    println("Latest price: ${data.quotes.last().close}")
}.onError { error ->
    println("Error: ${error.message}")
}
```

### Error Handling

```kotlin
val result = ticker.history(Period.ONE_MONTH)

when (result) {
    is YFinanceResult.Success -> {
        // Handle success
        val data = result.data
        println("Got ${data.quotes.size} quotes")
    }
    is YFinanceResult.Error -> {
        // Handle error
        println("Error: ${result.message}")
        println("Error type: ${result.errorType}")
        result.cause?.printStackTrace()
    }
}

// Or use functional style
result
    .onSuccess { data ->
        println("Success: ${data.quotes.size} quotes")
    }
    .onError { error ->
        println("Error: ${error.message}")
    }
```

### Utility Functions

```kotlin
import io.github.yfinance.utils.*

// Calculate SMA
val quotes = data.quotes
val sma20 = quotes.calculateSMA(20)

// Get statistics
val highest = quotes.getHighestPrice()
val lowest = quotes.getLowestPrice()
val avgVolume = quotes.getAverageVolume()

// Date conversions
val timestamp = 1672531200L
val dateTime = timestamp.toLocalDateTime()
```

## Architecture

YFinance-KT is built with modern Kotlin best practices:

- **Data Classes**: Immutable data models for all financial data
- **Sealed Classes**: Type-safe error handling with `YFinanceResult`
- **Coroutines**: All API calls are suspend functions
- **Ktor Client**: Modern, async HTTP client with connection pooling
- **kotlinx.serialization**: Fast JSON parsing
- **kotlinx.datetime**: Modern date/time handling

### Project Structure

```
src/main/kotlin/io/github/yfinance/
├── Ticker.kt                    # Main API class
├── YFinance.kt                  # Library entry point
├── model/                       # Data models
│   ├── Period.kt
│   ├── Interval.kt
│   ├── Quote.kt
│   ├── HistoricalData.kt
│   ├── TickerInfo.kt
│   ├── Dividend.kt
│   ├── Split.kt
│   ├── Action.kt               # Corporate actions (NEW)
│   ├── Calendar.kt             # Calendar events (NEW)
│   ├── Financial.kt            # Financial statements (NEW)
│   ├── News.kt                 # News articles (NEW)
│   ├── Recommendation.kt       # Analyst recommendations (NEW)
│   └── YFinanceResult.kt
├── client/                      # HTTP client
│   ├── YFinanceClient.kt
│   └── YFinanceApiModels.kt
└── utils/                       # Utilities
    └── Extensions.kt
```

## Requirements

- Kotlin 2.0.21 or higher
- JVM 17 or higher
- Coroutines support

## Dependencies

- Ktor Client 2.3.12
- kotlinx.serialization 1.7.3
- kotlinx.coroutines 1.9.0
- kotlinx.datetime 0.6.1
- kotlin-logging 7.0.0

## Disclaimer

This library is **not affiliated, endorsed, or vetted by Yahoo, Inc.** It's an open-source tool that uses Yahoo's publicly available APIs and is intended for research and educational purposes only.

Use at your own risk. Yahoo Finance API is not officially documented and may change without notice.

## License

Apache License 2.0 - see [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- Inspired by [yfinance](https://github.com/ranaroussi/yfinance) by Ran Aroussi
- Built with ❤️ using Kotlin and Ktor

## Examples

See the [examples](src/main/kotlin/io/github/yfinance/examples) directory for more usage examples.

## Support

For issues, questions, or contributions, please visit the [GitHub repository](https://github.com/yourusername/yfinance-kt).
