package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ArticleDatabase
import com.example.data.model.ArticleEntity
import com.example.data.model.DigestEntity
import com.example.data.model.EmailEntity
import com.example.data.model.FeedbackEntity
import com.example.data.model.YouTubeVideoEntity
import com.example.data.repository.NewsRepository
import com.example.notification.DigestNotificationReceiver
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class ActiveScreen {
    DIGEST,
    ARCHIVE,
    EMAILS,
    PROFILE
}

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val message: String) : UiState
    data class Error(val errorMessage: String) : UiState
}

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ArticleDatabase.getDatabase(application)
    private val repository = NewsRepository(database.dao())
    private val sharedPrefs = application.getSharedPreferences("neural_digest_prefs", Context.MODE_PRIVATE)

    // UI Navigation state
    private val _currentScreen = MutableStateFlow(ActiveScreen.DIGEST)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    private val _selectedArticle = MutableStateFlow<ArticleEntity?>(null)
    val selectedArticle: StateFlow<ArticleEntity?> = _selectedArticle.asStateFlow()

    // Auth Session State
    private val _userEmail = MutableStateFlow("agenticai54@gmail.com") // Logged in by default per environment info
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastSyncedTime: StateFlow<Long> = _lastSyncedTime.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Alarm scheduler state
    private val _alarmEnabled = MutableStateFlow(true)
    val alarmEnabled: StateFlow<Boolean> = _alarmEnabled.asStateFlow()

    // Personalization configuration state
    private val _selectedTopics = MutableStateFlow<Set<String>>(emptySet())
    val selectedTopics: StateFlow<Set<String>> = _selectedTopics.asStateFlow()

    private val _selectedSources = MutableStateFlow<Set<String>>(emptySet())
    val selectedSources: StateFlow<Set<String>> = _selectedSources.asStateFlow()

    // List of available topics and sources
    val availableTopics = listOf(
        "Machine Learning",
        "Large Language Models",
        "NLP",
        "Computer Vision",
        "Robotics",
        "AI Safety",
        "AI Policy"
    )

    val availableSources = listOf(
        "TechCrunch",
        "Wired",
        "VentureBeat",
        "OpenAI Blog",
        "MIT Tech Review",
        "arXiv Research"
    )

    // Reactive streams from database
    val articles: StateFlow<List<ArticleEntity>> = repository.allArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<ArticleEntity>> = repository.favoriteArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedbacks: StateFlow<List<FeedbackEntity>> = repository.allFeedbacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Email filtering: new emails since yesterday at 6:00 AM
    val yesterdaySixAmTimestamp: Long
        get() {
            // Yesterday at 6:00 AM UTC
            val dayMs = 24 * 60 * 60 * 1000L
            val currentMs = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val todayStr = sdf.format(Date(currentMs))
            
            // Parse start of today at 00:00:00
            val todayStart = sdf.parse(todayStr)?.time ?: currentMs
            // Yesterday 00:00:00 + 6 hours
            val yesterdayStart = todayStart - dayMs
            val sixHoursMs = 6 * 60 * 60 * 1000L
            return yesterdayStart + sixHoursMs
        }

    val emailsSinceYesterday: StateFlow<List<EmailEntity>> = repository.getEmailsSince(yesterdaySixAmTimestamp)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // YouTube companion subscription scans since yesterday 6:00 AM 
    val youtubeVideosSinceYesterday: StateFlow<List<YouTubeVideoEntity>> = repository.getYouTubeVideosSince(yesterdaySixAmTimestamp)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily digest overall summary matching today
    val todayDateString: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date())
        }

    val todayDigest: StateFlow<DigestEntity?> = repository.getDigestForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Load configurations
        val savedTopics = sharedPrefs.getStringSet("selected_topics", emptySet()) ?: emptySet()
        val savedSources = sharedPrefs.getStringSet("selected_sources", emptySet()) ?: emptySet()
        _selectedTopics.value = savedTopics
        _selectedSources.value = savedSources

        // Automatically fetch digest on launch
        triggerRefresh()
        
        // Ensure 7:00 AM Alarm is active in system on boot or startup
        if (_alarmEnabled.value) {
            DigestNotificationReceiver.scheduleDailyNotification(application)
        }
    }

    fun switchScreen(screen: ActiveScreen) {
        _currentScreen.value = screen
        _selectedArticle.value = null // reset reader when navigating
    }

    fun selectArticle(article: ArticleEntity?) {
        _selectedArticle.value = article
    }

    fun toggleFavorite(articleId: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(articleId, isFav)
            // Trigger a quick cross-device mock sync after updating favorites
            triggerCloudSync()
        }
    }
    
    fun markEmailAsRead(id: String) {
        viewModelScope.launch {
            repository.markEmailAsRead(id)
        }
    }

    fun markYouTubeVideoAsRead(id: String) {
        viewModelScope.launch {
            repository.markYouTubeVideoAsRead(id)
        }
    }

    fun toggleTopic(topic: String) {
        val currentSet = _selectedTopics.value.toMutableSet()
        if (currentSet.contains(topic)) {
            currentSet.remove(topic)
        } else {
            currentSet.add(topic)
        }
        _selectedTopics.value = currentSet
        sharedPrefs.edit().putStringSet("selected_topics", currentSet).apply()
    }

    fun toggleSource(source: String) {
        val currentSet = _selectedSources.value.toMutableSet()
        if (currentSet.contains(source)) {
            currentSet.remove(source)
        } else {
            currentSet.add(source)
        }
        _selectedSources.value = currentSet
        sharedPrefs.edit().putStringSet("selected_sources", currentSet).apply()
    }

    fun submitFeedback(articleId: String, score: Int, isThumbsUp: Boolean, comment: String = "") {
        viewModelScope.launch {
            repository.saveFeedback(articleId, score, isThumbsUp, comment)
            triggerCloudSync()
        }
    }

    suspend fun getFeedback(articleId: String): FeedbackEntity? {
        return repository.getFeedbackForArticle(articleId)
    }

    fun toggleAlarmSetting(enabled: Boolean) {
        _alarmEnabled.value = enabled
        if (enabled) {
            DigestNotificationReceiver.scheduleDailyNotification(getApplication())
        }
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val dateStr = todayDateString
            val result = repository.refreshDigest(
                dateStr = dateStr,
                preferredTopics = _selectedTopics.value.toList(),
                preferredSources = _selectedSources.value.toList()
            )
            result.onSuccess {
                _uiState.value = UiState.Success("Morning AI digest loaded successfully.")
                _lastSyncedTime.value = System.currentTimeMillis()
            }.onFailure { exception ->
                _uiState.value = UiState.Error(exception.message ?: "Failed to generate AI morning digest.")
            }
        }
    }

    fun triggerCloudSync() {
        if (_userEmail.value.isEmpty()) return
        viewModelScope.launch {
            _isSyncing.value = true
            // Simulate cloud payload transport to device coordinator
            kotlinx.coroutines.delay(1200)
            _lastSyncedTime.value = System.currentTimeMillis()
            _isSyncing.value = false
        }
    }

    fun signIn(email: String) {
        _userEmail.value = email
        triggerCloudSync()
    }

    fun signOut() {
        _userEmail.value = ""
    }
}
