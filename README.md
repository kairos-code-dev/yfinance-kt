# YFinance-KT

[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Kotlin library for fetching financial data from Yahoo Finance. This is a modern, idiomatic Kotlin implementation inspired by the popular Python library [yfinance](https://github.com/ranaroussi/yfinance).

## Features

- 📈 **Historical Price Data** - Fetch OHLCV data for any period and interval
- 📊 **Company Information** - Get comprehensive ticker information including fundamentals
- 💰 **Dividends & Splits** - Access dividend and stock split history
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
