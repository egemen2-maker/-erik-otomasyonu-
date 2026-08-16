package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.VideoProject
import com.example.ui.components.CopyActionButton
import com.example.ui.components.ExportSimulationModal
import com.example.ui.components.HashtagCloudView
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.InstagramPurple
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioPrimaryLight
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSecondaryLight
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTertiary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.VideoAutomationViewModel

@Composable
fun PublishPackageScreen(
    viewModel: VideoAutomationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProject.collectAsState()
    var selectedPlatformTab by remember { mutableIntStateOf(0) } // 0: Instagram Reels, 1: YouTube Shorts & Video
    var showExportModal by remember { mutableStateOf(false) }

    val project = activeProject
    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aktif proje bulunamadı.", color = TextSecondary)
        }
        return
    }

    val igData = project.publishPack.instagramPack
    val ytData = project.publishPack.youtubePack

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Yükleme Öncesi SEO & İçerik Paketi",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${project.topic} • ${project.durationSeconds}s Video",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { showExportModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paketi Çıkar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Platform Tab Switcher: Instagram vs YouTube
            item {
                TabRow(
                    selectedTabIndex = selectedPlatformTab,
                    containerColor = StudioSurfaceElevated,
                    contentColor = if (selectedPlatformTab == 0) InstagramPink else YouTubeRed,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedPlatformTab]),
                            color = if (selectedPlatformTab == 0) InstagramPink else YouTubeRed
                        )
                    },
                    divider = {},
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedPlatformTab == 0,
                        onClick = { selectedPlatformTab = 0 },
                        text = {
                            Text(
                                text = "Instagram Reels Paketi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPlatformTab == 0) InstagramPink else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedPlatformTab == 1,
                        onClick = { selectedPlatformTab = 1 },
                        text = {
                            Text(
                                text = "YouTube Shorts & SEO",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPlatformTab == 1) YouTubeRed else TextSecondary
                            )
                        }
                    )
                }
            }

            // INSTAGRAM TAB CONTENT
            if (selectedPlatformTab == 0) {
                // 1. Caption & Copy Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = InstagramPink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Reels Açıklama Metni (Caption)",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                CopyActionButton(
                                    textToCopy = "${igData.caption}\n\n${igData.fullHashtagString}",
                                    label = "Açıklamayı Kopyala",
                                    isPrimary = true
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StudioSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = igData.caption,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Hashtags Cloud
                item {
                    HashtagCloudView(
                        hashtags = igData.topHashtags + igData.nicheHashtags,
                        onCopyAll = {},
                        title = "Viral & Niş Hashtagler (${(igData.topHashtags + igData.nicheHashtags).size} Adet)",
                        tagColor = InstagramPink
                    )
                }

                // 3. Audio & Best Posting Time
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = StudioSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text("Önerilen Trend Ses / Müzik:", color = TextMuted, fontSize = 11.sp)
                                    Text(igData.audioRecommendation, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = StudioTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text("En Yüksek Etkileşim Saati:", color = TextMuted, fontSize = 11.sp)
                                    Text(igData.bestPostingTime, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (igData.firstCommentPin.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = StudioPrimaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Sabitlenecek Yorum:", color = TextMuted, fontSize = 11.sp)
                                        Text(igData.firstCommentPin, color = TextPrimary, fontSize = 12.sp)
                                    }
                                    CopyActionButton(
                                        textToCopy = igData.firstCommentPin,
                                        label = "Kopyala"
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Share to Instagram Action Button
                item {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, project.topic)
                                putExtra(Intent.EXTRA_TEXT, "${igData.caption}\n\n${igData.fullHashtagString}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Instagram / Sosyal Medyada Paylaş"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("share_instagram_pack_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = InstagramPink),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Instagram'da Paylaş (Açıklama & Etiketler Hazır)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // YOUTUBE TAB CONTENT
            if (selectedPlatformTab == 1) {
                // 1. High-CTR Title Options
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Yüksek CTR Başlık Seçenekleri (A/B Testi)",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            ytData.titleOptions.forEach { titleOpt ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = StudioSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = titleOpt,
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        CopyActionButton(
                                            textToCopy = titleOpt,
                                            label = "Kopyala"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. SEO Description Box with Timestamps
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
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
                                Text(
                                    text = "YouTube Açıklaması & Zaman Damgaları",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                CopyActionButton(
                                    textToCopy = ytData.description,
                                    label = "Açıklamayı Kopyala",
                                    isPrimary = true
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StudioSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = ytData.description,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // 3. YouTube Tags (Comma separated)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Tag, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "YouTube Arama Etiketleri (${ytData.tags.size})",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                CopyActionButton(
                                    textToCopy = ytData.tagsCommaSeparated,
                                    label = "Etiketleri Kopyala",
                                    isPrimary = true
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StudioSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = ytData.tagsCommaSeparated,
                                    color = StudioSecondaryLight,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Thumbnail Concept Prompt
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = StudioPrimaryLight, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "AI Thumbnail / Kapak Resmi Promptu",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                CopyActionButton(
                                    textToCopy = ytData.thumbnailPrompt,
                                    label = "Promptu Kopyala"
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StudioSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = ytData.thumbnailPrompt,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                // 5. Share to YouTube Action Button
                item {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, ytData.selectedTitle)
                                putExtra(Intent.EXTRA_TEXT, "${ytData.selectedTitle}\n\n${ytData.description}\n\nEtiketler:\n${ytData.tagsCommaSeparated}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "YouTube / Sosyal Medyada Paylaş"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("share_youtube_pack_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("YouTube Shorts'a Aktar (Başlık & SEO Hazır)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Export Render Simulation Modal
        if (showExportModal) {
            ExportSimulationModal(
                project = project,
                onDismiss = { showExportModal = false },
                onSharePackage = {
                    val fullBundle = """
                        ======================================
                        AUTOREEL STUDIO OTOMASYON PAKETİ
                        ======================================
                        Konu: ${project.topic}
                        Kategori: ${project.niche.label}
                        Süre: ${project.durationSeconds} saniye
                        Viral Kanca: ${project.script.hookLine}

                        --- INSTAGRAM REELS ---
                        Açıklama:
                        ${igData.caption}

                        Hashtagler:
                        ${igData.fullHashtagString}

                        Trend Ses: ${igData.audioRecommendation}
                        En İyi Yayın Saati: ${igData.bestPostingTime}

                        --- YOUTUBE SHORTS & SEO ---
                        Başlık Seçenekleri:
                        ${ytData.titleOptions.joinToString("\n")}

                        Açıklama & Zaman Damgaları:
                        ${ytData.description}

                        YouTube Etiketleri:
                        ${ytData.tagsCommaSeparated}

                        Kapak Görseli AI Promptu:
                        ${ytData.thumbnailPrompt}
                    """.trimIndent()

                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "AutoReel Paket: ${project.topic}")
                        putExtra(Intent.EXTRA_TEXT, fullBundle)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Tüm Otomasyon Paketini Dışa Aktar"))
                }
            )
        }
    }
}
