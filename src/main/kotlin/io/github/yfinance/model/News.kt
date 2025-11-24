package io.github.yfinance.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a news article
 *
 * @property title Article title
 * @property publisher Publisher name
 * @property link Article URL
 * @property publishTime Publish time (epoch seconds)
 * @property type Article type (e.g., "STORY")
 * @property thumbnail Thumbnail image URL
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
     * Get publish time as Instant
     */
    fun getPublishInstant(): Instant = Instant.fromEpochSeconds(publishTime)
}

/**
 * Collection of news articles
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
    fun getSortedArticles(): List<NewsArticle> =
        articles.sortedByDescending { it.publishTime }

    /**
     * Get articles from specific publisher
     */
    fun getByPublisher(publisher: String): List<NewsArticle> =
        articles.filter { it.publisher.equals(publisher, ignoreCase = true) }
}
