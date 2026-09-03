package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.model.CaptionStyle
import com.example.model.HookType
import com.example.model.SceneItem
import com.example.model.SubtitlePosition
import com.example.model.TransitionEffect
import com.example.model.VideoAspectRatio
import com.example.model.VideoProject
import com.example.ui.components.TimelineControls
import com.example.ui.components.VideoPlayerCanvas
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioPrimaryLight
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSecondaryLight
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTertiary
import com.example.ui.theme.SubtitleHighlightYellow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.VideoAutomationViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoEditorScreen(
    viewModel: VideoAutomationViewModel,
    onNavigateToPublish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSceneIndex by viewModel.currentSceneIndex.collectAsState()
    val sceneProgress by viewModel.sceneProgress.collectAsState()
    val totalProgressSeconds by viewModel.totalProgressSeconds.collectAsState()

    var selectedEditorTab by remember { mutableIntStateOf(0) } // 0: Altyazı & Kanca, 1: Algoritma Kalkanı, 2: Sahneler, 3: Ses & TTS
    var editingSceneIndex by remember { mutableStateOf<Int?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val project = activeProject
    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Proje bulunamadı. Lütfen yeni video oluşturun.", color = TextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Video Preview Player Canvas
        item {
            VideoPlayerCanvas(
                project = project,
                currentSceneIndex = currentSceneIndex,
                currentSceneProgress = sceneProgress,
                isPlaying = isPlaying,
                onTogglePlay = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        // Timeline Scrubber & Step Controls
        item {
            TimelineControls(
                totalDuration = project.durationSeconds,
                currentProgressTime = totalProgressSeconds,
                isPlaying = isPlaying,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { seconds ->
                    // calculate approx scene
                    var cum = 0f
                    var targetIdx = 0
                    for (i in project.script.scenes.indices) {
                        cum += project.script.scenes[i].durationSeconds
                        if (seconds <= cum) {
                            targetIdx = i
                            break
                        }
                    }
                    viewModel.seekToScene(targetIdx)
                },
                onStepPrev = { viewModel.stepScene(false) },
                onStepNext = { viewModel.stepScene(true) },
                scenes = project.script.scenes,
                currentSceneIndex = currentSceneIndex
            )
        }

        // Aspect Ratio & Format Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VideoAspectRatio.values().forEach { aspect ->
                    val isSelected = project.aspectRatio == aspect
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) StudioPrimary else StudioSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) StudioPrimaryLight else StudioBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.updateAspectRatio(aspect) }
                    ) {
                        Text(
                            text = aspect.label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Editor Sub-tabs: 1. Altyazı & Stil, 2. Sahne Metinleri, 3. Ses & Müzik
        item {
            TabRow(
                selectedTabIndex = selectedEditorTab,
                containerColor = StudioSurfaceElevated,
                contentColor = StudioPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedEditorTab]),
                        color = StudioPrimary
                    )
                },
                divider = {},
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedEditorTab == 0,
                    onClick = { selectedEditorTab = 0 },
                    text = { Text("Altyazı & Kanca", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Subtitles, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedEditorTab == 1,
                    onClick = { selectedEditorTab = 1 },
                    text = { Text("Algoritma", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedEditorTab == 2,
                    onClick = { selectedEditorTab = 2 },
                    text = { Text("Sahneler (${project.script.scenes.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedEditorTab == 3,
                    onClick = { selectedEditorTab = 3 },
                    text = { Text("Ses & TTS", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // Tab Content: 0. Altyazı & Stil
        if (selectedEditorTab == 0) {
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
                            text = "Viral Altyazı Animasyon Şablonu",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CaptionStyle.values().forEach { style ->
                                val isSelected = project.styleSettings.captionStyle == style
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateCaptionStyle(style) },
                                    label = {
                                        Text(
                                            text = style.label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudioPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = StudioSurfaceVariant,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Altyazı Konumu (Ekranda)",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubtitlePosition.values().forEach { pos ->
                                val isSelected = project.styleSettings.subtitlePosition == pos
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) StudioSecondary else StudioSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.updateSubtitlePosition(pos) }
                                ) {
                                    Text(
                                        text = when (pos) {
                                            SubtitlePosition.CENTER -> "Ortada (Reels)"
                                            SubtitlePosition.LOWER_THIRD -> "Alt Kısım"
                                            SubtitlePosition.UPPER_THIRD -> "Üst Kısım"
                                        },
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // MrBeast Bouncing Emojis Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚡ Zıplayan Emojiler (MrBeast Modu)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Kelimelere göre dinamik 💰, 🔥, 🧠, 🎯 emojileri ekler ve zıplatır.",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = project.styleSettings.bouncingEmojisEnabled,
                                onCheckedChange = { viewModel.toggleBouncingEmojis() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StudioPrimary,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = StudioBorder
                                )
                            )
                        }
                    }
                }
            }

            // Tab 0 Sub-Item: A/B Hook Testing Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.5f))
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
                                    imageVector = Icons.Default.CompareArrows,
                                    contentDescription = null,
                                    tint = StudioPrimaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "A/B Viral Kanca (Hook) Testi",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StudioPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Aktif: Kanca ${project.splitHooks.activeHook.name}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioPrimaryLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Videonuzun ilk 3 saniyesini iki farklı psikolojik açıyla test edin. Seçtiğiniz kanca ilk sahneye ve video başlığına anında entegre edilir.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        // Hook Option A: Curiosity / Mystery
                        val isHookA = project.splitHooks.activeHook == HookType.A
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchHookType(HookType.A)
                                    Toast.makeText(context, "Kanca A (Merak Açısı) seçildi!", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isHookA) StudioPrimary.copy(alpha = 0.15f) else StudioSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isHookA) 1.5.dp else 1.dp,
                                if (isHookA) StudioPrimary else StudioBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Kanca A (Merak & Gizem)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isHookA) StudioPrimaryLight else TextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SuccessGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = project.splitHooks.hookARetentionRate,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "\"${project.splitHooks.hookA}\"",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Hook Option B: FOMO / Urgency
                        val isHookB = project.splitHooks.activeHook == HookType.B
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchHookType(HookType.B)
                                    Toast.makeText(context, "Kanca B (FOMO / Aciliyet) seçildi!", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isHookB) StudioSecondary.copy(alpha = 0.15f) else StudioSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isHookB) 1.5.dp else 1.dp,
                                if (isHookB) StudioSecondary else StudioBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Kanca B (FOMO & Kayıp Korkusu)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isHookB) StudioSecondaryLight else TextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SubtitleHighlightYellow.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = project.splitHooks.hookBRetentionRate,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SubtitleHighlightYellow,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "\"${project.splitHooks.hookB}\"",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Content: 1. Algoritma Ceza Kalkanı Raporu
        if (selectedEditorTab == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header Score Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Algoritma Ceza Kalkanı",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "YouTube & Meta Yapay Zeka Politikası Uyumu",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SuccessGreen.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, SuccessGreen)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "%${project.algorithmSafety.safetyScore}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SuccessGreen
                                    )
                                    Text(
                                        text = "GÜVENLİK",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }

                        // Badges Row
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (badge in project.algorithmSafety.complianceBadges) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StudioSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, StudioBorder)
                                ) {
                                    Text(
                                        text = "✓ $badge",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Summary Statistics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = StudioSurface
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "CEZA RİSKİ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Text(text = project.algorithmSafety.penaltyRiskLevel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = StudioSurface
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "İZLEYİCİ TUTMA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Text(text = project.algorithmSafety.retentionPrediction, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioPrimaryLight)
                                }
                            }
                        }

                        // YouTube Studio Synthetic Declaration Copy Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StudioSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "YouTube Studio Bildirim Metni",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(project.algorithmSafety.youtubeSyntheticDeclaration))
                                            Toast.makeText(context, "Bildirim metni kopyalandı!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Kopyala",
                                            tint = StudioPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = project.algorithmSafety.youtubeSyntheticDeclaration,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Safety Tips Checklist
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Önerilen Algoritma Stratejisi:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            for (tip in project.algorithmSafety.safetyTips) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(text = "🛡️", fontSize = 11.sp)
                                    Text(
                                        text = tip,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab Content: 2. Sahne Kurgusu Listesi
        if (selectedEditorTab == 2) {
            itemsIndexed(project.script.scenes) { index, scene ->
                val isCurrent = index == currentSceneIndex
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.seekToScene(index)
                            editingSceneIndex = if (editingSceneIndex == index) null else index
                        }
                        .border(
                            width = if (isCurrent) 1.5.dp else 0.5.dp,
                            color = if (isCurrent) StudioPrimaryLight else StudioBorder,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) StudioSurfaceElevated else StudioSurface
                    )
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isCurrent) StudioPrimary else StudioSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "Sahne ${index + 1} (${String.format("%.1fs", scene.durationSeconds)})",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StudioSurfaceVariant
                                ) {
                                    Text(
                                        text = scene.transitionType.label.take(12),
                                        color = StudioSecondaryLight,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StudioTertiary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = scene.soundEffectCue,
                                        color = StudioTertiary,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "🗣️ \"${scene.narrationText}\"",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Text(
                            text = "🎬 Görsel: ${scene.visualDescription}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Inline Scene Editor Dropdown
                        if (editingSceneIndex == index) {
                            Spacer(modifier = Modifier.height(4.dp))
                            var editableText by remember { mutableStateOf(scene.narrationText) }

                            OutlinedTextField(
                                value = editableText,
                                onValueChange = {
                                    editableText = it
                                    viewModel.updateSceneNarration(index, it)
                                },
                                label = { Text("Seslendirme & Altyazı Metni", color = StudioPrimaryLight) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = StudioSurfaceVariant,
                                    unfocusedContainerColor = StudioSurfaceVariant,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                minLines = 2
                            )

                            // Quick Transition & Visual switchers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TransitionEffect.values().take(3).forEach { trans ->
                                    val isTransSelected = scene.transitionType == trans
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isTransSelected) StudioPrimary else StudioSurfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.updateSceneTransition(index, trans) }
                                    ) {
                                        Text(
                                            text = trans.name,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab Content: 3. Ses & Müzik (İnsansı TTS)
        if (selectedEditorTab == 3) {
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
                            text = "Seslendirme & İnsansı Konuşma (TTS)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Humanized Breathing & Tone Modulation Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🗣️ İnsansı Nefes & Ritmik Tonlama",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Robotik sesi kırar; soru ve maddelerde insansı mikro-pause'lar ekler.",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = project.styleSettings.humanizedBreathing,
                                onCheckedChange = { viewModel.toggleHumanizedBreathing() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StudioSecondary,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = StudioBorder
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = StudioSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "AI Anlatıcı: ${project.styleSettings.voiceType}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Android yerel Text-To-Speech Türkçe / İngilizce motoru ile senkronize seslendirme",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = StudioTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Telif Hakkı Olmayan Müzik: ${project.styleSettings.bgMusicName}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Instagram Reels ve YouTube Shorts için telifsiz viral ritimler",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Bottom CTA: Proceed to Publish & SEO Package
        item {
            Button(
                onClick = onNavigateToPublish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("go_to_publish_pack_button")
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = StudioPrimary),
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Publish, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yükleme & SEO Paketine Geç (Başlık & Hashtagler) ➔",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
