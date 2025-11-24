package io.github.yfinance.client

import io.github.yfinance.model.*
import kotlinx.serialization.json.*

/**
 * Parsing functions to convert Yahoo Finance API responses to our data models
 */

internal fun parseIncomeStatement(symbol: String, jsonData: JsonObject, module: String): IncomeStatement {
    val statements = jsonData[module]?.jsonObject?.get("incomeStatementHistory")?.jsonArray
        ?: jsonData[module]?.jsonObject?.get("incomeStatementHistoryQuarterly")?.jsonArray
        ?: return IncomeStatement(symbol, emptyMap())

    val data = mutableMapOf<String, Map<String, Double?>>()

    statements.forEach { stmt ->
        val stmtObj = stmt.jsonObject
        val endDate = stmtObj["endDate"]?.jsonObject?.get("fmt")?.jsonPrimitive?.content ?: return@forEach

        val metrics = mutableMapOf<String, Double?>()
        stmtObj.entries.forEach { (key, value) ->
            if (value is JsonObject && value.containsKey("raw")) {
                val rawValue = value["raw"]?.jsonPrimitive?.doubleOrNull
                metrics[key] = rawValue
            }
        }

        data[endDate] = metrics
    }

    return IncomeStatement(symbol, data)
}

internal fun parseBalanceSheet(symbol: String, jsonData: JsonObject, module: String): BalanceSheet {
    val statements = jsonData[module]?.jsonObject?.get("balanceSheetStatements")?.jsonArray
        ?: jsonData[module]?.jsonObject?.get("balanceSheetStatementsQuarterly")?.jsonArray
        ?: return BalanceSheet(symbol, emptyMap())

    val data = mutableMapOf<String, Map<String, Double?>>()

    statements.forEach { stmt ->
        val stmtObj = stmt.jsonObject
        val endDate = stmtObj["endDate"]?.jsonObject?.get("fmt")?.jsonPrimitive?.content ?: return@forEach

        val metrics = mutableMapOf<String, Double?>()
        stmtObj.entries.forEach { (key, value) ->
            if (value is JsonObject && value.containsKey("raw")) {
                val rawValue = value["raw"]?.jsonPrimitive?.doubleOrNull
                metrics[key] = rawValue
            }
        }

        data[endDate] = metrics
    }

    return BalanceSheet(symbol, data)
}

internal fun parseCashFlow(symbol: String, jsonData: JsonObject, module: String): CashFlow {
    val statements = jsonData[module]?.jsonObject?.get("cashflowStatements")?.jsonArray
        ?: jsonData[module]?.jsonObject?.get("cashflowStatementsQuarterly")?.jsonArray
        ?: return CashFlow(symbol, emptyMap())

    val data = mutableMapOf<String, Map<String, Double?>>()

    statements.forEach { stmt ->
        val stmtObj = stmt.jsonObject
        val endDate = stmtObj["endDate"]?.jsonObject?.get("fmt")?.jsonPrimitive?.content ?: return@forEach

        val metrics = mutableMapOf<String, Double?>()
        stmtObj.entries.forEach { (key, value) ->
            if (value is JsonObject && value.containsKey("raw")) {
                val rawValue = value["raw"]?.jsonPrimitive?.doubleOrNull
                metrics[key] = rawValue
            }
        }

        data[endDate] = metrics
    }

    return CashFlow(symbol, data)
}

