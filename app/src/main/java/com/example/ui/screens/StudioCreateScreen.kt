package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlatformTarget
import com.example.model.VideoNiche
import com.example.model.VideoTone
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioPrimaryLight
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSecondaryLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.VideoAutomationViewModel

@Composable
fun StudioCreateScreen(
    viewModel: VideoAutomationViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedNiche by remember { mutableStateOf(VideoNiche.AUTO_DETECT) }
    var promptTopic by remember { mutableStateOf("Yapay zeka ile günde 1 saat çalışarak pasif gelir elde etmenin 3 somut adımı") }
    var selectedPlatform by remember { mutableStateOf(PlatformTarget.ALL_IN_ONE) }
    var selectedTone by remember { mutableStateOf(VideoTone.ENERGETIC) }
    var durationSeconds by remember { mutableIntStateOf(30) }

    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationStage by viewModel.generationStage.collectAsState()
    val suggestedIdeas by viewModel.suggestedIdeas.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Section 1: Glassmorphic Minimalist Hero Header
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0x18FFFFFF), // Frosted glass
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x28FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = StudioPrimary.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.6f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = StudioPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "AI Video Motoru",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sıfırdan Özgün Senaryo & Çoklu Sahne",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StudioPrimary.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "Gemini Pro",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioPrimaryLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (isGenerating) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x24000000),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = StudioPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = generationStage.ifBlank { "Video ve sahneler kurgulanıyor..." },
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Transparent Prompt Input Box
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0x1AFFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2EFFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Video Konusu",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (promptTopic.isNotBlank()) {
                            Text(
                                text = "Temizle",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { promptTopic = "" }
                                    .padding(4.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = promptTopic,
                        onValueChange = { promptTopic = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prompt_input_field"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x22FFFFFF),
                            unfocusedContainerColor = Color(0x14FFFFFF),
                            focusedBorderColor = StudioPrimary,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        placeholder = {
                            Text(
                                "Videonuz ne hakkında olsun? (örn: Yapay zeka ile pasif gelir yolları)",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        minLines = 3,
                        maxLines = 4
                    )

                    // Fast Inspiration Ideas
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hızlı Fikir Seç",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = { viewModel.loadSuggestedIdeas(selectedNiche) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Fikirleri Yenile",
                                    tint = StudioSecondaryLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        val quickIdeas = suggestedIdeas.ifEmpty {
                            listOf(
                                "Yapay zeka ile günde 1 saat çalışarak pasif gelir elde etmenin 3 somut adımı",
                                "Alex Hormozi'nin asla reddedilemeyecek teklif oluşturma formülü",
                                "Bermuda Şeytan Üçgeni'nde kaybolan 19. Uçuş Filosunun gizemi",
                                "Geleceğin 3 büyük yapay zeka devrimi ve insanların bilmediği gerçekler"
                            )
                        }

                        quickIdeas.take(3).forEach { idea ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x12FFFFFF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { promptTopic = idea }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = StudioSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = idea,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Video Türü / Niche (Sade Şeffaf Çipler)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0x1AFFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2EFFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Video Türü",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedNiche == VideoNiche.AUTO_DETECT) "🤖 AI Otomatik Seçer" else "✋ Seçildi",
                            color = if (selectedNiche == VideoNiche.AUTO_DETECT) StudioPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val niches = listOf(
                            VideoNiche.AUTO_DETECT,
                            VideoNiche.CRYPTO_FINANCE,
                            VideoNiche.HORMOZI_BUSINESS,
                            VideoNiche.MYSTERY_STORY,
                            VideoNiche.TECH_AI,
                            VideoNiche.MOTIVATION,
                            VideoNiche.SCIENCE_SPACE
                        )

                        items(niches) { niche ->
                            val isSelected = selectedNiche == niche
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) StudioPrimary.copy(alpha = 0.35f) else Color(0x14FFFFFF),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) StudioPrimary else Color(0x2AFFFFFF)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        selectedNiche = niche
                                        if (niche != VideoNiche.AUTO_DETECT && niche.defaultPrompt.isNotBlank()) {
                                            promptTopic = niche.defaultPrompt
                                        }
                                        viewModel.loadSuggestedIdeas(niche)
                                    }
                            ) {
                                Text(
                                    text = niche.label,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Video Süresi & Format (Sade Şeffaf Seçici)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0x1AFFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2EFFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Video Süresi",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "9:16 Dikey Format (Reels & Shorts)",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 30, 60).forEach { sec ->
                            val isSelected = durationSeconds == sec
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) StudioPrimary.copy(alpha = 0.35f) else Color(0x14FFFFFF),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) StudioPrimary else Color(0x2AFFFFFF)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { durationSeconds = sec }
                            ) {
                                Text(
                                    text = "${sec}s",
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Transparent Info Card answering user questions
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x14FFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x24FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Her Seferinde %100 Yeni ve Özgün Video",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "• Gemini AI her seferinde konunuza özel yeni senaryo, kancalar ve altyazılar yazar.\n• Tek bir oda rengi yerine Finans, Teknoloji, Gizem, Uzay ve İş temalarında bağımsız sahneler ve parçacık efektleri dinamik eşleştirilir.\n• API anahtarlarınız kaydedildiğinde tüm video render ve SEO paketleme tam otomatik çalışır.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Section 6: Action Buttons (Large Glowing Generate Button)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        viewModel.generateVideo(
                            topic = promptTopic,
                            niche = selectedNiche,
                            platformTarget = selectedPlatform,
                            tone = selectedTone,
                            durationSeconds = durationSeconds
                        )
                        onNavigateToEditor()
                    },
                    enabled = !isGenerating && promptTopic.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_automation_button")
                        .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = StudioPrimary),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Video Üretiliyor...",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✨ Videoyu Üret (AI Senaryo & Kurgu)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                // Batch Week Generator button (Transparent Outlined)
                OutlinedButton(
                    onClick = {
                        viewModel.batchGenerateWeekTopics(selectedNiche)
                        onNavigateToEditor()
                    },
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = StudioSecondaryLight,
                        containerColor = Color(0x10FFFFFF)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = StudioSecondaryLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Haftalık 3 Video Otomasyonu Oluştur",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
