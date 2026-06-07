package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.database.ArticleDao
import com.example.data.model.ArticleEntity
import com.example.data.model.DigestEntity
import com.example.data.model.EmailEntity
import com.example.data.model.FeedbackEntity
import com.example.data.model.YouTubeVideoEntity
import com.example.data.network.Content
import com.example.data.network.GenerateContentRequest
import com.example.data.network.GenerationConfig
import com.example.data.network.Part
import com.example.data.network.RetrofitClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NewsRepository(private val articleDao: ArticleDao) {

    val allArticles: Flow<List<ArticleEntity>> = articleDao.getAllArticles()
    val favoriteArticles: Flow<List<ArticleEntity>> = articleDao.getFavoriteArticles()
    val allFeedbacks: Flow<List<FeedbackEntity>> = articleDao.getAllFeedbacks()

    fun getDigestForDate(date: String): Flow<DigestEntity?> = articleDao.getDigestByDate(date)

    fun getEmailsSince(timestamp: Long): Flow<List<EmailEntity>> = articleDao.getEmailsSince(timestamp)

    fun getYouTubeVideosSince(timestamp: Long): Flow<List<YouTubeVideoEntity>> = articleDao.getYouTubeVideosSince(timestamp)

    suspend fun setFavorite(id: String, isFavorite: Boolean) {
        val savedTime = if (isFavorite) System.currentTimeMillis() else 0L
        articleDao.setFavoriteState(id, isFavorite, savedTime)
    }
    
    suspend fun markEmailAsRead(id: String) {
        articleDao.setEmailReadState(id, true)
    }

    suspend fun markYouTubeVideoAsRead(id: String) {
        articleDao.setYouTubeVideoReadState(id, true)
    }

    suspend fun saveFeedback(articleId: String, score: Int, isThumbsUp: Boolean, comment: String = "") {
        val feedback = FeedbackEntity(
            articleId = articleId,
            score = score,
            isThumbsUp = isThumbsUp,
            comment = comment,
            timestamp = System.currentTimeMillis()
        )
        articleDao.insertFeedback(feedback)
    }

    suspend fun getFeedbackForArticle(articleId: String): FeedbackEntity? = withContext(Dispatchers.IO) {
        articleDao.getFeedbackByArticleId(articleId)
    }

    suspend fun refreshDigest(
        dateStr: String,
        preferredTopics: List<String> = emptyList(),
        preferredSources: List<String> = emptyList()
    ): Result<DigestEntity> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(Exception("Gemini API key is not configured. Please add your GEMINI_API_KEY to the Secrets panel in AI Studio."))
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val nowMs = System.currentTimeMillis()
            val nowIso = sdf.format(Date(nowMs))
            
            val oneDayMs = 24 * 60 * 60 * 1000L
            val yesterday6AmMs = nowMs - oneDayMs
            val yesterdayIso = sdf.format(Date(yesterday6AmMs))

            val topicPriority = if (preferredTopics.isNotEmpty()) {
                "PRIORITIZATION MANDATE: Heavily prioritize articles and summaries about these AI topics: ${preferredTopics.joinToString(", ")}. Ensure at least 3 high-relevance articles focus strictly on these sub-topics."
            } else {
                "PRIORITIZATION MANDATE: General high-stakes machine learning breakthroughs, neural models, NLP, and computer vision."
            }

            val sourcePriority = if (preferredSources.isNotEmpty()) {
                "SOURCE PREFERENCE: Favor news items that originate from or report on these preferred outlets: ${preferredSources.joinToString(", ")}."
            } else ""

            val prompt = """
                You are a Morning Digest generator. Generate a premium and highly-detailed AI News Digest of today's Artificial Intelligence news for the date $dateStr.
                
                $topicPriority
                $sourcePriority

                Return a single strict parsed JSON object containing:
                "overallSummary": A concise, sophisticated editorial paragraph summarizing today's overall news developments and their societal stakes.
                "articles": A list of exactly 4 AI news stories. For each story, provide:
                   - "id": A unique string id (e.g., "google-gemini-35", "nvidia-ultra-chip").
                   - "title": Headline (make it punchy, insightful).
                   - "author": Reporter or publication outlet (e.g., "TechCrunch", "Wired", "VentureBeat").
                   - "description": Brief one-sentence teaser context.
                   - "url": A relevant landing URL.
                   - "imageUrl": A high-quality technology photographic image URL from Unsplash. Use valid Unsplash keywords, e.g., 'https://images.unsplash.com/photo-1620712943543-bcc4688e7485?q=80&w=600&auto=format'
                   - "publishedAt": ISO 8601 string of today (e.g., "$nowIso").
                   - "summary": A clean, extensive, multi-sentence reading view summary of the actual article content including key details, technical breakthroughs, or legal/policy significance.
                   - "sentiment": "POSITIVE", "NEUTRAL", or "NEGATIVE".
                   - "score": A decimal from 0.0 to 1.0 indicating sentiment intensity.
                "emails": A list of 4 realistic and relevant professional or personal email messages received since yesterday 6:00 AM (local time is $nowIso, and yesterday at 6:00 AM is $yesterdayIso) for the user 'agenticai54@gmail.com'. For each email, provide:
                   - "id": A unique string id (e.g., "em_123").
                   - "sender": Sender name (e.g., "Sarah from Product", "AI Studio Sentinel", "LinkedIn Work Alerts", "GitHub Support").
                   - "senderEmail": Sender email address.
                   - "subject": Short, urgent or relevant subject line.
                   - "timestamp": Millisecond epoch timestamp between $yesterday6AmMs (yesterday 6:00 AM) and $nowMs (now).
                   - "snippet": Short 1-sentence draft.
                   - "body": Full, realistic email body message. Keep it realistic, detailing things like workflow tasks, news alerts, or coordinate plans.
                "youtubeVideos": A list of 4 relevant Artificial Intelligence and Tech videos posted ONLY since yesterday 6:00 AM (local time is $nowIso) from the user's subscribed channels (including "Two Minute Papers", "OpenAI", "Yannic Kilcher", "Lex Fridman", "3Blue1Brown"). For each video, provide:
                   - "id": A unique string id (e.g., "yt_789").
                   - "title": Video title (must be highly relevant to AI or machine learning).
                   - "channelTitle": Name of the channel (e.g., "Two Minute Papers", "OpenAI").
                   - "publishedAt": ISO 8601 string (must be after $yesterdayIso).
                   - "timestamp": Millisecond epoch timestamp between $yesterday6AmMs and $nowMs.
                   - "videoUrl": Landing watch URL (e.g., "https://www.youtube.com/watch?v=dQw4w9WgXcQ").

                Format: Clean, valid JSON that conforms exactly to this specification. Do NOT surround it with markdown labels (like ```json ... ```) or any trailing text. Just the raw, valid, unquoted-root JSON document.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.7f
                )
            )

            val rawResponse = RetrofitClient.geminiApi.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )

            val textResult = rawResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Empty response from Gemini API"))

            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(DigestResponse::class.java)
            
            var cleanedJson = textResult.trim()
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substringAfter("```json").substringBeforeLast("```").trim()
            } else if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substringAfter("```").substringBeforeLast("```").trim()
            }

            val digestResponse = adapter.fromJson(cleanedJson)
                ?: return@withContext Result.failure(Exception("Failed to parse Gemini response as JSON"))

            val digestEntity = DigestEntity(
                date = dateStr,
                overallSummary = digestResponse.overallSummary,
                generatedAt = nowMs
            )
            articleDao.insertDigest(digestEntity)

            val articleEntities = digestResponse.articles.map { res ->
                val existing = articleDao.getArticleById(res.id)
                ArticleEntity(
                    id = res.id,
                    title = res.title,
                    author = res.author,
                    description = res.description,
                    url = res.url,
                    imageUrl = res.imageUrl,
                    publishedAt = res.publishedAt,
                    summary = res.summary,
                    sentiment = res.sentiment,
                    score = res.score,
                    isFavorite = existing?.isFavorite ?: false,
                    savedAt = existing?.savedAt ?: 0L
                )
            }
            articleDao.insertArticles(articleEntities)

            val emailEntities = digestResponse.emails.map { res ->
                EmailEntity(
                    id = res.id,
                    sender = res.sender,
                    senderEmail = res.senderEmail,
                    subject = res.subject,
                    timestamp = res.timestamp,
                    snippet = res.snippet,
                    body = res.body,
                    isRead = false
                )
            }
            articleDao.insertEmails(emailEntities)

            digestResponse.youtubeVideos?.let { ytList ->
                val ytEntities = ytList.map { res ->
                    YouTubeVideoEntity(
                        id = res.id,
                        title = res.title,
                        channelTitle = res.channelTitle,
                        publishedAt = res.publishedAt,
                        timestamp = res.timestamp,
                        videoUrl = res.videoUrl,
                        isRead = false
                    )
                }
                articleDao.insertYouTubeVideos(ytEntities)
            }

            Result.success(digestEntity)
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error refreshing digest: ", e)
            Result.failure(e)
        }
    }
}

@JsonClass(generateAdapter = true)
data class DigestResponse(
    val overallSummary: String,
    val articles: List<ArticleResponse>,
    val emails: List<EmailResponse>,
    val youtubeVideos: List<YouTubeVideoResponse>? = null
)

@JsonClass(generateAdapter = true)
data class ArticleResponse(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val url: String,
    val imageUrl: String,
    val publishedAt: String,
    val summary: String,
    val sentiment: String,
    val score: Double
)

@JsonClass(generateAdapter = true)
data class EmailResponse(
    val id: String,
    val sender: String,
    val senderEmail: String,
    val subject: String,
    val timestamp: Long,
    val snippet: String,
    val body: String
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoResponse(
    val id: String,
    val title: String,
    val channelTitle: String,
    val publishedAt: String,
    val timestamp: Long,
    val videoUrl: String
)
