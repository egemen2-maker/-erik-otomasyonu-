package com.example.model

enum class VideoNiche(val label: String, val iconName: String, val defaultPrompt: String) {
    TECH_AI("Yapay Zeka & Teknoloji", "Memory", "Günde 1 saat çalışarak yapay zeka araçlarıyla nasıl pasif gelir elde edilir? 3 somut adım."),
    MOTIVATION("Motivasyon & Disiplin", "FitnessCenter", "Her sabah saat 5'te kalkan insanların asla söylemediği 3 psikolojik sır."),
    CRYPTO_FINANCE("Finans & Para Yönetimi", "AttachMoney", "Zenginlerin paranızı eritmemek için uyguladığı 50/30/20 bütçe kuralı."),
    PSYCHOLOGY_FACTS("Psikoloji & İlginç Bilgiler", "Psychology", "İnsanların %95'inin farkında olmadığı 3 şok edici beyin hilesi."),
    PRODUCTIVITY("Üretkenlik & Odaklanma", "Bolt", "Dopamin detoksu ile 48 saatte odaklanmayı 10 katına çıkarmanın formülü."),
    SCIENCE_SPACE("Evren & Bilim", "RocketLaunch", "Eğer bir kara deliğe düşseydiniz vücudunuza ne olurdu? Spagetti etkisi."),
    FITNESS_HEALTH("Sağlık & Fitness", "Favorite", "Günde 10.000 adım atmanın vücutta değiştirdiği 4 mucizevi biyolojik süreç."),
    CUSTOM("Özel Konu / Prompt", "AutoAwesome", "")
}

enum class PlatformTarget(val title: String, val badge: String, val defaultAspect: VideoAspectRatio) {
    ALL_IN_ONE("Instagram Reels & YouTube Shorts", "9:16 Viral", VideoAspectRatio.PORTRAIT_9_16),
    INSTAGRAM_REELS("Instagram Reels", "Reels", VideoAspectRatio.PORTRAIT_9_16),
    YOUTUBE_SHORTS("YouTube Shorts", "Shorts", VideoAspectRatio.PORTRAIT_9_16),
    YOUTUBE_LONG("YouTube Video (Yatay)", "16:9 4K", VideoAspectRatio.LANDSCAPE_16_9)
}

enum class VideoAspectRatio(val label: String, val widthRatio: Float, val heightRatio: Float) {
    PORTRAIT_9_16("9:16 Dikey", 9f, 16f),
    LANDSCAPE_16_9("16:9 Yatay", 16f, 9f),
    SQUARE_1_1("1:1 Kare", 1f, 1f)
}

enum class VideoTone(val label: String, val desc: String) {
    ENERGETIC("Yüksek Enerjili & Dinamik", "Hızlı geçişler, vurucu giriş ve dikkat çekici ses tonu"),
    CINEMATIC("Sinematik & Derin", "Epik anlatım, ağır geçişler, derin odak"),
    MYSTERIOUS("Gizemli & Merak Uyandırıcı", "Soru işaretleriyle başlayan, son saniyeye kadar kitleyi tutan"),
    INFORMATIVE("Profesyonel & Öğretici", "Net bilgiler, madde madde sunum ve güven verici ton"),
    STORYTELLER("Hikaye Anlatıcısı", "Duygusal bağ kuran akıcı hikaye kurgusu")
}

enum class TransitionEffect(val label: String) {
    ZOOM_IN("Zoom In & Pulse"),
    WHIP_PAN("Whip Pan Hızlı Geçiş"),
    GLITCH("Glitch & Cyberpunk"),
    FADE_BLACK("Sinematik Kararma"),
    LIGHT_LEAK("Işık Süzmesi (Light Leak)"),
    SLIDE_UP("Slide Up Hızlı Kayma")
}

enum class CaptionStyle(val label: String, val previewText: String) {
    KARAOKE_POP("Karaoke Pop", "Canlı Vurgulu Kelimeler"),
    MR_BEAST_BOLD("Viral Bold", "SARI & SİYAH VURGULU"),
    GLOW_NEON("Neon Glow", "Siber Parlama Efekti"),
    MINIMAL_CLEAN("Minimalist", "Zarif ve Sade Tipografi"),
    BOXED_BADGE("Kutu Arka Plan", "Okunabilir Koyu Zemin")
}

enum class SubtitlePosition(val label: String) {
    CENTER("Ekran Ortası (Reels Stili)"),
    LOWER_THIRD("Alt Bölge (Geleneksel)"),
    UPPER_THIRD("Üst Bölge (Göz Hizası)")
}

