package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ArticleEntity
import com.example.data.model.EmailEntity
import com.example.data.model.FeedbackEntity
import com.example.data.model.YouTubeVideoEntity
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.NewsViewModel
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

// Style Tokens conforming to Design Guidelines (Sleek Interface Theme)
val M3SlateBackground = Color(0xFF131118)     // Deep slate dark background
val M3CardBackground = Color(0xFF1F1D24)      // Premium card container background
val M3BorderColor = Color(0xFF35313D)          // Subtle card border
val AccentColorBlue = Color(0xFFD0BCFF)         // Lavender accent primary
val AccentColorCyan = Color(0xFFB5E3C4)         // Soft positive green

val SentimentPositiveColor = Color(0xFFB5E3C4) // Beautiful light green
val SentimentNeutralColor = Color(0xFFFFD54F)  // Glowing soft amber
val SentimentNegativeColor = Color(0xFFFF8A80) // Pastel red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: NewsViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedArticle by viewModel.selectedArticle.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val emailsSinceYesterday by viewModel.emailsSinceYesterday.collectAsStateWithLifecycle()
    val youtubeVideosSinceYesterday by viewModel.youtubeVideosSinceYesterday.collectAsStateWithLifecycle()
    
    val unreadEmailCount = emailsSinceYesterday.count { !it.isRead }
    val unreadYtCount = youtubeVideosSinceYesterday.count { !it.isRead }
    val totalUnreadCompanion = unreadEmailCount + unreadYtCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().offset(x = (-8).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(AccentColorBlue, Color(0xFF8C52FF))
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NeuralDigest",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    if (selectedArticle == null) {
                        IconButton(
                            onClick = { viewModel.triggerRefresh() },
                            modifier = Modifier.testTag("refresh_action_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Manual Content Sync Generator",
                                tint = AccentColorBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = M3SlateBackground
                )
            )
        },
        bottomBar = {
            if (selectedArticle == null) {
                NavigationBar(
                    containerColor = M3SlateBackground,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(width = (0.5).dp, color = M3BorderColor)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == ActiveScreen.DIGEST,
                        onClick = { viewModel.switchScreen(ActiveScreen.DIGEST) },
                        icon = { Icon(if (currentScreen == ActiveScreen.DIGEST) Icons.Filled.Newspaper else Icons.Outlined.Newspaper, contentDescription = "AI digest feed") },
                        label = { Text("Briefing") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentColorBlue,
                            selectedTextColor = AccentColorBlue,
                            unselectedIconColor = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                            unselectedTextColor = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                            indicatorColor = M3BorderColor
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == ActiveScreen.EMAILS,
                        onClick = { viewModel.switchScreen(ActiveScreen.EMAILS) },
                        icon = {
                            Box {
                                Icon(if (currentScreen == ActiveScreen.EMAILS) Icons.Filled.Mail else Icons.Outlined.MailOutline, contentDescription = "Inbox scanner")
                                if (totalUnreadCompanion > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 6.dp, y = (-4).dp)
                                            .background(Color.Red, CircleShape)
                                            .size(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = totalUnreadCompanion.toString(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        label = { Text("Inbox Sync") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentColorBlue,
                            selectedTextColor = AccentColorBlue,
                            unselectedIconColor = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                            unselectedTextColor = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                            indicatorColor = M3BorderColor
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == ActiveScreen.ARCHIVE,
                        onClick = { viewModel.switchScreen(ActiveScreen.ARCHIVE) },
                        icon = { Icon(if (currentScreen == ActiveScreen.ARCHIVE) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Saved offline articles") },
                        label = { Text("Archive") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentColorBlue,
                            selectedTextColor = AccentColorBlue,
                            unselectedIconColor = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                            unselectedTextColor = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                            indicatorColor = M3BorderColor
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == ActiveScreen.PROFILE,
                        onClick = { viewModel.switchScreen(ActiveScreen.PROFILE) },
                        icon = { Icon(if (currentScreen == ActiveScreen.PROFILE) Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Sync settings") },
                        label = { Text("Config") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentColorBlue,
                            selectedTextColor = AccentColorBlue,
                            unselectedIconColor = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                            unselectedTextColor = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                            indicatorColor = M3BorderColor
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(M3SlateBackground)
        ) {
            // Error, loading toasts
            when (uiState) {
                is UiState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = AccentColorBlue,
                        trackColor = M3BorderColor
                    )
                }
                is UiState.Error -> {
                    val errMsg = (uiState as UiState.Error).errorMessage
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        colors = CardDefaults.cardColors(containerColor = SentimentNegativeColor.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errMsg,
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {}
            }

            AnimatedContent(
                targetState = selectedArticle != null,
                label = "ScreenTransition"
            ) { isReading ->
                if (isReading) {
                    selectedArticle?.let { article ->
                        ArticleReaderView(
                            article = article,
                            viewModel = viewModel,
                            onBack = { viewModel.selectArticle(null) },
                            onToggleFavorite = { viewModel.toggleFavorite(article.id, !article.isFavorite) }
                        )
                    }
                } else {
                    when (currentScreen) {
                        ActiveScreen.DIGEST -> DashboardTab(viewModel)
                        ActiveScreen.ARCHIVE -> ArchiveTab(viewModel)
                        ActiveScreen.EMAILS -> EmailInboxTab(viewModel)
                        ActiveScreen.PROFILE -> ProfileSettingsTab(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTab(viewModel: NewsViewModel) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val todayDigest by viewModel.todayDigest.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            WelcomeHeadlineSection(viewModel.userEmail.value)
        }

        // Daily Digest Summary Card
        item {
            val simpleDate = remember {
                val sdf = SimpleDateFormat("MMMM dd", Locale.US)
                sdf.format(Date())
            }
            todayDigest?.let { digest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, M3BorderColor, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = M3CardBackground),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DAILY INSIGHT • $simpleDate",
                                    color = Color(0xFFCCC2DC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Morning Pulse",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF333537), CircleShape)
                                    .border(1.dp, M3BorderColor, CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(SentimentPositiveColor, CircleShape)
                                    )
                                    Text(
                                        text = "Positive Bias",
                                        color = SentimentPositiveColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = digest.overallSummary,
                            color = Color(0xFFCAC4D0),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            } ?: Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, M3BorderColor, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = M3CardBackground),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AccentColorBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Synthesizing tailored briefs...", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        // Custom High-End Sentiment Analysis weekly trend chart
        item {
            SentimentTrendsChart(articles = articles)
        }

        // Section header
        item {
            Text(
                text = "EXECUTIVE BRIEFINGS",
                color = AccentColorBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Headlines & summaries
        if (articles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = M3CardBackground)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No analysis reports loaded yet.", color = Color.Gray)
                    }
                }
            }
        } else {
            items(articles, key = { it.id }) { article ->
                ArticleCardItem(
                    article = article,
                    onClick = { viewModel.selectArticle(article) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SentimentTrendsChart(articles: List<ArticleEntity>) {
    val counts = remember(articles) {
        val baseData = listOf(
            Triple(4, 2, 1), // Monday
            Triple(5, 3, 0), // Tuesday
            Triple(3, 4, 1), // Wednesday
            Triple(6, 1, 1), // Thursday
            Triple(4, 3, 2), // Friday
            Triple(5, 2, 0), // Saturday
            Triple(6, 2, 1)  // Sunday
        )
        
        if (articles.isNotEmpty()) {
            val pos = articles.count { it.sentiment.uppercase() == "POSITIVE" }
            val neu = articles.count { it.sentiment.uppercase() == "NEUTRAL" }
            val neg = articles.count { it.sentiment.uppercase() == "NEGATIVE" }
            baseData.toMutableList().apply {
                this[6] = Triple(pos.coerceAtLeast(1), neu.coerceAtLeast(1), neg.coerceAtLeast(0))
            }
        } else {
            baseData
        }
    }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, M3BorderColor, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = M3CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SENTIMENT TRENDS OVER TIME",
                        color = Color(0xFFCCC2DC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sentiment Pulse Grid",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     LegendIndicator(label = "Opt", color = SentimentPositiveColor)
                     LegendIndicator(label = "Neu", color = SentimentNeutralColor)
                     LegendIndicator(label = "Caut", color = SentimentNegativeColor)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val paddingX = 14.dp.toPx()
                val chartWidth = size.width - paddingX * 2
                val chartHeight = size.height - 10.dp.toPx()
                val barWidth = 16.dp.toPx()
                val stepX = chartWidth / 6f

                val maxVal = 10f

                val levels = 3
                for (i in 0..levels) {
                     val y = chartHeight * (i.toFloat() / levels)
                     drawLine(
                         color = M3BorderColor.copy(alpha = 0.4f),
                         start = androidx.compose.ui.geometry.Offset(paddingX, y),
                         end = androidx.compose.ui.geometry.Offset(size.width - paddingX, y),
                         strokeWidth = 1.dp.toPx()
                     )
                }

                // Bar compiler drawing
                counts.forEachIndexed { index, triple ->
                    val total = (triple.first + triple.second + triple.third).toFloat()
                    val normFactor = if (total > 0) chartHeight / maxVal else 0.0f

                    val hPositive = triple.first * normFactor
                    val hNeutral = triple.second * normFactor
                    val hNegative = triple.third * normFactor

                    val x = paddingX + index * stepX - barWidth / 2f
                    var currentY = chartHeight

                    if (hNegative > 0) {
                        drawRect(
                            color = SentimentNegativeColor,
                            topLeft = androidx.compose.ui.geometry.Offset(x, currentY - hNegative),
                            size = androidx.compose.ui.geometry.Size(barWidth, hNegative)
                        )
                        currentY -= hNegative
                    }

                    if (hNeutral > 0) {
                        drawRect(
                            color = SentimentNeutralColor,
                            topLeft = androidx.compose.ui.geometry.Offset(x, currentY - hNeutral),
                            size = androidx.compose.ui.geometry.Size(barWidth, hNeutral)
                        )
                        currentY -= hNeutral
                    }

                    if (hPositive > 0) {
                        drawRect(
                            color = SentimentPositiveColor,
                            topLeft = androidx.compose.ui.geometry.Offset(x, currentY - hPositive),
                            size = androidx.compose.ui.geometry.Size(barWidth, hPositive)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Text(
                        text = day,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun LegendIndicator(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WelcomeHeadlineSection(userEmail: String) {
    Column {
        Text(
            text = "Welcome Back, Reporter",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "Authenticated with $userEmail",
            color = AccentColorBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ArticleCardItem(
    article: ArticleEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, M3BorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("article_card_${article.id}"),
        colors = CardDefaults.cardColors(containerColor = M3CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            if (article.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.author,
                        color = AccentColorBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    
                    SentimentBadge(sentiment = article.sentiment, score = article.score)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = article.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.Serif
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = article.description,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ArticleReaderView(
    article: ArticleEntity,
    viewModel: NewsViewModel,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    var rating by remember { mutableStateOf(0) }
    var thumbsUp by remember { mutableStateOf<Boolean?>(null) }
    var comment by remember { mutableStateOf("") }
    var hasSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(article.id) {
         val existing = viewModel.getFeedback(article.id)
         if (existing != null) {
              rating = existing.score
              thumbsUp = existing.isThumbsUp
              comment = existing.comment
              hasSubmitted = true
         }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(M3SlateBackground)
            .padding(16.dp)
            .testTag("clean_reading_view")
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Close reader", tint = Color.White)
                }
                
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (article.isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save offline",
                        tint = if (article.isFavorite) AccentColorBlue else Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = article.title,
                color = Color.White,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "By ${article.author}",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Published ${formatIsoTime(article.publishedAt)}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                SentimentBadge(sentiment = article.sentiment, score = article.score, detailed = true)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (article.imageUrl.isNotEmpty()) {
            item {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, M3BorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = M3CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXECUTIVE BRIEF",
                        color = AccentColorBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.summary,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Integration of feedback mechanism
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, M3BorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = M3CardBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RATE SUMMARY QUALITY",
                        color = AccentColorBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your feedback is stored and directly used to optimize the underlying Gemini summarizer models.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (hasSubmitted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF262529), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Submitted",
                                tint = SentimentPositiveColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Feedback saved! Thanks for improving summaries.",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Is this helpful?", color = Color.White, fontSize = 13.sp)
                            
                            IconButton(
                                onClick = { thumbsUp = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (thumbsUp == true) AccentColorBlue.copy(alpha = 0.2f) else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(1.dp, if (thumbsUp == true) AccentColorBlue else M3BorderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbUp,
                                    contentDescription = "Thumbs Up",
                                    tint = if (thumbsUp == true) AccentColorBlue else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { thumbsUp = false },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (thumbsUp == false) SentimentNegativeColor.copy(alpha = 0.2f) else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(1.dp, if (thumbsUp == false) SentimentNegativeColor else M3BorderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbDown,
                                    contentDescription = "Thumbs Down",
                                    tint = if (thumbsUp == false) SentimentNegativeColor else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "Digest rating:", color = Color.White, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                (1..5).forEach { star ->
                                    val isSelected = star <= rating
                                    IconButton(
                                        onClick = { rating = star },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "$star Stars",
                                            tint = if (isSelected) Color(0xFFFFD180) else Color.Gray,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            placeholder = { Text("What could be improved? (Optional)", color = Color.Gray, fontSize = 12.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth().testTag("comment_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColorBlue,
                                unfocusedBorderColor = M3BorderColor,
                                focusedContainerColor = M3SlateBackground,
                                unfocusedContainerColor = M3SlateBackground
                            ),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (thumbsUp != null || rating > 0) {
                                    viewModel.submitFeedback(
                                        articleId = article.id,
                                        score = rating,
                                        isThumbsUp = thumbsUp ?: true,
                                        comment = comment
                                    )
                                    hasSubmitted = true
                                    Toast.makeText(context, "Feedback registered successfully", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("submit_feedback_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentColorBlue),
                            shape = RoundedCornerShape(10.dp),
                            enabled = thumbsUp != null || rating > 0
                        ) {
                            Text(text = "Submit rating", color = Color(0xFF381E72), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share analysis with colleagues",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShareButton(
                        label = "X (Twitter)",
                        icon = Icons.Filled.Send,
                        backgroundColor = Color(0xFF1DA1F2),
                        onClick = { shareArticle(context, article.title, article.author, article.sentiment, article.summary, "Twitter") }
                    )
                    ShareButton(
                        label = "LinkedIn",
                        icon = Icons.Filled.Share,
                        backgroundColor = Color(0xFF0A66C2),
                        onClick = { shareArticle(context, article.title, article.author, article.sentiment, article.summary, "LinkedIn") }
                    )
                    ShareButton(
                        label = "Email",
                        icon = Icons.Filled.Email,
                        backgroundColor = Color(0xFFEA4335),
                        onClick = { shareArticle(context, article.title, article.author, article.sentiment, article.summary, "Email") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ShareButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SentimentBadge(
    sentiment: String,
    score: Double,
    detailed: Boolean = false
) {
    val color = when (sentiment.uppercase(Locale.ROOT)) {
        "POSITIVE" -> SentimentPositiveColor
        "NEGATIVE" -> SentimentNegativeColor
        else -> SentimentNeutralColor
    }
    
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = if (detailed) "${sentiment.uppercase()} (${String.format("%.2f", score)})" else sentiment.uppercase(),
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ArchiveTab(viewModel: NewsViewModel) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, tint = AccentColorBlue, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Personal Archive",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }
            Text(
                text = "Locally synchronized summaries available for offline reading.",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (favorites.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "Your archive is currently empty.",
                    tip = "Click the bookmark icon at the top of any brief screen to download summaries instantly."
                )
            }
        } else {
            items(favorites, key = { it.id }) { article ->
                ArticleCardItem(
                    article = article,
                    onClick = { viewModel.selectArticle(article) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EmailInboxTab(viewModel: NewsViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Emails, 1 = YouTube Subscriptions
    val emailsSinceYesterday by viewModel.emailsSinceYesterday.collectAsStateWithLifecycle()
    val youtubeVideos by viewModel.youtubeVideosSinceYesterday.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedEmail by remember { mutableStateOf<EmailEntity?>(null) }

    if (selectedEmail != null) {
        EmailReaderSheet(
            email = selectedEmail!!,
            onBack = { selectedEmail = null },
            onMarkRead = {
                viewModel.markEmailAsRead(selectedEmail!!.id)
                selectedEmail = null
            }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sub Tabs Choice Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val emailUnreadCount = emailsSinceYesterday.count { !it.isRead }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 0) M3CardBackground else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .border(
                            1.dp,
                            if (selectedTab == 0) AccentColorBlue else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Emails",
                            tint = if (selectedTab == 0) AccentColorBlue else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Inbox Scans",
                            color = if (selectedTab == 0) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (emailUnreadCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.Red, CircleShape)
                                    .size(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emailUnreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                val ytUnreadCount = youtubeVideos.count { !it.isRead }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 1) M3CardBackground else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .border(
                            1.dp,
                            if (selectedTab == 1) AccentColorBlue else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayCircle,
                            contentDescription = "YouTube Subs",
                            tint = if (selectedTab == 1) AccentColorBlue else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YouTube Subs",
                            color = if (selectedTab == 1) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (ytUnreadCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.Red, CircleShape)
                                    .size(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ytUnreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Gmail Inbound Companion",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Curated AI news and updates from your mailbox received since yesterday 6:00 AM.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (emailsSinceYesterday.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "Inbox clear since yesterday 6:00 AM.",
                                tip = "No incoming bulletins found yet. Authenticate completely in synchronization settings."
                            )
                        }
                    } else {
                        items(emailsSinceYesterday, key = { it.id }) { email ->
                            EmailRowCard(
                                email = email,
                                onClick = { selectedEmail = email }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "YouTube Subscription Releases",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "New Artificial Intelligence related video uploads since yesterday 6:00 AM.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (youtubeVideos.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = "No recent AI updates on your subscriptions list.",
                                tip = "Click refresh at top of BRIEFING to scan subscriptions for Yannic Kilcher, OpenAI, etc. channels."
                            )
                        }
                    } else {
                        items(youtubeVideos, key = { it.id }) { video ->
                            YouTubeVideoRowCard(
                                video = video,
                                onClick = {
                                    viewModel.markYouTubeVideoAsRead(video.id)
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Opening YouTube watch URL...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun YouTubeVideoRowCard(
    video: YouTubeVideoEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, M3BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("youtube_card_${video.id}"),
        colors = CardDefaults.cardColors(containerColor = M3CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .background(
                        color = if (!video.isRead) AccentColorBlue else Color.Transparent,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.channelTitle,
                        color = Color(0xFFFFD180),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = formatEpochTime(video.timestamp),
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = video.title,
                    color = if (!video.isRead) Color.White else Color.LightGray,
                    fontWeight = if (!video.isRead) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Watch",
                        tint = AccentColorBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Watch subscription on YouTube",
                        color = AccentColorBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmailRowCard(
    email: EmailEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, M3BorderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("email_card_${email.id}"),
        colors = CardDefaults.cardColors(containerColor = M3CardBackground),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .background(
                        color = if (!email.isRead) AccentColorBlue else Color.Transparent,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = email.sender,
                        color = if (!email.isRead) Color.White else Color.LightGray,
                        fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = formatEpochTime(email.timestamp),
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = email.subject,
                    color = if (!email.isRead) Color.White else Color.LightGray,
                    fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = email.snippet,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmailReaderSheet(
    email: EmailEntity,
    onBack: () -> Unit,
    onMarkRead: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(M3SlateBackground)
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                }
                
                IconButton(onClick = onMarkRead) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = "Mark as read", tint = AccentColorBlue)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, M3BorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = M3CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sender: ${email.sender} (${email.senderEmail})",
                        color = AccentColorBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Subject: ${email.subject}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = email.body,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onMarkRead,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColorBlue)
            ) {
                Text("Dismiss & Mark read", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsTab(viewModel: NewsViewModel) {
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncedTime by viewModel.lastSyncedTime.collectAsStateWithLifecycle()
    val alarmEnabled by viewModel.alarmEnabled.collectAsStateWithLifecycle()
    val selectedTopics by viewModel.selectedTopics.collectAsStateWithLifecycle()
    val selectedSources by viewModel.selectedSources.collectAsStateWithLifecycle()

    var customMailInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = AccentColorBlue, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sync & Personalization",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }
            Text(
                text = "Authenticate and prioritize how your machine learning summaries update.",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Section: Personalization Settings (Topics & Sources)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, M3BorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = M3CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PERSONALIZED AI FEED",
                        color = AccentColorBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prioritize specific interests in your morning generative summaries.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "AI Fields & Topics",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.availableTopics.forEach { topic ->
                            val isSelected = selectedTopics.contains(topic)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentColorBlue else Color(0xFF2C2A31))
                                    .clickable { 
                                         viewModel.toggleTopic(topic) 
                                    }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) AccentColorBlue else M3BorderColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = topic,
                                    color = if (isSelected) Color(0xFF381E72) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Preferred News Sources",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.availableSources.forEach { source ->
                            val isSelected = selectedSources.contains(source)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentColorBlue else Color(0xFF2C2A31))
                                    .clickable { viewModel.toggleSource(source) }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) AccentColorBlue else M3BorderColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = source,
                                    color = if (isSelected) Color(0xFF381E72) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    if (selectedTopics.isNotEmpty() || selectedSources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C2A31), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                             Icon(
                                 imageVector = Icons.Filled.Star,
                                 contentDescription = "Preferences",
                                 tint = AccentColorBlue,
                                 modifier = Modifier.size(14.dp)
                             )
                             Spacer(modifier = Modifier.width(8.dp))
                             Text(
                                 text = "Preferences active. Next Refresh will compile customized briefs. ✨",
                                 color = Color.White,
                                 fontSize = 11.sp
                             )
                        }
                    }
                }
            }
        }

        // Section: Authentication & Cloud Sync
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, M3BorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = M3CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACCOUNT SYNCHRONIZATION",
                        color = AccentColorBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (userEmail.isNotEmpty()) {
                        Text(
                            text = "Signed in as",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = userEmail,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Last Cloud Sync: ${formatEpochDateTime(lastSyncedTime)}",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.triggerCloudSync() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentColorBlue),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isSyncing
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.Sync, contentDescription = "Sync", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Force Sync", fontSize = 12.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.signOut() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Sign Out", fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text(
                            text = "Sync articles and channel subscription updates across multiple devices.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customMailInput,
                            onValueChange = { customMailInput = it },
                            label = { Text("E-mail address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(M3SlateBackground)
                                .testTag("username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = AccentColorBlue,
                                unfocusedBorderColor = M3BorderColor,
                                focusedLabelColor = AccentColorBlue,
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (customMailInput.isNotEmpty()) {
                                    viewModel.signIn(customMailInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentColorBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("login_button")
                        ) {
                            Text("Mock Sync Authentication Login", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Delivery settings (Alarms)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, M3BorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = M3CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NOTIFICATION DELIVERY",
                        color = AccentColorBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Everyday 7:00 AM Prompt",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Curate and dispatch summaries via secure push notification alarm.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Switch(
                            checked = alarmEnabled,
                            onCheckedChange = { viewModel.toggleAlarmSetting(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentColorBlue,
                                checkedTrackColor = AccentColorBlue.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = M3BorderColor
                            ),
                            modifier = Modifier.testTag("alarm_switch")
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun EmptyStateCard(message: String, tip: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, M3BorderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = M3CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Newspaper,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = Color.LightGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tip,
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Helpers
fun formatIsoTime(isoStr: String): String {
    return try {
        if (isoStr.isEmpty()) return "Today"
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoStr) ?: return "Today"
        val outSdf = SimpleDateFormat("MMMM dd, hh:mm a", Locale.US)
        outSdf.format(date)
    } catch (e: Exception) {
        "Today"
    }
}

fun formatEpochTime(epochMs: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.US)
    return sdf.format(Date(epochMs))
}

fun formatEpochDateTime(epochMs: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
    return sdf.format(Date(epochMs))
}

fun shareArticle(
    context: Context,
    title: String,
    author: String,
    sentiment: String,
    summary: String,
    platform: String
) {
    val shareText = when (platform) {
        "Twitter" -> "Check out today's AI headlines: \"$title\" ($sentiment sentiment) - Curated summary from NeuralDigest! ☀️🤖 #AI"
        "LinkedIn" -> "Today's Artificial Intelligence Development: \n\n$title\nSentiment analysis: $sentiment\n\nCurated Summary: $summary\n\n#AI #TechnologyUpdates"
        else -> "Hi there,\n\nI wanted to share this Artificial Intelligence update with you:\n\nTitle: $title\nJournalist/Source: $author\nSentiment: $sentiment\nSummary:\n$summary\n\nShared from NeuralDigest."
    }
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "AI News: $title")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}