internal fun parseEarnings(symbol: String, jsonData: JsonObject, frequency: StatementFrequency): EarningsData {
    val earningsChart = jsonData["earnings"]?.jsonObject?.get("earningsChart")?.jsonObject
    val quarterlyEarnings = earningsChart?.get("quarterly")?.jsonArray

    val earnings = mutableListOf<Earnings>()

    quarterlyEarnings?.forEach { qe ->
        val qeObj = qe.jsonObject
        val date = qeObj["date"]?.jsonPrimitive?.content ?: return@forEach

        // Parse date string to epoch (simplified)
        val timestamp = try {
            date.toLong()
        } catch (e: Exception) {
            0L
        }

        val actual = qeObj["actual"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull
        val estimate = qeObj["estimate"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull

        earnings.add(
            Earnings(
                date = timestamp,
                epsActual = actual,
                epsEstimate = estimate,
                epsDifference = if (actual != null && estimate != null) actual - estimate else null,
                surprisePercent = if (actual != null && estimate != null && estimate != 0.0) {
                    ((actual - estimate) / estimate) * 100.0
                } else null
            )
        )
    }

    return EarningsData(symbol, earnings, frequency)
}

internal fun parseCalendar(symbol: String, jsonData: JsonObject): Calendar {
    val calendarEvents = jsonData["calendarEvents"]?.jsonObject

    return Calendar(
        symbol = symbol,
        earnings = calendarEvents?.get("earnings")?.jsonObject?.get("earningsDate")?.jsonArray
            ?.firstOrNull()?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
        exDividendDate = calendarEvents?.get("exDividendDate")?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
        dividendDate = calendarEvents?.get("dividendDate")?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
        earningsAverage = calendarEvents?.get("earnings")?.jsonObject?.get("earningsAverage")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.doubleOrNull,
        earningsLow = calendarEvents?.get("earnings")?.jsonObject?.get("earningsLow")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.doubleOrNull,
        earningsHigh = calendarEvents?.get("earnings")?.jsonObject?.get("earningsHigh")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.doubleOrNull,
        revenueAverage = calendarEvents?.get("earnings")?.jsonObject?.get("revenueAverage")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.longOrNull,
        revenueLow = calendarEvents?.get("earnings")?.jsonObject?.get("revenueLow")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.longOrNull,
        revenueHigh = calendarEvents?.get("earnings")?.jsonObject?.get("revenueHigh")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.longOrNull
    )
}

internal fun parseEarningsDates(symbol: String, jsonData: JsonObject, limit: Int): EarningsDatesData {
    // Simplified parsing
    return EarningsDatesData(symbol, emptyList())
}

internal fun parseMajorHolders(symbol: String, jsonData: JsonObject): MajorHoldersData {
    val breakdown = jsonData["majorHoldersBreakdown"]?.jsonObject

    return MajorHoldersData(
        symbol = symbol,
        insidersPercentHeld = breakdown?.get("insidersPercentHeld")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.doubleOrNull,
        institutionsPercentHeld = breakdown?.get("institutionsPercentHeld")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.doubleOrNull,
        institutionsFloatPercentHeld = breakdown?.get("institutionsFloatPercentHeld")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.doubleOrNull,
        institutionsCount = breakdown?.get("institutionsCount")?.jsonObject
            ?.get("raw")?.jsonPrimitive?.intOrNull
    )
}

internal fun parseInstitutionalHolders(symbol: String, jsonData: JsonObject): InstitutionalHoldersData {
    val ownership = jsonData["institutionOwnership"]?.jsonObject?.get("ownershipList")?.jsonArray

    val holders = mutableListOf<InstitutionalHolder>()

    ownership?.forEach { owner ->
        val ownerObj = owner.jsonObject

        holders.add(
            InstitutionalHolder(
                holder = ownerObj["organization"]?.jsonPrimitive?.content ?: "",
                shares = ownerObj["position"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull ?: 0L,
                dateReported = ownerObj["reportDate"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
                percentOut = ownerObj["pctHeld"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
                value = ownerObj["value"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull
            )
        )
    }

    return InstitutionalHoldersData(symbol, holders)
}

internal fun parseMutualFundHolders(symbol: String, jsonData: JsonObject): MutualFundHoldersData {
    val ownership = jsonData["fundOwnership"]?.jsonObject?.get("ownershipList")?.jsonArray

    val holders = mutableListOf<MutualFundHolder>()

    ownership?.forEach { owner ->
        val ownerObj = owner.jsonObject

        holders.add(
            MutualFundHolder(
                holder = ownerObj["organization"]?.jsonPrimitive?.content ?: "",
                shares = ownerObj["position"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull ?: 0L,
                dateReported = ownerObj["reportDate"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
                percentOut = ownerObj["pctHeld"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
                value = ownerObj["value"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull
            )
        )
    }

    return MutualFundHoldersData(symbol, holders)
}

internal fun parseInsiderTransactions(symbol: String, jsonData: JsonObject): InsiderTransactionsData {
    val transactions = jsonData["insiderTransactions"]?.jsonObject?.get("transactions")?.jsonArray

    val txList = mutableListOf<InsiderTransaction>()

    transactions?.forEach { tx ->
        val txObj = tx.jsonObject

        txList.add(
            InsiderTransaction(
                insider = txObj["filerName"]?.jsonPrimitive?.content ?: "",
                relation = txObj["filerRelation"]?.jsonPrimitive?.content,
                lastDate = txObj["startDate"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
                transactionType = txObj["transactionText"]?.jsonPrimitive?.content,
                ownerType = txObj["ownershipNature"]?.jsonPrimitive?.content,
                sharesTraded = txObj["shares"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
                value = txObj["value"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull
            )
        )
    }

    return InsiderTransactionsData(symbol, txList)
}

internal fun parseInsiderRosterHolders(symbol: String, jsonData: JsonObject): InsiderRosterHoldersData {
    val holders = jsonData["insiderHolders"]?.jsonObject?.get("holders")?.jsonArray

    val holderList = mutableListOf<InsiderRosterHolder>()

    holders?.forEach { holder ->
        val holderObj = holder.jsonObject

        holderList.add(
            InsiderRosterHolder(
                name = holderObj["name"]?.jsonPrimitive?.content ?: "",
                position = holderObj["relation"]?.jsonPrimitive?.content,
                url = holderObj["url"]?.jsonPrimitive?.content,
                recentTransaction = holderObj["latestTransDate"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
                positionDirectDate = holderObj["positionDirectDate"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
                shares = holderObj["positionDirect"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull
            )
        )
    }

    return InsiderRosterHoldersData(symbol, holderList)
}

internal fun parseRecommendations(symbol: String, jsonData: JsonObject): RecommendationsData {
    val history = jsonData["upgradeDowngradeHistory"]?.jsonObject?.get("history")?.jsonArray

    val recommendations = mutableListOf<Recommendation>()

    history?.forEach { rec ->
        val recObj = rec.jsonObject

        recommendations.add(
            Recommendation(
                date = recObj["epochGradeDate"]?.jsonPrimitive?.long ?: 0L,
                firm = recObj["firm"]?.jsonPrimitive?.content ?: "",
                toGrade = recObj["toGrade"]?.jsonPrimitive?.contentOrNull,
                fromGrade = recObj["fromGrade"]?.jsonPrimitive?.contentOrNull,
                action = recObj["action"]?.jsonPrimitive?.contentOrNull
            )
        )
    }

    return RecommendationsData(symbol, recommendations)
}

internal fun parseRecommendationsSummary(symbol: String, jsonData: JsonObject): RecommendationsSummaryData {
    val trend = jsonData["recommendationTrend"]?.jsonObject?.get("trend")?.jsonArray

    val summaries = mutableListOf<RecommendationSummary>()

    trend?.forEach { period ->
        val periodObj = period.jsonObject

        summaries.add(
            RecommendationSummary(
                period = periodObj["period"]?.jsonPrimitive?.content ?: "",
                strongBuy = periodObj["strongBuy"]?.jsonPrimitive?.intOrNull ?: 0,
                buy = periodObj["buy"]?.jsonPrimitive?.intOrNull ?: 0,
                hold = periodObj["hold"]?.jsonPrimitive?.intOrNull ?: 0,
                sell = periodObj["sell"]?.jsonPrimitive?.intOrNull ?: 0,
                strongSell = periodObj["strongSell"]?.jsonPrimitive?.intOrNull ?: 0
            )
        )
    }

    return RecommendationsSummaryData(symbol, summaries)
}

internal fun parseAnalystPriceTargets(symbol: String, jsonData: JsonObject): AnalystPriceTargets {
    val financialData = jsonData["financialData"]?.jsonObject

    return AnalystPriceTargets(
        symbol = symbol,
        current = financialData?.get("currentPrice")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
        targetHigh = financialData?.get("targetHighPrice")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
        targetLow = financialData?.get("targetLowPrice")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
        targetMean = financialData?.get("targetMeanPrice")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
        targetMedian = financialData?.get("targetMedianPrice")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull
    )
}

internal fun parseSustainability(symbol: String, jsonData: JsonObject): Sustainability {
    val esg = jsonData["esgScores"]?.jsonObject

    return Sustainability(
        symbol = symbol,
        environmentScore = esg?.get("environmentScore")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
        socialScore = esg?.get("socialScore")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
        governanceScore = esg?.get("governanceScore")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
        totalEsg = esg?.get("totalEsg")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
        percentile = esg?.get("percentile")?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
        esgPerformance = esg?.get("esgPerformance")?.jsonPrimitive?.contentOrNull,
        peerGroup = esg?.get("peerGroup")?.jsonPrimitive?.contentOrNull,
        highestControversy = esg?.get("highestControversy")?.jsonPrimitive?.intOrNull,
        peerCount = esg?.get("peerCount")?.jsonPrimitive?.intOrNull
    )
}

internal fun parseSECFilings(symbol: String, jsonData: JsonObject): SECFilings {
    val secFilings = jsonData["secFilings"]?.jsonObject?.get("filings")?.jsonArray

    val filings = mutableListOf<SECFiling>()

    secFilings?.forEach { filing ->
        val filingObj = filing.jsonObject

        filings.add(
            SECFiling(
                date = filingObj["date"]?.jsonPrimitive?.long ?: 0L,
                epochDate = filingObj["epochDate"]?.jsonPrimitive?.long ?: 0L,
                type = filingObj["type"]?.jsonPrimitive?.content ?: "",
                title = filingObj["title"]?.jsonPrimitive?.content ?: "",
                edgarUrl = filingObj["edgarUrl"]?.jsonPrimitive?.content ?: ""
            )
        )
    }

    return SECFilings(symbol, filings)
}

internal fun parseShares(symbol: String, jsonData: JsonObject): SharesData {
    val keyStats = jsonData["defaultKeyStatistics"]?.jsonObject
    val sharesOutstanding = keyStats?.get("sharesOutstanding")?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull

    val data = if (sharesOutstanding != null) {
        mapOf(System.currentTimeMillis() / 1000 to sharesOutstanding)
    } else {
        emptyMap()
    }

    return SharesData(symbol, data)
}

internal fun parseOptionChain(jsonResponse: JsonObject): YFinanceResult<OptionChain> {
    val optionChain = jsonResponse["optionChain"]?.jsonObject
    val result = optionChain?.get("result")?.jsonArray?.firstOrNull()?.jsonObject

    if (result == null) {
        return YFinanceResult.Error("No option chain data")
    }

    val options = result["options"]?.jsonArray?.firstOrNull()?.jsonObject

    val calls = options?.get("calls")?.jsonArray?.map { call ->
        val callObj = call.jsonObject
        OptionContract(
            contractSymbol = callObj["contractSymbol"]?.jsonPrimitive?.content ?: "",
            strike = callObj["strike"]?.jsonObject?.get("raw")?.jsonPrimitive?.double ?: 0.0,
            lastPrice = callObj["lastPrice"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            bid = callObj["bid"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            ask = callObj["ask"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            change = callObj["change"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            percentChange = callObj["percentChange"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            volume = callObj["volume"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
            openInterest = callObj["openInterest"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
            impliedVolatility = callObj["impliedVolatility"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            inTheMoney = callObj["inTheMoney"]?.jsonPrimitive?.boolean ?: false,
            lastTradeDate = callObj["lastTradeDate"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
            contractSize = callObj["contractSize"]?.jsonPrimitive?.content ?: "REGULAR",
            currency = callObj["currency"]?.jsonPrimitive?.content ?: "USD"
        )
    } ?: emptyList()

    val puts = options?.get("puts")?.jsonArray?.map { put ->
        val putObj = put.jsonObject
        OptionContract(
            contractSymbol = putObj["contractSymbol"]?.jsonPrimitive?.content ?: "",
            strike = putObj["strike"]?.jsonObject?.get("raw")?.jsonPrimitive?.double ?: 0.0,
            lastPrice = putObj["lastPrice"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            bid = putObj["bid"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            ask = putObj["ask"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            change = putObj["change"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            percentChange = putObj["percentChange"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            volume = putObj["volume"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
            openInterest = putObj["openInterest"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
            impliedVolatility = putObj["impliedVolatility"]?.jsonObject?.get("raw")?.jsonPrimitive?.doubleOrNull,
            inTheMoney = putObj["inTheMoney"]?.jsonPrimitive?.boolean ?: false,
            lastTradeDate = putObj["lastTradeDate"]?.jsonObject?.get("raw")?.jsonPrimitive?.longOrNull,
            contractSize = putObj["contractSize"]?.jsonPrimitive?.content ?: "REGULAR",
            currency = putObj["currency"]?.jsonPrimitive?.content ?: "USD"
        )
    } ?: emptyList()

    val expirationDate = options?.get("expirationDate")?.jsonPrimitive?.long ?: 0L
    val underlyingPrice = result["quote"]?.jsonObject?.get("regularMarketPrice")?.jsonObject
        ?.get("raw")?.jsonPrimitive?.doubleOrNull
    val underlyingSymbol = result["underlyingSymbol"]?.jsonPrimitive?.content

    return YFinanceResult.Success(
        OptionChain(
            expirationDate = expirationDate,
            calls = calls,
            puts = puts,
            underlyingPrice = underlyingPrice,
            underlyingSymbol = underlyingSymbol
        )
    )
}

internal fun parseNews(symbol: String, jsonResponse: JsonObject): YFinanceResult<NewsData> {
    val news = jsonResponse["news"]?.jsonArray

    val articles = news?.map { newsItem ->
        val item = newsItem.jsonObject
        NewsArticle(
            title = item["title"]?.jsonPrimitive?.content ?: "",
            publisher = item["publisher"]?.jsonPrimitive?.content ?: "",
            link = item["link"]?.jsonPrimitive?.content ?: "",
            publishTime = item["providerPublishTime"]?.jsonPrimitive?.long ?: 0L,
            type = item["type"]?.jsonPrimitive?.contentOrNull,
            thumbnail = item["thumbnail"]?.jsonObject?.get("resolutions")?.jsonArray
                ?.firstOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull,
            relatedTickers = item["relatedTickers"]?.jsonArray?.map {
                it.jsonPrimitive.content
            } ?: emptyList()
        )
    } ?: emptyList()

    return YFinanceResult.Success(NewsData(symbol, articles))
}
