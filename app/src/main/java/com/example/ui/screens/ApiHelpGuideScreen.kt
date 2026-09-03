package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ApiHelpGuideScreen(
    viewModel: VideoAutomationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val socialConfig by viewModel.socialConfig.collectAsState()
    val statusMessage by viewModel.apiStatusMessage.collectAsState()
    val isLoading by viewModel.isLiveApiLoading.collectAsState()

    var geminiInput by remember { mutableStateOf(socialConfig.geminiApiKey) }
    var ytInput by remember { mutableStateOf(socialConfig.youtubeApiKey) }
    var igInput by remember { mutableStateOf(socialConfig.instagramAccessToken) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label kopyalandı!", Toast.LENGTH_SHORT).show()
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
            // Screen Header
            Text(
                text = "API & Kurulum Rehberi",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Google Gemini AI, YouTube ve Instagram API anahtarlarını ücretsiz nasıl alacağınızın adım adım resimli anlatımı",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Live API Configuration & Quick Tester Card (Frosted Glass)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x28FFFFFF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40FFFFFF))
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Hızlı API Anahtarı Kaydı & Canlı Test",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Güvenli Depolama",
                                color = StudioPrimaryLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Status Message Display
                    if (!statusMessage.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (statusMessage!!.startsWith("✅")) Color(0x2500E676) else Color(0x25FF5252),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (statusMessage!!.startsWith("✅")) Color(0x6000E676) else Color(0x60FF5252)
                            )
                        ) {
                            Text(
                                text = statusMessage!!,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // 1. Gemini Key Input & Live Test Button
                    Text(
                        text = "1. Google Gemini API Anahtarı (AI Studio)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPrimaryLight
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = geminiInput,
                            onValueChange = { geminiInput = it },
                            placeholder = { Text("AIzaSy...", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_gemini_key"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioPrimary,
                                unfocusedBorderColor = StudioBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Button(
                            onClick = { viewModel.testGeminiApiKey(geminiInput) },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(48.dp).testTag("btn_test_gemini")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Test Et", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 2. YouTube Key Input
                    Text(
                        text = "2. YouTube Data API v3 Anahtarı",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = YouTubeRed
                    )

                    OutlinedTextField(
                        value = ytInput,
                        onValueChange = { ytInput = it },
                        placeholder = { Text("YouTube API Key...", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_yt_key"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YouTubeRed,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // 3. Instagram Token Input
                    Text(
                        text = "3. Instagram Meta Graph API Erişim Belirteci",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InstagramPink
                    )

                    OutlinedTextField(
                        value = igInput,
                        onValueChange = { igInput = it },
                        placeholder = { Text("IGQVJ... veya EAABw...", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_ig_token"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InstagramPink,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Save All Button
                    Button(
                        onClick = {
                            viewModel.saveAllApiCredentials(geminiInput, ytInput, igInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_save_all_keys")
                    ) {
                        Text("💾 Tüm API Anahtarlarını Cihaza Kaydet", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Detailed Guide: Google Gemini API (Free, AI Studio)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = StudioPrimary.copy(alpha = 0.25f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🤖", fontSize = 16.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Google Gemini API (ÜCRETSİZ)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Google AI Studio • 15 İstek/Dakika Ücretsiz",
                                fontSize = 11.sp,
                                color = StudioSecondary
                            )
                        }
                    }

                    // Explanation Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 Neden Almalısınız?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = StudioPrimaryLight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gemini API, uygulamanın sınırsız viral YouTube Shorts ve Instagram Reels senaryoları yazmasını, kanca üretmesini ve gelen yorumlara akıllı yanıtlar vermesini sağlar. Tamamen ÜCRETSİZDİR ve kredi kartı gerektirmez.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Text(
                        text = "Adım Adım Nasıl Alınır? (1 Dakika)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )

                    // Steps
                    val steps = listOf(
                        "1. Tarayıcınızda 'aistudio.google.com' adresine gidin.",
                        "2. Gmail (Google) hesabınız ile oturum açın.",
                        "3. Sol menüde veya üstte bulunan mavi 'Get API key' butonuna tıklayın.",
                        "4. 'Create API key' (API Anahtarı Oluştur) butonuna basın.",
                        "5. Üretilen 'AIzaSy...' ile başlayan anahtarı kopyalayın.",
                        "6. Yukarıdaki kutuya yapıştırıp 'Test Et' butonuna basın!"
                    )

                    steps.forEach { stepText ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudioSecondary, modifier = Modifier.size(15.dp).padding(top = 2.dp))
                            Text(text = stepText, fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    // Copy Link Helper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { copyToClipboard("https://aistudio.google.com/app/apikey", "Google AI Studio Linki") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Studio Linkini Kopyala", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Detailed Guide: YouTube Data API v3
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = YouTubeRed.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("▶️", fontSize = 16.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "YouTube Data API v3",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Google Cloud Console • Canlı Yorum ve Analiz",
                                fontSize = 11.sp,
                                color = YouTubeRed
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 Neden Almalısınız?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = YouTubeRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Shorts videonuzun altına gelen gerçek izleyici yorumlarını uygulamaya canlı çekmek ve tek tıkla yapay zeka ile yanıtlamak için kullanılır.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Text(
                        text = "Adım Adım Nasıl Alınır?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )

                    val ytSteps = listOf(
                        "1. 'console.cloud.google.com' adresine gidin.",
                        "2. Üstten 'Yeni Proje' (Create Project) oluşturun.",
                        "3. Sol menüden 'API ve Hizmetler' -> 'Kitaplık' sekmesini açın.",
                        "4. 'YouTube Data API v3' araması yapın ve 'Etkinleştir' deyin.",
                        "5. 'Kimlik Bilgileri' -> '+ Kimlik Bilgisi Oluştur' -> 'API Anahtarı'nı seçin.",
                        "6. Oluşan anahtarı bu sayfadaki YouTube kutusuna yapıştırın."
                    )

                    ytSteps.forEach { stepText ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(15.dp).padding(top = 2.dp))
                            Text(text = stepText, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Detailed Guide: Instagram Meta Graph API
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = InstagramPink.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📷", fontSize = 16.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Instagram Meta Graph API",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Meta for Developers • Canlı Reels Yorumları",
                                fontSize = 11.sp,
                                color = InstagramPink
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 Neden Almalısınız?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = InstagramPink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Reels izleyicilerinizin sorularını ve yorumlarını canlı çekmek, algoritmanın sevdiği ilk 30 dakika içinde hızlıca AI yanıtı vermek için kullanılır.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Text(
                        text = "Adım Adım Nasıl Alınır?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )

                    val igSteps = listOf(
                        "1. Instagram hesabınızın Profesyonel (İçerik Üretici) hesabı olduğundan emin olun.",
                        "2. 'developers.facebook.com' adresine gidin ve bir Uygulama oluşturun.",
                        "3. Araçlar -> 'Graph API Explorer' sayfasına gidin.",
                        "4. İzinlerden 'instagram_basic' ve 'instagram_manage_comments' seçin.",
                        "5. 'Generate Access Token' butonuna tıklayarak belirteci kopyalayın."
                    )

                    igSteps.forEach { stepText ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = InstagramPink, modifier = Modifier.size(15.dp).padding(top = 2.dp))
                            Text(text = stepText, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // FAQ Section (Sık Sorulan Sorular)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "❓ Sık Sorulan Sorular (SSS)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // FAQ 1
                    Column {
                        Text(
                            text = "• API girmeden uygulama çalışır mı?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = StudioPrimaryLight
                        )
                        Text(
                            text = "EVET! Uygulama içinde yerleşik akıllı şablon motoru ve yerel AI motoru bulunmaktadır. API girmeden de videolar üretebilir ve düzenleyebilirsiniz. Kendi Gemini API anahtarınızı girdiğinizde ise Google'ın en yeni modelleri canlı olarak devreye girer.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // FAQ 2
                    Column {
                        Text(
                            text = "• Anahtarlarım güvende mi?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = StudioSecondaryLight
                        )
                        Text(
                            text = "EVET. Girdiğiniz tüm anahtarlar yalnızca kendi cihazınızın güvenli Android SharedPreferences deposunda şifreli tutulur. Asla harici bir üçüncü taraf sunucuya aktarılmaz.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // FAQ 3
                    Column {
                        Text(
                            text = "• Şablon tekrarından nasıl kurtulurum?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = StudioTertiary
                        )
                        Text(
                            text = "Kurgu sekmesinde bulunan '📐 Video Görsel Düzeni (Layout)' bölümünden 'Bölünmüş Ekran (Split)', 'Podcast / İkili Sohbet' veya 'Adım Geri Sayım' modlarını seçtiğinizde videonun ekran düzeni, animasyonları ve kurgu stili baştan sona değişir.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
