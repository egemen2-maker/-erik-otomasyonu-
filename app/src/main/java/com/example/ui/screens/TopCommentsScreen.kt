package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TrendingUp
import com.example.data.api.SocialApiConfig
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommentItem
import com.example.model.CommentSentiment
import com.example.model.ReplyTone
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioPrimaryDark
import com.example.ui.theme.StudioPrimaryLight
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.VideoAutomationViewModel

@Composable
fun TopCommentsScreen(
    viewModel: VideoAutomationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProject.collectAsState()
    val commentsList by viewModel.commentsList.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingComments.collectAsState()
    val isAutoReplyEnabled by viewModel.isAiAutoReplyEnabled.collectAsState()
    val activeTone by viewModel.activeReplyTone.collectAsState()
    val socialConfig by viewModel.socialConfig.collectAsState()
    val apiStatusMessage by viewModel.apiStatusMessage.collectAsState()
    val isLiveApiLoading by viewModel.isLiveApiLoading.collectAsState()

    var selectedPlatformFilter by remember { mutableStateOf("Tümü") }
    var selectedSentimentFilter by remember { mutableStateOf<CommentSentiment?>(null) }
    var showAddCommentDialog by remember { mutableStateOf(false) }
    var showApiConnectDialog by remember { mutableStateOf(false) }

    val filteredComments = commentsList.filter { comment ->
        val matchPlatform = when (selectedPlatformFilter) {
            "Instagram" -> comment.platform.contains("Instagram", ignoreCase = true)
            "YouTube" -> comment.platform.contains("YouTube", ignoreCase = true)
            "TikTok" -> comment.platform.contains("TikTok", ignoreCase = true)
            else -> true
        }
        val matchSentiment = selectedSentimentFilter == null || comment.sentiment == selectedSentimentFilter
        matchPlatform && matchSentiment
    }

    val repliedCount = commentsList.count { it.isReplied }
    val totalCount = commentsList.size
    val replyRate = if (totalCount > 0) ((repliedCount.toFloat() / totalCount) * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Hero Overview Card (High Density style)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, StudioBorder.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StudioSurfaceVariant
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Forum,
                                    contentDescription = null,
                                    tint = StudioPrimaryDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "EN ÇOK GELEN YORUMLAR & AI BOT",
                                    color = Color(0xFF1D192B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }

                        Text(
                            text = "Canlı Etkileşim: Aktif",
                            color = StudioPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "İzleyici Yorumları & Akıllı Yanıt Yönetimi",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    )

                    Text(
                        text = "Videonuz için en çok tekrarlanan soruları görün, manuel olarak kendi cevabınızı yazın veya istediğiniz zaman tek dokunuşla AI ile otomatik yanıtlayın.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // 2x2 Stats Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = StudioSurface,
                            border = BorderStroke(1.dp, StudioBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "TOPLAM GELEN YORUM",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "1,480+ Yorum",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioPrimary
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = StudioSurface,
                            border = BorderStroke(1.dp, StudioBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "YANITLANMA ORANI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "$repliedCount/$totalCount (%$replyRate)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (replyRate > 60) SuccessGreen else WarningAmber
                                )
                            }
                        }
                    }

                    // Auto-Reply Mode Toggle Switch
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAutoReplyEnabled) StudioPrimaryDark else StudioSurface,
                        border = BorderStroke(1.dp, if (isAutoReplyEnabled) StudioPrimaryDark else StudioBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SmartToy,
                                    contentDescription = null,
                                    tint = if (isAutoReplyEnabled) Color.White else StudioPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isAutoReplyEnabled) "Otomatik AI Yanıtlayıcı (Aktif)" else "İsteğe Bağlı AI / Manuel Yanıt Modu",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAutoReplyEnabled) Color.White else TextPrimary
                                    )
                                    Text(
                                        text = if (isAutoReplyEnabled) "Yeni gelen sorulara anında AI yanıtı atanır" else "Yorumları siz yazabilir veya dilediğinizde AI'a yanıtlatabilirsiniz",
                                        fontSize = 11.sp,
                                        color = if (isAutoReplyEnabled) Color.White.copy(alpha = 0.8f) else TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isAutoReplyEnabled,
                                onCheckedChange = { viewModel.toggleAutoReplyMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StudioPrimaryDark,
                                    checkedTrackColor = StudioPrimaryLight,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFCAC4D0)
                                ),
                                modifier = Modifier.testTag("switch_auto_reply")
                            )
                        }
                    }
                }
            }
        }

        // Active Project Context & Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ŞU ANKİ VİDEO PROJESİ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = activeProject?.topic ?: "Günde 1 Saat Yapay Zeka ile Pasif Gelir",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadTopCommentsForProject() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StudioPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_refresh_comments")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = StudioPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Yenile",
                                    tint = StudioPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Yorumları Analiz Et",
                                fontSize = 11.sp,
                                color = StudioPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Tone selector
                    Text(
                        text = "AI Yanıt Tonu:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ReplyTone.values()) { tone ->
                            val isSelected = activeTone == tone
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setReplyTone(tone) },
                                label = {
                                    Text(
                                        text = "${tone.emoji} ${tone.label}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioPrimaryLight,
                                    selectedLabelColor = StudioPrimaryDark,
                                    containerColor = StudioSurfaceElevated,
                                    labelColor = TextPrimary
                                ),
                                border = BorderStroke(1.dp, if (isSelected) StudioPrimary else StudioBorder)
                            )
                        }
                    }

                    // Live Social API Connection Status Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = StudioSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudSync,
                                        contentDescription = null,
                                        tint = StudioPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Gerçek Hesap & Canlı API",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showApiConnectDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, StudioPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Key,
                                        contentDescription = null,
                                        tint = StudioPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "API & Hesap Bağla",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioPrimary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // YouTube status chip
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (socialConfig.isYouTubeConnected) YouTubeRed.copy(alpha = 0.15f) else StudioSurface,
                                    border = BorderStroke(0.5.dp, if (socialConfig.isYouTubeConnected) YouTubeRed else StudioBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (socialConfig.isYouTubeConnected) SuccessGreen else Color.Gray)
                                        )
                                        Text(
                                            text = if (socialConfig.isYouTubeConnected) "YouTube: Canlı" else "YouTube: Pasif",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Instagram status chip
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (socialConfig.isInstagramConnected) InstagramPink.copy(alpha = 0.15f) else StudioSurface,
                                    border = BorderStroke(0.5.dp, if (socialConfig.isInstagramConnected) InstagramPink else StudioBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (socialConfig.isInstagramConnected) SuccessGreen else Color.Gray)
                                        )
                                        Text(
                                            text = if (socialConfig.isInstagramConnected) "Instagram: Canlı" else "Instagram: Pasif",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // API Status Feedback Banner
                    if (!apiStatusMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StudioPrimaryDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (isLiveApiLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = apiStatusMessage ?: "",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearStatusMessage() },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Kapat",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.autoReplyAllPendingWithAi()
                                Toast.makeText(context, "Tüm bekleyen yorumlar AI ile yanıtlandı!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("btn_auto_reply_all"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tümünü AI ile Yanıtla",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = { showAddCommentDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StudioBorder),
                            modifier = Modifier.testTag("btn_add_simulated_comment")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Yorum Simüle Et",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Platform & Sentiment Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Filtrele:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val platformOptions = listOf("Tümü", "Instagram", "YouTube", "TikTok")
                    items(platformOptions) { plat ->
                        val isSelected = selectedPlatformFilter == plat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPlatformFilter = plat },
                            label = {
                                Text(
                                    text = plat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioSurfaceVariant,
                                selectedLabelColor = Color(0xFF1D192B),
                                containerColor = StudioSurface,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(1.dp, if (isSelected) StudioPrimary else StudioBorder)
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSentimentFilter == null,
                            onClick = { selectedSentimentFilter = null },
                            label = { Text("Tüm Kategoriler", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioPrimaryLight,
                                selectedLabelColor = StudioPrimaryDark
                            ),
                            border = BorderStroke(1.dp, if (selectedSentimentFilter == null) StudioPrimary else StudioBorder)
                        )
                    }

                    items(CommentSentiment.values()) { sentiment ->
                        val isSelected = selectedSentimentFilter == sentiment
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSentimentFilter = if (isSelected) null else sentiment },
                            label = { Text(sentiment.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(sentiment.badgeColorHex).copy(alpha = 0.15f),
                                selectedLabelColor = Color(sentiment.badgeColorHex)
                            ),
                            border = BorderStroke(1.dp, if (isSelected) Color(sentiment.badgeColorHex) else StudioBorder)
                        )
                    }
                }
            }
        }

        // Section Title: Most Frequent Questions / Comments List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "En Sık Gelen Yorumlar (${filteredComments.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Sıklık Sırasına Göre",
                    fontSize = 11.sp,
                    color = StudioPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Comment Cards
        if (filteredComments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                    border = BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Filtreye uygun yorum bulunamadı.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = { viewModel.loadTopCommentsForProject() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                        ) {
                            Text("Yeni Yorumları Yükle", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredComments, key = { it.id }) { comment ->
                CommentCardItem(
                    comment = comment,
                    activeTone = activeTone,
                    onUpdateCustomReply = { newText ->
                        viewModel.updateCustomReplyText(comment.id, newText)
                    },
                    onApplyAiSuggestion = {
                        viewModel.applyAiSuggestionToActiveReply(comment.id)
                    },
                    onGenerateNewAiReply = {
                        viewModel.generateNewAiReplyForComment(comment.id, activeTone)
                    },
                    onSendReply = { replyText, isAi ->
                        viewModel.executeLiveOrSimulatedReply(comment, replyText, isAi)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Yanıt", replyText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Yanıt işlendi ve panoya kopyalandı! ✅", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Live API & Account Connection Dialog
    if (showApiConnectDialog) {
        ApiConnectionDialog(
            currentConfig = socialConfig,
            isLoading = isLiveApiLoading,
            onDismiss = { showApiConnectDialog = false },
            onFetchYouTube = { apiKey, videoId ->
                viewModel.fetchLiveYouTubeComments(apiKey, videoId)
            },
            onFetchInstagram = { token, mediaId ->
                viewModel.fetchLiveInstagramComments(token, mediaId)
            },
            onSaveConfig = { updatedConfig ->
                viewModel.updateSocialConfig(updatedConfig)
                showApiConnectDialog = false
                Toast.makeText(context, "API Ayarları Kaydedildi!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Add Simulated Comment Dialog
    if (showAddCommentDialog) {
        AddCommentDialog(
            onDismiss = { showAddCommentDialog = false },
            onAdd = { author, platform, text ->
                viewModel.addManualIncomingComment(author, platform, text)
                showAddCommentDialog = false
                Toast.makeText(context, "Yeni yorum eklendi ve AI yanıtı hazırlandı!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun CommentCardItem(
    comment: CommentItem,
    activeTone: ReplyTone,
    onUpdateCustomReply: (String) -> Unit,
    onApplyAiSuggestion: () -> Unit,
    onGenerateNewAiReply: () -> Unit,
    onSendReply: (String, Boolean) -> Unit
) {
    val platformColor = when {
        comment.platform.contains("Instagram", ignoreCase = true) -> InstagramPink
        comment.platform.contains("YouTube", ignoreCase = true) -> YouTubeRed
        comment.platform.contains("TikTok", ignoreCase = true) -> TikTokCyan
        else -> StudioPrimary
    }

    var isEditingReply by remember { mutableStateOf(false) }
    var currentInputText by remember(comment.userCustomReply, comment.activeReplyText) {
        mutableStateOf(comment.userCustomReply.ifBlank { comment.activeReplyText })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (comment.isReplied) SuccessGreen.copy(alpha = 0.5f) else StudioBorder,
                RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Platform badge, Author info, Time, and Likes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Platform Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = platformColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = comment.platform,
                            color = platformColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Author info
                    Text(
                        text = comment.authorName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = comment.authorHandle,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Beğeni",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${comment.likesCount}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = comment.timestamp,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            // Frequency Highlight Banner (Top Repeat Question)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = StudioPrimaryLight.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = StudioPrimaryDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "🔥 En Sık Gelen Yorum: ${comment.frequencyCount} kullanıcı sordu",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioPrimaryDark
                        )
                    }

                    Text(
                        text = "%${comment.frequencyPercentage} Sıklık",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPrimaryDark
                    )
                }
            }

            // The Comment Body Text
            Text(
                text = "\"${comment.commentText}\"",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 18.sp
            )

            // Sentiment & Category Pills
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(comment.sentiment.badgeColorHex).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = comment.sentiment.label,
                        color = Color(comment.sentiment.badgeColorHex),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioSurfaceVariant
                ) {
                    Text(
                        text = comment.category,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // AI Suggested Response Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StudioSurfaceElevated,
                border = BorderStroke(1.dp, StudioBorder.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = StudioPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Akıllı AI Yanıt Önerisi (${activeTone.label}):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onGenerateNewAiReply,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Yeniden Üret",
                                    tint = StudioPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = comment.aiSuggestedReply,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                currentInputText = comment.aiSuggestedReply
                                onApplyAiSuggestion()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = StudioPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Alana Aktar & Düzenle",
                                fontSize = 10.sp,
                                color = StudioPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // User Custom Reply Text Field ("Ben uygulamada cevap yazacağım")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Cevabınız (Manuel Yazabilir veya AI Önerisini Kullanabilirsiniz):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = currentInputText,
                    onValueChange = {
                        currentInputText = it
                        onUpdateCustomReply(it)
                    },
                    placeholder = {
                        Text(
                            text = "Örn: Merhaba, prompt listesini profil linkine ekledim...",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_reply_${comment.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPrimary,
                        unfocusedBorderColor = StudioBorder,
                        focusedContainerColor = StudioSurface,
                        unfocusedContainerColor = StudioSurface
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextPrimary),
                    minLines = 2,
                    maxLines = 4
                )

                // Quick Preset Chips for Faster Replies
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    val presets = listOf(
                        "📌 Prompt linki bio'da!",
                        "🚀 DM'den ilettim!",
                        "💡 Part 2 videosu yarın yayında!",
                        "❤️ Teşekkürler, keyifli çalışmalar!"
                    )
                    items(presets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioSurfaceVariant,
                            modifier = Modifier.clickable {
                                currentInputText = preset
                                onUpdateCustomReply(preset)
                            }
                        ) {
                            Text(
                                text = preset,
                                fontSize = 9.sp,
                                color = Color(0xFF1D192B),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Action Row: Send Custom Reply vs Instant AI Reply
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Instant AI Reply Button ("istediğim zaman da o cevaplayabilecek")
                Button(
                    onClick = {
                        onSendReply(comment.aiSuggestedReply, true)
                    },
                    modifier = Modifier.weight(1f).testTag("btn_reply_ai_${comment.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimaryLight)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = StudioPrimaryDark,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI ile Yanıtla",
                        fontSize = 11.sp,
                        color = StudioPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Send User Typed Reply
                Button(
                    onClick = {
                        val replyToSend = currentInputText.ifBlank { comment.aiSuggestedReply }
                        onSendReply(replyToSend, false)
                    },
                    modifier = Modifier.weight(1f).testTag("btn_reply_manual_${comment.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cevabı Gönder",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Status Badge if Replied
            AnimatedVisibility(
                visible = comment.isReplied,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (comment.repliedWithAi) "✅ Yanıtlandı (Akıllı AI Botu ile) • Panoya Kopyalandı" else "✅ Yanıtlandı (Manuel Yazıldı) • Panoya Kopyalandı",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddCommentDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var author by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("Instagram") }
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Yeni İzleyici Yorumu Ekle (Simülasyon)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Kullanıcı Adı") },
                    placeholder = { Text("Örn: Ali Kaya") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Platform:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Instagram", "YouTube", "TikTok").forEach { p ->
                        FilterChip(
                            selected = platform == p,
                            onClick = { platform = p },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Yorum Metni") },
                    placeholder = { Text("Örn: Videodaki kaynak linki nerede?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onAdd(author.ifBlank { "İzleyici" }, platform, commentText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
            ) {
                Text("Ekle ve AI Yanıtı Üret")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun ApiConnectionDialog(
    currentConfig: SocialApiConfig,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onFetchYouTube: (apiKey: String, videoInput: String) -> Unit,
    onFetchInstagram: (accessToken: String, mediaInput: String) -> Unit,
    onSaveConfig: (SocialApiConfig) -> Unit
) {
    var ytApiKey by remember { mutableStateOf(currentConfig.youtubeApiKey) }
    var ytVideoInput by remember { mutableStateOf(currentConfig.youtubeVideoIdOrUrl) }
    var ytOAuthToken by remember { mutableStateOf(currentConfig.youtubeOAuthToken) }

    var igAccessToken by remember { mutableStateOf(currentConfig.instagramAccessToken) }
    var igMediaInput by remember { mutableStateOf(currentConfig.instagramMediaIdOrUrl) }

    var selectedTab by remember { mutableStateOf(0) } // 0: YouTube, 1: Instagram

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Key,
                    contentDescription = null,
                    tint = StudioPrimary
                )
                Text(
                    text = "Gerçek Hesap & Canlı API Bağlantısı",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "YouTube Data API v3 ve Instagram Graph API ile gerçek kanalınızdan/hesabınızdan canlı izleyici yorumlarını çekin ve AI ile otomatik yanıtlayın.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedTab == 0) YouTubeRed.copy(alpha = 0.15f) else StudioSurfaceVariant,
                            border = BorderStroke(1.dp, if (selectedTab == 0) YouTubeRed else StudioBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 0 }
                        ) {
                            Text(
                                text = "YouTube Data API",
                                color = if (selectedTab == 0) YouTubeRed else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedTab == 1) InstagramPink.copy(alpha = 0.15f) else StudioSurfaceVariant,
                            border = BorderStroke(1.dp, if (selectedTab == 1) InstagramPink else StudioBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 1 }
                        ) {
                            Text(
                                text = "Instagram Graph API",
                                color = if (selectedTab == 1) InstagramPink else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                if (selectedTab == 0) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ytApiKey,
                                onValueChange = { ytApiKey = it },
                                label = { Text("Google Cloud YouTube API Key") },
                                placeholder = { Text("AIzaSy...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = ytVideoInput,
                                onValueChange = { ytVideoInput = it },
                                label = { Text("YouTube Video / Shorts Linki veya ID") },
                                placeholder = { Text("https://youtu.be/... veya dQw4w9WgXcQ") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = ytOAuthToken,
                                onValueChange = { ytOAuthToken = it },
                                label = { Text("Google OAuth Token (Canlı Yanıtlama için - İsteğe Bağlı)") },
                                placeholder = { Text("ya29.a0...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = { onFetchYouTube(ytApiKey, ytVideoInput) },
                                enabled = !isLoading && ytApiKey.isNotBlank() && ytVideoInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("YouTube'dan Çekiliyor...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("▶️ YouTube Yorumlarını Canlı Çek")
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = igAccessToken,
                                onValueChange = { igAccessToken = it },
                                label = { Text("Meta Graph API Access Token") },
                                placeholder = { Text("EAA...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = igMediaInput,
                                onValueChange = { igMediaInput = it },
                                label = { Text("Instagram Reels / Post Linki veya ID") },
                                placeholder = { Text("https://instagram.com/reel/C_... veya 1802...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = { onFetchInstagram(igAccessToken, igMediaInput) },
                                enabled = !isLoading && igAccessToken.isNotBlank() && igMediaInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = InstagramPink),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Instagram'dan Çekiliyor...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📸 Instagram Reels Yorumlarını Canlı Çek")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveConfig(
                        currentConfig.copy(
                            youtubeApiKey = ytApiKey,
                            youtubeVideoIdOrUrl = ytVideoInput,
                            youtubeOAuthToken = ytOAuthToken,
                            instagramAccessToken = igAccessToken,
                            instagramMediaIdOrUrl = igMediaInput
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
            ) {
                Text("Ayarları Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

