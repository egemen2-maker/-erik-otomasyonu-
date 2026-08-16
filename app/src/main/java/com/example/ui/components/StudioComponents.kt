package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CaptionStyle
import com.example.model.SceneItem
import com.example.model.SubtitlePosition
import com.example.model.TransitionEffect
import com.example.model.VideoAspectRatio
import com.example.model.VideoProject
import com.example.model.VideoStyleSettings
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
import com.example.ui.theme.SubtitleHighlightCyan
import com.example.ui.theme.SubtitleHighlightPink
import com.example.ui.theme.SubtitleHighlightYellow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun VideoPlayerCanvas(
    project: VideoProject,
    currentSceneIndex: Int,
    currentSceneProgress: Float,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentScene = project.script.scenes.getOrNull(currentSceneIndex)
        ?: project.script.scenes.firstOrNull()

    val sceneBackgroundRes = when ((currentScene?.bgThemeIndex ?: 0) % 3) {
        0 -> R.drawable.scene_cyberpunk_tech
        1 -> R.drawable.scene_luxury_motivation
        2 -> R.drawable.scene_space_nebula
        else -> R.drawable.scene_cyberpunk_tech
    }

    // Dynamic camera scale / transition animation
    val infiniteTransition = rememberInfiniteTransition(label = "camera")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val transitionScale = when (currentScene?.transitionType) {
        TransitionEffect.ZOOM_IN -> 1.0f + (currentSceneProgress * 0.08f)
        TransitionEffect.WHIP_PAN -> 1.02f
        TransitionEffect.GLITCH -> 1.01f + (sin(currentSceneProgress * 20.0).toFloat() * 0.02f)
        else -> 1.0f
    }

    val aspect = project.aspectRatio
    val targetAspect = when (aspect) {
        VideoAspectRatio.PORTRAIT_9_16 -> 9f / 16f
        VideoAspectRatio.LANDSCAPE_16_9 -> 16f / 9f
        VideoAspectRatio.SQUARE_1_1 -> 1f / 1f
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(20.dp))
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = StudioPrimary),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(targetAspect)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Background Visual Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pulseScale * transitionScale
                        scaleY = pulseScale * transitionScale
                    }
            ) {
                Image(
                    painter = painterResource(id = sceneBackgroundRes),
                    contentDescription = currentScene?.visualDescription ?: "Scene background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark vignette gradient overlay for crisp subtitle contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )
            }

            // Top Status Bar: Viral Score & Timecode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Viral Hook Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = StudioPrimaryLight,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${project.hookScore} Viral Puan",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Scene Indicator & SFX Cue
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!currentScene?.soundEffectCue.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioTertiary.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = "⚡ ${currentScene?.soundEffectCue}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${(currentSceneIndex + 1)} / ${project.script.scenes.size}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Top Hook Banner (Optional Overlay)
            if (project.styleSettings.showHookBanner && currentSceneIndex == 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 46.dp, start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = YouTubeRed.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "🔥 ${project.script.hookLine}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Central / Dynamic Subtitle Box
            val subtitleAlignment = when (project.styleSettings.subtitlePosition) {
                SubtitlePosition.CENTER -> Alignment.Center
                SubtitlePosition.LOWER_THIRD -> Alignment.BottomCenter
                SubtitlePosition.UPPER_THIRD -> Alignment.TopCenter
            }

            val subtitlePaddingBottom = when (project.styleSettings.subtitlePosition) {
                SubtitlePosition.LOWER_THIRD -> 64.dp
                else -> 0.dp
            }

            val subtitlePaddingTop = when (project.styleSettings.subtitlePosition) {
                SubtitlePosition.UPPER_THIRD -> 64.dp
                else -> 0.dp
            }

            Box(
                modifier = Modifier
                    .align(subtitleAlignment)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = subtitlePaddingBottom,
                        top = subtitlePaddingTop
                    )
            ) {
                currentScene?.let { scene ->
                    KaraokeSubtitleBox(
                        subtitleText = scene.onScreenSubtitle.ifBlank { scene.narrationText },
                        highlightWords = scene.textHighlightWords,
                        progress = currentSceneProgress,
                        style = project.styleSettings.captionStyle,
                        fontSizeSp = project.styleSettings.fontSizeSp
                    )
                }
            }

            // Bottom Waveform & Track Info
            if (project.styleSettings.showWaveform) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = StudioSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = project.styleSettings.bgMusicName,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Simulated live audio level meter
                    AnimatedAudioWaveform(isPlaying = isPlaying)
                }
            }

            // Tap Overlay Play/Pause icon button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onTogglePlay() },
                contentAlignment = Alignment.Center
            ) {
                if (!isPlaying) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, StudioPrimary),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Oynat",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KaraokeSubtitleBox(
    subtitleText: String,
    highlightWords: List<String>,
    progress: Float,
    style: CaptionStyle,
    fontSizeSp: Int = 22
) {
    val words = remember(subtitleText) { subtitleText.split(" ").filter { it.isNotBlank() } }
    val activeWordIndex = (progress * words.size).toInt().coerceIn(0, (words.size - 1).coerceAtLeast(0))

    when (style) {
        CaptionStyle.KARAOKE_POP -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildKaraokeSpannable(words, activeWordIndex, highlightWords),
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = (fontSizeSp + 6).sp
                    )
                }
            }
        }

        CaptionStyle.MR_BEAST_BOLD -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SubtitleHighlightYellow,
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = subtitleText.uppercase(),
                    color = Color.Black,
                    fontSize = (fontSizeSp + 2).sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    letterSpacing = 1.sp
                )
            }
        }

        CaptionStyle.GLOW_NEON -> {
            Text(
                text = subtitleText,
                color = SubtitleHighlightCyan,
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .shadow(16.dp, spotColor = SubtitleHighlightCyan)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        CaptionStyle.BOXED_BADGE -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = StudioPrimary,
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = subtitleText,
                    color = Color.White,
                    fontSize = fontSizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        CaptionStyle.MINIMAL_CLEAN -> {
            Text(
                text = subtitleText,
                color = Color.White,
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun buildKaraokeSpannable(
    words: List<String>,
    activeIndex: Int,
    highlightKeywords: List<String>
): androidx.compose.ui.text.AnnotatedString {
    val cleanHighlightKeywords = highlightKeywords.map { it.uppercase().trim() }
    return androidx.compose.ui.text.buildAnnotatedString {
        words.forEachIndexed { index, word ->
            val isCurrent = index == activeIndex
            val isPassed = index < activeIndex
            val isKeyword = cleanHighlightKeywords.any { word.uppercase().contains(it) }

            val textColor = when {
                isCurrent -> if (isKeyword) SubtitleHighlightPink else SubtitleHighlightYellow
                isPassed -> Color.White
                else -> Color.White.copy(alpha = 0.45f)
            }

            val fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold

            pushStyle(
                androidx.compose.ui.text.SpanStyle(
                    color = textColor,
                    fontWeight = fontWeight,
                    background = if (isCurrent) Color.White.copy(alpha = 0.15f) else Color.Transparent
                )
            )
            append(word)
            pop()

            if (index < words.size - 1) {
                append(" ")
            }
        }
    }
}

@Composable
fun AnimatedAudioWaveform(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = if (isPlaying) 18f else 4f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = if (isPlaying) 22f else 6f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = if (isPlaying) 16f else 5f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = if (isPlaying) 20f else 4f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "b4"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(bar1, bar2, bar3, bar4).forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StudioSecondary)
            )
        }
    }
}