enum class ProjectStatus(val label: String) {
    DRAFT("Taslak"),
    GENERATING("AI Üretiyor..."),
    READY_TO_PUBLISH("Yüklemeye Hazır"),
    EXPORTED("Dışa Aktarıldı")
}

data class SceneItem(
    val id: Int,
    val orderIndex: Int,
    val durationSeconds: Float = 4.0f,
    val narrationText: String,
    val visualDescription: String,
    val onScreenSubtitle: String,
    val transitionType: TransitionEffect = TransitionEffect.ZOOM_IN,
    val soundEffectCue: String = "Whoosh",
    val bgThemeIndex: Int = 0,
    val textHighlightWords: List<String> = emptyList()
)

data class VideoScript(
    val title: String,
    val hookLine: String,
    val scenes: List<SceneItem>,
    val ctaLine: String,
    val totalDurationSeconds: Int = 30
)

data class InstagramPublishData(
    val caption: String,
    val viralHooks: List<String> = emptyList(),
    val topHashtags: List<String> = emptyList(),
    val nicheHashtags: List<String> = emptyList(),
    val audioRecommendation: String = "Trending Ambient Beat (120 BPM)",
    val firstCommentPin: String = "",
    val bestPostingTime: String = "Bugün 18:00 - 21:30",
    val coverTitle: String = ""
) {
    val fullHashtagString: String
        get() = (topHashtags + nicheHashtags).joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
}

data class YouTubePublishData(
    val titleOptions: List<String> = emptyList(),
    val selectedTitle: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val thumbnailPrompt: String = "",
    val pinnedComment: String = "",
    val categoryName: String = "Bilim ve Teknoloji",
    val estimatedCtr: String = "%11.4 CTR Potansiyeli"
) {
    val tagsCommaSeparated: String
        get() = tags.joinToString(", ")
}

data class SocialPublishPack(
    val instagramPack: InstagramPublishData,
    val youtubePack: YouTubePublishData
)

data class VideoStyleSettings(
    val captionStyle: CaptionStyle = CaptionStyle.KARAOKE_POP,
    val subtitlePosition: SubtitlePosition = SubtitlePosition.CENTER,
    val fontSizeSp: Int = 22,
    val bgMusicName: String = "Cyberpunk Pulse (Telif Yok)",
    val voiceType: String = "Derin & Karizmatik",
    val voiceSpeed: Float = 1.0f,
    val showHookBanner: Boolean = true,
    val showWaveform: Boolean = true
)

data class VideoProject(
    val id: Long = 0,
    val topic: String,
    val niche: VideoNiche,
    val platformTarget: PlatformTarget,
    val aspectRatio: VideoAspectRatio,
    val tone: VideoTone,
    val durationSeconds: Int,
    val status: ProjectStatus = ProjectStatus.READY_TO_PUBLISH,
    val createdAt: Long = System.currentTimeMillis(),
    val hookScore: Int = 94,
    val estimatedViralMultiplier: String = "4.2x Viral",
    val script: VideoScript,
    val publishPack: SocialPublishPack,
    val styleSettings: VideoStyleSettings = VideoStyleSettings()
)

enum class CommentSentiment(val label: String, val badgeColorHex: Long) {
    QUESTION("Sık Sorulan Soru", 0xFF6750A4),
    PURCHASE_LINK("Link & Prompt Talebi", 0xFFB54708),
    POSITIVE("Övgü & Teşekkür", 0xFF1B8755),
    FEEDBACK("Öneri & Görüş", 0xFF175CD3)
}

enum class ReplyTone(val label: String, val emoji: String) {
    FRIENDLY("Samimi & Enerjik", "🔥"),
    PROFESSIONAL("Profesyonel & Net", "💼"),
    HUMOROUS("Mizahi & Espirili", "😄"),
    LINK_CALL("Linke Yönlendiren", "🔗")
}

data class CommentItem(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val platform: String, // "Instagram", "YouTube", "TikTok"
    val commentText: String,
    val videoTitle: String,
    val frequencyCount: Int, // e.g. 148
    val frequencyPercentage: Int, // e.g. 42
    val sentiment: CommentSentiment = CommentSentiment.QUESTION,
    val category: String = "Prompt / Kod İsteme",
    val aiSuggestedReply: String,
    val userCustomReply: String = "",
    val isReplied: Boolean = false,
    val repliedWithAi: Boolean = false,
    val activeReplyText: String = "",
    val timestamp: String = "10 dk önce",
    val likesCount: Int = 42
)

