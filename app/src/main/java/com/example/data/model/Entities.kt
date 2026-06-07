package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val url: String = "",
    val imageUrl: String = "",
    val publishedAt: String = "",
    val summary: String = "",
    val sentiment: String = "", // "POSITIVE", "NEUTRAL", "NEGATIVE"
    val score: Double = 0.5,     // numerical sentiment intensity
    val isFavorite: Boolean = false,
    val savedAt: Long = 0L
)

@Entity(tableName = "digests")
data class DigestEntity(
    @PrimaryKey
    val date: String, // e.g., "2026-06-07"
    val overallSummary: String,
    val generatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey
    val id: String,
    val sender: String,
    val senderEmail: String,
    val subject: String,
    val timestamp: Long, // ms since epoch
    val snippet: String,
    val body: String,
    val isRead: Boolean = false
)

@Entity(tableName = "feedbacks")
data class FeedbackEntity(
    @PrimaryKey
    val articleId: String,
    val score: Int, // Star rating 1 to 5
    val isThumbsUp: Boolean, // True for thumbs up, False for thumbs down
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "youtube_videos")
data class YouTubeVideoEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val channelTitle: String,
    val publishedAt: String, // ISO
    val timestamp: Long, // Milliseconds Since Epoch
    val videoUrl: String,
    val isRead: Boolean = false
)
