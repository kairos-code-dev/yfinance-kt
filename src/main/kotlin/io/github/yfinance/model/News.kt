package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a news article
 *
 * @property title The title of the article
 * @property publisher The publisher name
 * @property link The URL to the article
 * @property publishTime The publication timestamp
 * @property type The type of content (e.g., "STORY", "VIDEO")
 * @property thumbnail Optional thumbnail URL
 * @property relatedTickers Related ticker symbols
 */
@Serializable
data class NewsArticle(
    val title: String,
    val publisher: String,
    val link: String,
    val publishTime: Long,
    val type: String? = null,
    val thumbnail: String? = null,
    val relatedTickers: List<String> = emptyList()
) {
    /**
     * Get the publish time as an Instant
     */
    fun getPublishInstant(): Instant = Instant.fromEpochSeconds(publishTime)
}

/**
 * Represents a collection of news articles for a ticker
 *
 * @property symbol The ticker symbol
 * @property articles List of news articles
 */
@Serializable
data class NewsData(
    val symbol: String,
    val articles: List<NewsArticle>
) {
    /**
     * Get articles sorted by publish time (newest first)
     */
    fun getSortedArticles(): List<NewsArticle> = articles.sortedByDescending { it.publishTime }

    /**
     * Filter articles by publisher
     */
    fun filterByPublisher(publisher: String): List<NewsArticle> =
        articles.filter { it.publisher.equals(publisher, ignoreCase = true) }

    /**
     * Filter articles by date range
     */
    fun filterByDateRange(startTimestamp: Long, endTimestamp: Long): List<NewsArticle> =
        articles.filter { it.publishTime in startTimestamp..endTimestamp }
}