@Composable
fun TimelineControls(
    totalDuration: Int,
    currentProgressTime: Float,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onStepPrev: () -> Unit,
    onStepNext: () -> Unit,
    scenes: List<SceneItem>,
    currentSceneIndex: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = StudioSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Multi-track visual blocks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioSurfaceVariant),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                scenes.forEachIndexed { index, scene ->
                    val weight = scene.durationSeconds / totalDuration.coerceAtLeast(1)
                    val isCurrent = index == currentSceneIndex
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(weight.coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isCurrent) StudioPrimary else StudioSurface
                            )
                            .border(
                                width = if (isCurrent) 1.5.dp else 0.5.dp,
                                color = if (isCurrent) StudioPrimaryLight else StudioBorder,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S${index + 1}",
                            color = if (isCurrent) Color.White else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Slider Scrubber & Timecode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val currentSec = currentProgressTime.toInt()
                val min = currentSec / 60
                val sec = currentSec % 60
                val totMin = totalDuration / 60
                val totSec = totalDuration % 60

                Text(
                    text = String.format("%02d:%02d", min, sec),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = currentProgressTime,
                    onValueChange = { onSeek(it) },
                    valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = StudioPrimaryLight,
                        activeTrackColor = StudioPrimary,
                        inactiveTrackColor = StudioSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = String.format("%02d:%02d", totMin, totSec),
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Playback Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onStepPrev,
                    modifier = Modifier
                        .size(40.dp)
                        .background(StudioSurfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Önceki Sahne",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Button(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("play_pause_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "Durdur" else "Oynat (TTS)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onStepNext,
                    modifier = Modifier
                        .size(40.dp)
                        .background(StudioSurfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Sonraki Sahne",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CopyActionButton(
    textToCopy: String,
    label: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, textToCopy)
            clipboard.setPrimaryClip(clip)
            copied = true
            Toast.makeText(context, "$label kopyalandı! ✓", Toast.LENGTH_SHORT).show()
            scope.launch {
                delay(2000)
                copied = false
            }
        },
        modifier = modifier.height(38.dp),
        colors = if (isPrimary) {
            ButtonDefaults.buttonColors(containerColor = StudioPrimary)
        } else {
            ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant)
        },
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(
            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = null,
            tint = if (copied) SuccessGreen else Color.White,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (copied) "Kopyalandı!" else label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (copied) SuccessGreen else Color.White
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HashtagCloudView(
    hashtags: List<String>,
    onCopyAll: () -> Unit,
    title: String = "Etiketler (Hashtags)",
    tagColor: Color = StudioPrimaryLight
) {
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
                        imageVector = Icons.Outlined.Tag,
                        contentDescription = null,
                        tint = tagColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                CopyActionButton(
                    textToCopy = hashtags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" },
                    label = "Tümünü Kopyala",
                    isPrimary = true
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                hashtags.forEach { tag ->
                    val cleanTag = if (tag.startsWith("#")) tag else "#$tag"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, StudioBorder)
                    ) {
                        Text(
                            text = cleanTag,
                            color = tagColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportSimulationModal(
    project: VideoProject,
    onDismiss: () -> Unit,
    onSharePackage: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var currentStep by remember { mutableStateOf("Video Kareleri Derleniyor (60 FPS 4K)...") }
    var isDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentStep = "Sahne Geçişleri ve Efektler İşleniyor..."
        progress = 0.25f
        delay(600)
        currentStep = "Dinamik Karaoke Altyazılar Katmana Basılıyor..."
        progress = 0.55f
        delay(700)
        currentStep = "Seslendirme ve Arka Plan Müziği Miksleniyor..."
        progress = 0.85f
        delay(600)
        currentStep = "Instagram Reels & YouTube Shorts Paketi Hazırlandı! 🎉"
        progress = 1.0f
        isDone = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!isDone) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(56.dp),
                        color = StudioPrimary,
                        trackColor = StudioSurfaceVariant
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = SuccessGreen.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, SuccessGreen),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }
                }

                Text(
                    text = if (isDone) "Otomasyon Paketi Hazır!" else "Otomasyon Dışa Aktarılıyor",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = currentStep,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                if (isDone) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            onSharePackage()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Metin & SEO Paketini Paylaş",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Stüdyoya Dön")
                    }
                }
            }
        }
    }
}
