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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionStyle
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

    var selectedEditorTab by remember { mutableIntStateOf(0) } // 0: Altyazı & Stil, 1: Sahne Kurgusu, 2: Ses & Müzik
    var editingSceneIndex by remember { mutableStateOf<Int?>(null) }

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
                    text = { Text("Altyazı Stili", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Subtitles, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedEditorTab == 1,
                    onClick = { selectedEditorTab = 1 },
                    text = { Text("Sahneler (${project.script.scenes.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedEditorTab == 2,
                    onClick = { selectedEditorTab = 2 },
                    text = { Text("Ses & Efekt", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp)) }
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
                    }
                }
            }
        }

        // Tab Content: 1. Sahne Kurgusu Listesi
        if (selectedEditorTab == 1) {
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

        // Tab Content: 2. Ses & Müzik
        if (selectedEditorTab == 2) {
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
                            text = "Seslendirme & Arka Plan Müziği",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

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
