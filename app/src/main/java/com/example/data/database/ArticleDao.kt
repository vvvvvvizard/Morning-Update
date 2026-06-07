package com.example.data.database

import androidx.room.*
import com.example.data.model.ArticleEntity
import com.example.data.model.DigestEntity
import com.example.data.model.EmailEntity
import com.example.data.model.FeedbackEntity
import com.example.data.model.YouTubeVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    // Article Queries
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun getFavoriteArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: String): ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Update
    suspend fun updateArticle(article: ArticleEntity)

    @Query("UPDATE articles SET isFavorite = :isFav, savedAt = :savedTime WHERE id = :id")
    suspend fun setFavoriteState(id: String, isFav: Boolean, savedTime: Long)

    // Digest Queries
    @Query("SELECT * FROM digests WHERE date = :date LIMIT 1")
    fun getDigestByDate(date: String): Flow<DigestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDigest(digest: DigestEntity)

    // Email Queries
    @Query("SELECT * FROM emails WHERE timestamp >= :timestamp ORDER BY timestamp DESC")
    fun getEmailsSince(timestamp: Long): Flow<List<EmailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)
    
    @Query("UPDATE emails SET isRead = :isRead WHERE id = :id")
    suspend fun setEmailReadState(id: String, isRead: Boolean)

    // Feedback Queries
    @Query("SELECT * FROM feedbacks ORDER BY timestamp DESC")
    fun getAllFeedbacks(): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity)

    @Query("SELECT * FROM feedbacks WHERE articleId = :articleId LIMIT 1")
    suspend fun getFeedbackByArticleId(articleId: String): FeedbackEntity?

    // YouTube Video Queries
    @Query("SELECT * FROM youtube_videos WHERE timestamp >= :timestamp ORDER BY timestamp DESC")
    fun getYouTubeVideosSince(timestamp: Long): Flow<List<YouTubeVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYouTubeVideos(videos: List<YouTubeVideoEntity>)

    @Query("UPDATE youtube_videos SET isRead = :isRead WHERE id = :id")
    suspend fun setYouTubeVideoReadState(id: String, isRead: Boolean)
}
