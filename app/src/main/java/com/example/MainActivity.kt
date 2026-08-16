package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ProjectLibraryScreen
import com.example.ui.screens.PublishPackageScreen
import com.example.ui.screens.StudioCreateScreen
import com.example.ui.screens.TopCommentsScreen
import com.example.ui.screens.VideoEditorScreen
import com.example.ui.theme.AutoReelTheme
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioPrimaryDark
import com.example.ui.theme.StudioPrimaryLight
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.VideoAutomationViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VideoAutomationViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AutoReelTheme {
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                var showDownloadGuide by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(StudioBackground),
                    containerColor = StudioBackground,
                    topBar = {
                        StudioTopBar(
                            onOpenDownloadGuide = { showDownloadGuide = true }
                        )
                    },
                    bottomBar = {
                        StudioBottomNavigation(
                            selectedTab = selectedTabIndex,
                            onTabSelected = { selectedTabIndex = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTabIndex) {
                            0 -> StudioCreateScreen(
                                viewModel = viewModel,
                                onNavigateToEditor = { selectedTabIndex = 1 }
                            )
                            1 -> VideoEditorScreen(
                                viewModel = viewModel,
                                onNavigateToPublish = { selectedTabIndex = 2 }
                            )
                            2 -> PublishPackageScreen(
                                viewModel = viewModel
                            )
                            3 -> TopCommentsScreen(
                                viewModel = viewModel
                            )
                            4 -> ProjectLibraryScreen(
                                viewModel = viewModel,
                                onNavigateToCreate = { selectedTabIndex = 0 },
                                onNavigateToEditor = { selectedTabIndex = 1 }
                            )
                        }
                    }
                }

                if (showDownloadGuide) {
                    AppDownloadGuideDialog(onDismiss = { showDownloadGuide = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioTopBar(
    onOpenDownloadGuide: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Video Automator",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Sistem Durumu: Aktif • AI Video & Yorum Motoru",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Download APK Guide Button
                    Surface(
                        shape = CircleShape,
                        color = StudioSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(
                            onClick = onOpenDownloadGuide,
                            modifier = Modifier.testTag("btn_top_download_guide")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = "Uygulamayı İndir",
                                tint = StudioPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // System Active Status Icon
                    Surface(
                        shape = CircleShape,
                        color = StudioPrimaryLight,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Sistem Durumu",
                                tint = StudioPrimaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = StudioBackground,
            titleContentColor = TextPrimary
        )
    )
}

@Composable
fun StudioBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = StudioSurfaceElevated,
        contentColor = TextPrimary,
        tonalElevation = 2.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = StudioBorder.copy(alpha = 0.35f)
        )
    ) {
        val navItems = listOf(
            NavigationTabItem("1. Üret", Icons.Filled.RocketLaunch, Icons.Outlined.RocketLaunch, "tab_create"),
            NavigationTabItem("2. Kurgu", Icons.Filled.Movie, Icons.Outlined.Movie, "tab_editor"),
            NavigationTabItem("3. SEO", Icons.Filled.Campaign, Icons.Outlined.Campaign, "tab_publish"),
            NavigationTabItem("4. Yorumlar", Icons.Filled.Forum, Icons.Outlined.Forum, "tab_comments"),
            NavigationTabItem("Kütüphane", Icons.Filled.Folder, Icons.Outlined.Folder, "tab_library")
        )

        navItems.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) StudioPrimaryDark else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) StudioPrimaryDark else TextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = StudioPrimaryLight,
                    selectedIconColor = StudioPrimaryDark,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = StudioPrimaryDark,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

@Composable
fun AppDownloadGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Android,
                    contentDescription = null,
                    tint = StudioPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Uygulamayı İndirme Rehberi",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Uygulamayı telefonunuza veya bilgisayarınıza 3 kolay yöntemle indirebilirsiniz:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Step 1: Direct APK Download
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioPrimaryLight.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "1. Android Telefonunuza APK Olarak İndirme (Önerilen)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioPrimaryDark
                        )
                        Text(
                            text = "Google AI Studio ekranının sağ üst köşesindeki ayarlar / menü (⋮ veya Settings) panelinden 'Download APK / Build APK' seçeneğine tıklayarak .apk dosyasını telefonunuza yükleyebilirsiniz.",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Step 2: ZIP Export
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "2. Tüm Projeyi ZIP Olarak Bilgisayara İndirme",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "AI Studio üst panelindeki 'Export Project' veya 'Download ZIP' butonunu kullanarak kaynak kodları indirebilir ve Android Studio ile açabilirsiniz.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Step 3: GitHub Push
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "3. Doğrudan GitHub'a Aktarma",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ayarlar menüsünden 'Push to GitHub' seçeneğiyle projenizi kendi reponuza senkronize edebilirsiniz.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
            ) {
                Text("Anladım")
            }
        }
    )
}


data class NavigationTabItem(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
