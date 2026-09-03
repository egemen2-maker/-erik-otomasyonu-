package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.VideoNiche
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.InstagramPurple
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioPrimaryLight
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSecondaryLight
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.VideoAutomationViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileStudioScreen(
    viewModel: VideoAutomationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profileData.collectAsState()
    val isInstagram = profile.platform == "Instagram"

    var selectedNicheForGen by remember { mutableStateOf(VideoNiche.TECH_AI) }
    var copiedItemMessage by remember { mutableStateOf<String?>(null) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label panoya kopyalandı!", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Screen Title & Description
            Text(
                text = "Profil & Kanal Stüdyosu",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "YouTube ve Instagram kanallarınız için sade, şeffaf ve viral dönüşüm odaklı profil mimarisi",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Platform Switcher Segmented Control (Frosted Glass)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0x20FFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x30FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Instagram Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.switchProfilePlatform("Instagram") },
                        color = if (isInstagram) Color(0x50E1306C) else Color.Transparent,
                        border = if (isInstagram) androidx.compose.foundation.BorderStroke(1.dp, InstagramPink) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📷", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Instagram Reels",
                                color = if (isInstagram) Color.White else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isInstagram) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    // YouTube Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.switchProfilePlatform("YouTube") },
                        color = if (!isInstagram) Color(0x50FF0000) else Color.Transparent,
                        border = if (!isInstagram) androidx.compose.foundation.BorderStroke(1.dp, YouTubeRed) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("▶️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "YouTube Shorts",
                                color = if (!isInstagram) Color.White else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (!isInstagram) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Live Profile Mockup Preview Card (Ultra-sleek Frosted Glass)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x28FFFFFF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40FFFFFF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isInstagram) "CANLI INSTAGRAM ÖNİZLEMESİ" else "CANLI YOUTUBE KANAL ÖNİZLEMESİ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInstagram) InstagramPink else YouTubeRed,
                            letterSpacing = 0.5.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x25FFFFFF)
                        ) {
                            Text(
                                text = "Canlı Görünüm",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isInstagram) {
                        // Instagram Profile Mockup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Avatar with Rainbow Gradient Border
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(0xFF833AB4),
                                                Color(0xFFFD1D1D),
                                                Color(0xFFFCB045),
                                                Color(0xFF833AB4)
                                            )
                                        ),
                                        CircleShape
                                    )
                                    .padding(2.5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF141724))
                                    .padding(2.dp)
                                    .clip(CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.scene_cyberpunk_tech),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Stats Counter
                            Row(
                                modifier = Modifier.weight(1f).padding(start = 20.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("68", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text("Gönderi", fontSize = 11.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("42.8K", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text("Takipçi", fontSize = 11.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("124", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text("Takip", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Name, Category & Bio
                        Text(
                            text = profile.channelName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = profile.category,
                            fontSize = 11.sp,
                            color = StudioPrimaryLight,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profile.bio,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Link Preview
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = StudioSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = profile.websiteLink,
                                fontSize = 11.sp,
                                color = StudioSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Instagram Story Highlights
                        Text(
                            text = "Öne Çıkan Hikayeler (Highlights):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            profile.highlightTitles.take(4).forEach { hl ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0x35FFFFFF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FFFFFF)),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = hl.take(2),
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = hl.drop(2).trim(),
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        // YouTube Channel Mockup
                        // 16:9 Banner Mockup
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.scene_luxury_motivation),
                                contentDescription = "YouTube Banner",
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            Text(
                                text = profile.channelName.uppercase(),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Channel info row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = YouTubeRed,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("▶", color = Color.White, fontSize = 24.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.channelName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${profile.handle} • 86.4K abone • 120 video",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = profile.bio.replace("\n", " ").take(65) + "...",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // YouTube Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("Abone Ol", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {},
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x50FFFFFF))
                            ) {
                                Text("Katıl", fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // AI 1-Tap Profile Generator
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
                        Text("✨", fontSize = 16.sp)
                        Text(
                            text = "Yapay Zeka ile Nişe Göre Profil Üret",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Kanal konunuza en uygun bio, kanca sloganı ve kullanıcı adını otomatik oluşturur:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            VideoNiche.TECH_AI to "🤖 Yapay Zeka",
                            VideoNiche.CRYPTO_FINANCE to "📈 Kripto/Finans",
                            VideoNiche.HORMOZI_BUSINESS to "💰 Hormozi Girişim",
                            VideoNiche.MOTIVATION to "⚔️ Disiplin/Zihin",
                            VideoNiche.SCIENCE_SPACE to "🔭 Uzay & Bilim"
                        ).forEach { (niche, label) ->
                            val isSel = selectedNicheForGen == niche
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    selectedNicheForGen = niche
                                    viewModel.generateProfileForNiche(niche)
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = StudioSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Editable Profile Fields Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Profil Bilgilerini Düzenle",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Channel Name
                    OutlinedTextField(
                        value = profile.channelName,
                        onValueChange = { viewModel.updateProfileChannelName(it) },
                        label = { Text("Kanal / Hesap Adı", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_profile_name"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimary,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Handle with Quick Suggestions
                    OutlinedTextField(
                        value = profile.handle,
                        onValueChange = { viewModel.updateProfileHandle(it) },
                        label = { Text("Kullanıcı Adı / Handle", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_profile_handle"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimary,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Suggested handles
                    Text("Önerilen Boşta / Akılda Kalıcı Handle'lar:", fontSize = 11.sp, color = TextSecondary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        profile.suggestedHandles.forEach { sHandle ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StudioSurfaceVariant,
                                modifier = Modifier.clickable {
                                    viewModel.updateProfileHandle(sHandle)
                                }
                            ) {
                                Text(
                                    text = sHandle,
                                    fontSize = 11.sp,
                                    color = StudioSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Bio field (multi-line)
                    OutlinedTextField(
                        value = profile.bio,
                        onValueChange = { viewModel.updateProfileBio(it) },
                        label = { Text("Biyografi (Emoji & Kanca İçeren)", fontSize = 12.sp) },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("input_profile_bio"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimary,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Website link
                    OutlinedTextField(
                        value = profile.websiteLink,
                        onValueChange = { viewModel.updateProfileLink(it) },
                        label = { Text("Web Sitesi / Linktree Bağlantısı", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_profile_link"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimary,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // AI Avatar & Banner Prompt Generator Card
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
                    Text(
                        text = "🎨 AI Görsel Üretim Promptları (Avatar & Banner)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Midjourney, DALL-E 3 veya Flux ile kanalınıza profesyonel profil resmi ve 16:9 banner oluşturmak için kullanın:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    // Avatar Prompt Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👤 Avatar Promptu:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioPrimaryLight)
                                IconButton(
                                    onClick = { copyToClipboard(profile.avatarPrompt, "Avatar Promptu") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = StudioPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = profile.avatarPrompt,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    // Banner Prompt Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🖼️ YouTube 16:9 Banner Promptu:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioSecondaryLight)
                                IconButton(
                                    onClick = { copyToClipboard(profile.bannerPrompt, "Banner Promptu") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = StudioSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = profile.bannerPrompt,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Action: Copy All Profile Settings
        item {
            Button(
                onClick = {
                    val fullProfileText = """
                    ${profile.platform} Kanal Profili:
                    Adı: ${profile.channelName}
                    Handle: ${profile.handle}
                    Kategori: ${profile.category}
                    
                    Biyografi:
                    ${profile.bio}
                    
                    Link: ${profile.websiteLink}
                    
                    Öne Çıkanlar:
                    ${profile.highlightTitles.joinToString(" • ")}
                    """.trimIndent()
                    copyToClipboard(fullProfileText, "Tüm Profil Bilgileri")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_copy_all_profile"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tüm Profil Bilgilerini Kopyala & Uygula",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
