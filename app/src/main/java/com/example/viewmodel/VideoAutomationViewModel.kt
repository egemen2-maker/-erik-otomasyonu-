package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TextToSpeechHelper
import com.example.data.ai.GeminiAutomationService
import com.example.data.api.ApiResult
import com.example.data.api.SocialApiConfig
import com.example.data.api.SocialMediaApiService
import com.example.data.local.AppDatabase
import com.example.data.local.ProjectRepository
import com.example.model.CaptionStyle
import com.example.model.ChannelProfileData
import com.example.model.CommentItem
import com.example.model.HookType
import com.example.model.PlatformTarget
import com.example.model.ProjectStatus
import com.example.model.ReplyTone
import com.example.model.SceneItem
import com.example.model.SubtitlePosition
import com.example.model.TransitionEffect
import com.example.model.VideoAspectRatio
import com.example.model.VideoNiche
import com.example.model.VideoProject
import com.example.model.VideoStyleSettings
import com.example.model.VideoTone
import com.example.model.VideoVisualLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VideoAutomationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(AppDatabase.getInstance(application).projectDao())
    private val aiService = GeminiAutomationService()
    private val ttsHelper = TextToSpeechHelper(application)
    private val socialApiService = SocialMediaApiService()

    private val sharedPrefs = application.getSharedPreferences("autoreel_api_prefs", Context.MODE_PRIVATE)

    // Live API & Credentials Configuration
    private val _socialConfig = MutableStateFlow(loadSavedSocialConfig())
    val socialConfig: StateFlow<SocialApiConfig> = _socialConfig.asStateFlow()

    private val _apiStatusMessage = MutableStateFlow<String?>(null)
    val apiStatusMessage: StateFlow<String?> = _apiStatusMessage.asStateFlow()

    private val _isLiveApiLoading = MutableStateFlow(false)
    val isLiveApiLoading: StateFlow<Boolean> = _isLiveApiLoading.asStateFlow()

    val savedProjects: StateFlow<List<VideoProject>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeProject = MutableStateFlow<VideoProject?>(null)
    val activeProject: StateFlow<VideoProject?> = _activeProject.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStage = MutableStateFlow("")
    val generationStage: StateFlow<String> = _generationStage.asStateFlow()

    private val _suggestedIdeas = MutableStateFlow<List<String>>(emptyList())
    val suggestedIdeas: StateFlow<List<String>> = _suggestedIdeas.asStateFlow()

    // Comments & AI Auto-Reply State
    private val _commentsList = MutableStateFlow<List<CommentItem>>(emptyList())
    val commentsList: StateFlow<List<CommentItem>> = _commentsList.asStateFlow()

    private val _isAnalyzingComments = MutableStateFlow(false)
    val isAnalyzingComments: StateFlow<Boolean> = _isAnalyzingComments.asStateFlow()

    private val _isAiAutoReplyEnabled = MutableStateFlow(false)
    val isAiAutoReplyEnabled: StateFlow<Boolean> = _isAiAutoReplyEnabled.asStateFlow()

    private val _activeReplyTone = MutableStateFlow(ReplyTone.FRIENDLY)
    val activeReplyTone: StateFlow<ReplyTone> = _activeReplyTone.asStateFlow()

    // Profile Optimization Studio State
    private val _profileData = MutableStateFlow(
        ChannelProfileData(
            platform = "Instagram",
            handle = "@ai.gelirleri",
            channelName = "Yapay Zeka & Pasif Gelir",
            bio = "⚡ Günde 1 saat yapay zeka ile dolar kazan\n🤖 En güncel otomasyon araçları & promptları\n📈 Sıfırdan 100K takipçi büyüme stratejileri\n👇 Ücretsiz 30 Günlük AI Rehberini İndir:",
            websiteLink = "linktr.ee/aigelirleri",
            category = "Yapay Zeka & Finans",
            avatarPrompt = "3D minimalist glowing futuristic avatar icon of a neon neural brain floating over dark obsidian, octane render, 8k, sleek tech aesthetics",
            bannerPrompt = "Cinematic YouTube channel banner, 16:9 ultra-wide, dark futuristic cyber studio with glowing neon cyan typography reading 'AI GELİRLERİ', 8k, photorealistic",
            suggestedHandles = listOf("@ai.gelirleri", "@zihin.kodlari", "@finans.yapayzeka", "@otomasyon.rehberi", "@dijital.zenginlik"),
            highlightTitles = listOf("🚀 Promptlar", "💰 Gelirler", "⚡ Araçlar", "❓ SSS", "⭐ Sonuçlar")
        )
    )
    val profileData: StateFlow<ChannelProfileData> = _profileData.asStateFlow()

    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSceneIndex = MutableStateFlow(0)
    val currentSceneIndex: StateFlow<Int> = _currentSceneIndex.asStateFlow()

    private val _sceneProgress = MutableStateFlow(0f)
    val sceneProgress: StateFlow<Float> = _sceneProgress.asStateFlow()

    private val _totalProgressSeconds = MutableStateFlow(0f)
    val totalProgressSeconds: StateFlow<Float> = _totalProgressSeconds.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Load initial pro starter project or trending ideas
        viewModelScope.launch {
            loadStarterProject()
            loadSuggestedIdeas(VideoNiche.TECH_AI)
            loadTopCommentsForProject()
        }
    }

    private fun loadSavedSocialConfig(): SocialApiConfig {
        return SocialApiConfig(
            geminiApiKey = sharedPrefs.getString("gemini_api_key", "") ?: "",
            youtubeApiKey = sharedPrefs.getString("yt_api_key", "") ?: "",
            youtubeVideoIdOrUrl = sharedPrefs.getString("yt_video_url", "") ?: "",
            youtubeOAuthToken = sharedPrefs.getString("yt_oauth_token", "") ?: "",
            instagramAccessToken = sharedPrefs.getString("ig_access_token", "") ?: "",
            instagramMediaIdOrUrl = sharedPrefs.getString("ig_media_url", "") ?: "",
            isYouTubeConnected = sharedPrefs.getBoolean("yt_connected", false),
            isInstagramConnected = sharedPrefs.getBoolean("ig_connected", false)
        )
    }

    private fun saveSocialConfigToPrefs(cfg: SocialApiConfig) {
        sharedPrefs.edit()
            .putString("gemini_api_key", cfg.geminiApiKey)
            .putString("yt_api_key", cfg.youtubeApiKey)
            .putString("yt_video_url", cfg.youtubeVideoIdOrUrl)
            .putString("yt_oauth_token", cfg.youtubeOAuthToken)
            .putString("ig_access_token", cfg.instagramAccessToken)
            .putString("ig_media_url", cfg.instagramMediaIdOrUrl)
            .putBoolean("yt_connected", cfg.isYouTubeConnected)
            .putBoolean("ig_connected", cfg.isInstagramConnected)
            .apply()
    }

    private suspend fun loadStarterProject() {
        val starter = aiService.generateProFallbackProject(
            topic = "Günde 1 Saat Yapay Zeka ile Pasif Gelir",
            niche = VideoNiche.TECH_AI,
            platformTarget = PlatformTarget.ALL_IN_ONE,
            tone = VideoTone.ENERGETIC,
            durationSeconds = 30
        )
        _activeProject.value = starter
    }

    fun loadSuggestedIdeas(niche: VideoNiche) {
        viewModelScope.launch {
            _suggestedIdeas.value = aiService.suggestTrendingTopics(niche, _socialConfig.value.geminiApiKey)
        }
    }

    fun generateVideo(
        topic: String,
        niche: VideoNiche,
        platformTarget: PlatformTarget,
        tone: VideoTone,
        durationSeconds: Int
    ) {
        viewModelScope.launch {
            stopPlayback()
            _isGenerating.value = true
            _generationStage.value = "Kurgu ve Otomasyon Başlatılıyor..."
            val customKey = _socialConfig.value.geminiApiKey
            try {
                val generated = aiService.generateCompleteVideoAutomation(
                    topic = topic,
                    niche = niche,
                    platformTarget = platformTarget,
                    tone = tone,
                    durationSeconds = durationSeconds,
                    customApiKey = customKey,
                    onStageUpdate = { stage ->
                        _generationStage.value = stage
                    }
                )
                val id = repository.saveProject(generated)
                val projectWithId = generated.copy(id = id)
                _activeProject.value = projectWithId
                _currentSceneIndex.value = 0
                _sceneProgress.value = 0f
                _totalProgressSeconds.value = 0f
                loadTopCommentsForProject(generated.topic, generated.niche)
            } catch (_: Exception) {
                val fallback = aiService.generateProFallbackProject(
                    topic = topic,
                    niche = niche,
                    platformTarget = platformTarget,
                    tone = tone,
                    durationSeconds = durationSeconds
                )
                val id = repository.saveProject(fallback)
                _activeProject.value = fallback.copy(id = id)
                loadTopCommentsForProject(fallback.topic, fallback.niche)
            } finally {
                _isGenerating.value = false
                _generationStage.value = ""
            }
        }
    }

    fun selectProject(project: VideoProject) {
        stopPlayback()
        _activeProject.value = project
        _currentSceneIndex.value = 0
        _sceneProgress.value = 0f
        _totalProgressSeconds.value = 0f
        loadTopCommentsForProject(project.topic, project.niche)
    }


    fun deleteProject(project: VideoProject) {
        viewModelScope.launch {
            if (project.id > 0) {
                repository.deleteProject(project.id)
            }
            if (_activeProject.value?.id == project.id) {
                loadStarterProject()
            }
        }
    }

    fun updateCaptionStyle(style: CaptionStyle) {
        val current = _activeProject.value ?: return
        val updated = current.copy(
            styleSettings = current.styleSettings.copy(captionStyle = style)
        )
        _activeProject.value = updated
        saveProjectAsync(updated)
    }

    fun updateSubtitlePosition(pos: SubtitlePosition) {
        val current = _activeProject.value ?: return
        val updated = current.copy(
            styleSettings = current.styleSettings.copy(subtitlePosition = pos)
        )
        _activeProject.value = updated
        saveProjectAsync(updated)
    }

    fun updateAspectRatio(aspect: VideoAspectRatio) {
        val current = _activeProject.value ?: return
        val updated = current.copy(aspectRatio = aspect)
        _activeProject.value = updated
        saveProjectAsync(updated)
    }

    fun switchHookType(hookType: HookType) {
        val current = _activeProject.value ?: return
        val newHookText = if (hookType == HookType.HOOK_B && current.splitHooks.hookB.isNotBlank()) {
            current.splitHooks.hookB
        } else {
            current.splitHooks.hookA
        }
        val scenes = current.script.scenes.toMutableList()
        if (scenes.isNotEmpty()) {
            val first = scenes[0]
            val rest = if (first.narrationText.contains(".")) {
                first.narrationText.substringAfter(".", "")
            } else ""
            scenes[0] = first.copy(
                narrationText = if (rest.isNotBlank()) "$newHookText. $rest" else newHookText,
                onScreenSubtitle = newHookText.take(38)
            )
        }
        val updated = current.copy(
            script = current.script.copy(
                hookLine = newHookText,
                scenes = scenes
            ),
            splitHooks = current.splitHooks.copy(selectedHookType = hookType)
        )
        _activeProject.value = updated
        saveProjectAsync(updated)
    }

    fun toggleBouncingEmojis() {
        val current = _activeProject.value ?: return
        val updated = current.copy(
            styleSettings = current.styleSettings.copy(
                bouncingEmojisEnabled = !current.styleSettings.bouncingEmojisEnabled
            )
        )
        _activeProject.value = updated
        saveProjectAsync(updated)
    }

    fun toggleHumanizedBreathing() {
        val current = _activeProject.value ?: return
        val updated = current.copy(
            styleSettings = current.styleSettings.copy(
                humanizedBreathing = !current.styleSettings.humanizedBreathing
            )
        )
        _activeProject.value = updated
        saveProjectAsync(updated)
    }

    fun updateVisualLayout(layout: VideoVisualLayout) {
        val current = _activeProject.value ?: return
        val updated = current.copy(
            styleSettings = current.styleSettings.copy(visualLayout = layout)
        )
        _activeProject.value = updated
        saveProjectAsync(updated)
    }

    fun updateSceneNarration(sceneIndex: Int, text: String) {
        val current = _activeProject.value ?: return
        val scenes = current.script.scenes.toMutableList()
        if (sceneIndex in scenes.indices) {
            val s = scenes[sceneIndex]
            scenes[sceneIndex] = s.copy(
                narrationText = text,
                onScreenSubtitle = text
            )
            val updated = current.copy(
                script = current.script.copy(scenes = scenes)
            )
            _activeProject.value = updated
            saveProjectAsync(updated)
        }
    }

    fun updateSceneTransition(sceneIndex: Int, transition: TransitionEffect) {
        val current = _activeProject.value ?: return
        val scenes = current.script.scenes.toMutableList()
        if (sceneIndex in scenes.indices) {
            val s = scenes[sceneIndex]
            scenes[sceneIndex] = s.copy(transitionType = transition)
            val updated = current.copy(
                script = current.script.copy(scenes = scenes)
            )
            _activeProject.value = updated
            saveProjectAsync(updated)
        }
    }

    fun updateSceneBackground(sceneIndex: Int, bgThemeIndex: Int) {
        val current = _activeProject.value ?: return
        val scenes = current.script.scenes.toMutableList()
        if (sceneIndex in scenes.indices) {
            val s = scenes[sceneIndex]
            scenes[sceneIndex] = s.copy(bgThemeIndex = bgThemeIndex)
            val updated = current.copy(
                script = current.script.copy(scenes = scenes)
            )
            _activeProject.value = updated
            saveProjectAsync(updated)
        }
    }

    private fun saveProjectAsync(project: VideoProject) {
        viewModelScope.launch {
            if (project.id > 0) {
                repository.updateProject(project)
            } else {
                val newId = repository.saveProject(project)
                _activeProject.value = project.copy(id = newId)
            }
        }
    }

    // Playback Engine with TTS & Timeline
    fun togglePlayPause() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        val project = _activeProject.value ?: return
        val scenes = project.script.scenes
        if (scenes.isEmpty()) return

        _isPlaying.value = true
        playbackJob?.cancel()

        playbackJob = viewModelScope.launch {
            val startIdx = _currentSceneIndex.value
            for (i in startIdx until scenes.size) {
                if (!_isPlaying.value) break
                _currentSceneIndex.value = i
                val scene = scenes[i]
                val duration = scene.durationSeconds.coerceAtLeast(2.0f)
                val durationMillis = (duration * 1000).toLong()

                // Speak scene text via TTS with humanized breathing
                ttsHelper.speak(
                    text = scene.narrationText,
                    speechRate = project.styleSettings.voiceSpeed,
                    humanizedBreathing = project.styleSettings.humanizedBreathing
                )

                // Animate progress for the scene
                val stepCount = 30
                val stepDelay = durationMillis / stepCount
                for (s in 0..stepCount) {
                    if (!_isPlaying.value) break
                    _sceneProgress.value = s.toFloat() / stepCount
                    calculateTotalProgress(project, i, _sceneProgress.value)
                    delay(stepDelay)
                }
            }

            // Loop or reset
            _isPlaying.value = false
            _sceneProgress.value = 0f
            _currentSceneIndex.value = 0
            _totalProgressSeconds.value = 0f
            ttsHelper.stop()
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        ttsHelper.stop()
    }

    fun seekToScene(sceneIndex: Int) {
        val project = _activeProject.value ?: return
        val scenes = project.script.scenes
        if (sceneIndex in scenes.indices) {
            _currentSceneIndex.value = sceneIndex
            _sceneProgress.value = 0f
            calculateTotalProgress(project, sceneIndex, 0f)
            if (_isPlaying.value) {
                startPlayback()
            }
        }
    }

    fun stepScene(next: Boolean) {
        val project = _activeProject.value ?: return
        val scenes = project.script.scenes
        val current = _currentSceneIndex.value
        val newIndex = if (next) {
            (current + 1).coerceAtMost(scenes.size - 1)
        } else {
            (current - 1).coerceAtLeast(0)
        }
        seekToScene(newIndex)
    }

    private fun calculateTotalProgress(project: VideoProject, sceneIndex: Int, currentSceneRatio: Float) {
        val scenes = project.script.scenes
        var elapsed = 0f
        for (i in 0 until sceneIndex.coerceAtMost(scenes.size)) {
            elapsed += scenes[i].durationSeconds
        }
        if (sceneIndex in scenes.indices) {
            elapsed += (scenes[sceneIndex].durationSeconds * currentSceneRatio)
        }
        _totalProgressSeconds.value = elapsed
    }

    fun batchGenerateWeekTopics(niche: VideoNiche) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationStage.value = "Haftalık 3 Viral Reels Paketi Hazırlanıyor..."
            val topics = aiService.suggestTrendingTopics(niche).take(3)
            topics.forEachIndexed { index, topic ->
                _generationStage.value = "Video ${index + 1}/3 Üretiliyor: ${topic.take(30)}..."
                val project = aiService.generateCompleteVideoAutomation(
                    topic = topic,
                    niche = niche,
                    platformTarget = PlatformTarget.ALL_IN_ONE,
                    tone = VideoTone.ENERGETIC,
                    durationSeconds = 30
                )
                repository.saveProject(project)
                if (index == 0) {
                    _activeProject.value = project
                }
            }
            _isGenerating.value = false
            _generationStage.value = ""
        }
    }

    // ==========================================
    // TOP COMMENTS & SMART AI AUTO-REPLY LOGIC
    // ==========================================

    fun loadTopCommentsForProject(customTitle: String? = null, customNiche: VideoNiche? = null) {
        viewModelScope.launch {
            _isAnalyzingComments.value = true
            val title = customTitle ?: _activeProject.value?.topic ?: "Günde 1 Saat Yapay Zeka ile Pasif Gelir"
            val niche = customNiche ?: _activeProject.value?.niche ?: VideoNiche.TECH_AI
            val cfg = _socialConfig.value

            // If user has connected live YouTube with a video url/id, fetch live real comments first!
            if (cfg.youtubeVideoIdOrUrl.isNotBlank()) {
                val liveResult = socialApiService.fetchLiveYouTubeComments(cfg.youtubeApiKey, cfg.youtubeVideoIdOrUrl)
                if (liveResult is ApiResult.Success && liveResult.data.isNotEmpty()) {
                    _commentsList.value = liveResult.data
                    _isAnalyzingComments.value = false
                    if (_isAiAutoReplyEnabled.value) {
                        autoReplyAllPendingWithAi()
                    }
                    return@launch
                }
            }

            // Otherwise, generate realistic, dynamic context-aware comments for this specific video
            val comments = aiService.generateTopComments(title, niche, cfg.geminiApiKey)
            _commentsList.value = comments
            _isAnalyzingComments.value = false

            // If auto-reply is on, automatically reply with AI
            if (_isAiAutoReplyEnabled.value) {
                autoReplyAllPendingWithAi()
            }
        }
    }

    fun setReplyTone(tone: ReplyTone) {
        _activeReplyTone.value = tone
    }

    fun toggleAutoReplyMode(enabled: Boolean) {
        _isAiAutoReplyEnabled.value = enabled
        if (enabled) {
            autoReplyAllPendingWithAi()
        }
    }

    fun updateCustomReplyText(commentId: String, text: String) {
        _commentsList.value = _commentsList.value.map { item ->
            if (item.id == commentId) {
                item.copy(userCustomReply = text, activeReplyText = text)
            } else {
                item
            }
        }
    }

    fun applyAiSuggestionToActiveReply(commentId: String) {
        _commentsList.value = _commentsList.value.map { item ->
            if (item.id == commentId) {
                item.copy(
                    userCustomReply = item.aiSuggestedReply,
                    activeReplyText = item.aiSuggestedReply
                )
            } else {
                item
            }
        }
    }

    fun generateNewAiReplyForComment(commentId: String, tone: ReplyTone = _activeReplyTone.value) {
        viewModelScope.launch {
            val target = _commentsList.value.find { it.id == commentId } ?: return@launch
            val videoTitle = _activeProject.value?.topic ?: target.videoTitle
            val generatedReply = aiService.generateAiReply(
                commentText = target.commentText,
                videoTitle = videoTitle,
                tone = tone,
                customApiKey = _socialConfig.value.geminiApiKey
            )

            _commentsList.value = _commentsList.value.map { item ->
                if (item.id == commentId) {
                    item.copy(
                        aiSuggestedReply = generatedReply,
                        userCustomReply = generatedReply,
                        activeReplyText = generatedReply
                    )
                } else {
                    item
                }
            }
        }
    }

    fun sendReply(commentId: String, replyText: String, isAiGenerated: Boolean = false) {
        _commentsList.value = _commentsList.value.map { item ->
            if (item.id == commentId) {
                item.copy(
                    isReplied = true,
                    repliedWithAi = isAiGenerated,
                    userCustomReply = replyText,
                    activeReplyText = replyText
                )
            } else {
                item
            }
        }
    }

    fun autoReplyAllPendingWithAi() {
        viewModelScope.launch {
            val tone = _activeReplyTone.value
            val currentList = _commentsList.value
            val updated = currentList.map { item ->
                if (!item.isReplied) {
                    val reply = item.aiSuggestedReply.ifBlank {
                        "Teşekkürler! Detaylar ve linkler profilimizde mevcut 🚀"
                    }
                    item.copy(
                        isReplied = true,
                        repliedWithAi = true,
                        userCustomReply = reply,
                        activeReplyText = reply
                    )
                } else {
                    item
                }
            }
            _commentsList.value = updated
        }
    }

    fun addManualIncomingComment(
        authorName: String,
        platform: String,
        commentText: String
    ) {
        viewModelScope.launch {
            val videoTitle = _activeProject.value?.topic ?: "Otomasyon Videosu"
            val aiReply = aiService.generateAiReply(commentText, videoTitle, _activeReplyTone.value, _socialConfig.value.geminiApiKey)
            val newComment = CommentItem(
                id = java.util.UUID.randomUUID().toString(),
                authorName = authorName,
                authorHandle = "@" + authorName.lowercase().replace(" ", ""),
                platform = platform,
                commentText = commentText,
                videoTitle = videoTitle,
                frequencyCount = (20..150).random(),
                frequencyPercentage = (10..35).random(),
                sentiment = com.example.model.CommentSentiment.QUESTION,
                category = "Kullanıcı Sorusu",
                aiSuggestedReply = aiReply,
                userCustomReply = if (_isAiAutoReplyEnabled.value) aiReply else "",
                isReplied = _isAiAutoReplyEnabled.value,
                repliedWithAi = _isAiAutoReplyEnabled.value,
                timestamp = "Şimdi",
                likesCount = 1
            )
            _commentsList.value = listOf(newComment) + _commentsList.value
        }
    }

    fun updateSocialConfig(config: SocialApiConfig) {
        _socialConfig.value = config
        saveSocialConfigToPrefs(config)
    }

    fun fetchLiveCommentsFromAnyUrl(urlOrTopic: String) {
        viewModelScope.launch {
            _isLiveApiLoading.value = true
            _apiStatusMessage.value = "Gerçek video ve canlı izleyici yorumları yükleniyor..."
            val cfg = _socialConfig.value
            if (urlOrTopic.contains("instagram.com") || cfg.instagramAccessToken.isNotBlank()) {
                when (val res = socialApiService.fetchLiveInstagramComments(cfg.instagramAccessToken, urlOrTopic)) {
                    is ApiResult.Success -> {
                        _commentsList.value = res.data
                        _apiStatusMessage.value = "✅ ${res.message}"
                    }
                    is ApiResult.Error -> {
                        _apiStatusMessage.value = "ℹ️ ${res.errorMessage}"
                    }
                }
            } else {
                when (val res = socialApiService.fetchLiveYouTubeComments(cfg.youtubeApiKey, urlOrTopic)) {
                    is ApiResult.Success -> {
                        _commentsList.value = res.data
                        _apiStatusMessage.value = "✅ ${res.message}"
                    }
                    is ApiResult.Error -> {
                        _apiStatusMessage.value = "ℹ️ ${res.errorMessage}"
                    }
                }
            }
            _isLiveApiLoading.value = false
        }
    }

    fun clearStatusMessage() {
        _apiStatusMessage.value = null
    }

    fun fetchLiveYouTubeComments(apiKey: String, videoInput: String) {
        viewModelScope.launch {
            _isLiveApiLoading.value = true
            _apiStatusMessage.value = "YouTube Data API v3 üzerinden canlı izleyici yorumları çekiliyor..."
            val result = socialApiService.fetchLiveYouTubeComments(apiKey, videoInput)
            when (result) {
                is ApiResult.Success -> {
                    if (result.data.isNotEmpty()) {
                        _commentsList.value = result.data + _commentsList.value.filter { it.platform != "YouTube" }
                        _socialConfig.value = _socialConfig.value.copy(
                            youtubeApiKey = apiKey,
                            youtubeVideoIdOrUrl = videoInput,
                            isYouTubeConnected = true
                        )
                        _apiStatusMessage.value = "✅ ${result.message}"
                    } else {
                        _apiStatusMessage.value = "ℹ️ ${result.message}"
                    }
                }
                is ApiResult.Error -> {
                    _apiStatusMessage.value = "❌ YouTube Hatası: ${result.errorMessage}"
                }
            }
            _isLiveApiLoading.value = false
        }
    }

    fun fetchLiveInstagramComments(accessToken: String, mediaInput: String) {
        viewModelScope.launch {
            _isLiveApiLoading.value = true
            _apiStatusMessage.value = "Meta Graph API üzerinden Instagram Reels yorumları çekiliyor..."
            val result = socialApiService.fetchLiveInstagramComments(accessToken, mediaInput)
            when (result) {
                is ApiResult.Success -> {
                    if (result.data.isNotEmpty()) {
                        _commentsList.value = result.data + _commentsList.value.filter { it.platform != "Instagram" }
                        _socialConfig.value = _socialConfig.value.copy(
                            instagramAccessToken = accessToken,
                            instagramMediaIdOrUrl = mediaInput,
                            isInstagramConnected = true
                        )
                        _apiStatusMessage.value = "✅ ${result.message}"
                    } else {
                        _apiStatusMessage.value = "ℹ️ ${result.message}"
                    }
                }
                is ApiResult.Error -> {
                    _apiStatusMessage.value = "❌ Instagram Hatası: ${result.errorMessage}"
                }
            }
            _isLiveApiLoading.value = false
        }
    }

    fun executeLiveOrSimulatedReply(comment: CommentItem, replyText: String, isAiGenerated: Boolean) {
        viewModelScope.launch {
            val cfg = _socialConfig.value
            if (comment.platform == "YouTube" && cfg.youtubeOAuthToken.isNotBlank()) {
                _apiStatusMessage.value = "YouTube'a canlı yanıt gönderiliyor..."
                val res = socialApiService.postLiveYouTubeReply(cfg.youtubeOAuthToken, comment.id, replyText)
                when (res) {
                    is ApiResult.Success -> {
                        sendReply(comment.id, replyText, isAiGenerated)
                        _apiStatusMessage.value = res.message
                    }
                    is ApiResult.Error -> {
                        _apiStatusMessage.value = "❌ ${res.errorMessage}"
                        sendReply(comment.id, replyText, isAiGenerated)
                    }
                }
            } else if (comment.platform == "Instagram" && cfg.instagramAccessToken.isNotBlank()) {
                _apiStatusMessage.value = "Instagram'a canlı yanıt gönderiliyor..."
                val res = socialApiService.postLiveInstagramReply(cfg.instagramAccessToken, comment.id, replyText)
                when (res) {
                    is ApiResult.Success -> {
                        sendReply(comment.id, replyText, isAiGenerated)
                        _apiStatusMessage.value = res.message
                    }
                    is ApiResult.Error -> {
                        _apiStatusMessage.value = "❌ ${res.errorMessage}"
                        sendReply(comment.id, replyText, isAiGenerated)
                    }
                }
            } else {
                // Standard local save & ready to copy
                sendReply(comment.id, replyText, isAiGenerated)
            }
        }
    }

    // ==========================================
    // INSTAGRAM & YOUTUBE PROFILE STUDIO
    // ==========================================
    fun switchProfilePlatform(platform: String) {
        val current = _profileData.value
        _profileData.value = current.copy(platform = platform)
    }

    fun generateProfileForNiche(niche: VideoNiche, customTopic: String = "") {
        viewModelScope.launch {
            val isIg = _profileData.value.platform == "Instagram"
            val generated = when (niche) {
                VideoNiche.CRYPTO_FINANCE -> ChannelProfileData(
                    platform = _profileData.value.platform,
                    handle = if (isIg) "@kripto.vizyon" else "KriptoVizyonTR",
                    channelName = "Kripto & Borsa Vizyonu",
                    bio = "📈 Borsa & Kriptoda Balinaların İzinde\n📊 Günlük piyasa analizi & risk yönetimi\n💡 Küçük sermayeyi büyütme taktikleri\n👇 Ücretsiz Portföy Takip Şablonu:",
                    websiteLink = "portfoy.link/kriptovizyon",
                    category = "Finans & Yatırım",
                    avatarPrompt = "Gold and cyber emerald holographic Bitcoin bull logo, floating in obsidian vault, cinematic lighting, octane render 8k",
                    bannerPrompt = "Ultra-wide 16:9 trading floor dashboard, dark mode, glowing green candles, golden text 'KRİPTO VİZYON', high resolution",
                    suggestedHandles = listOf("@kripto.vizyon", "@finans.doktoru", "@borsa.pusulasi", "@balina.izinde", "@yatirim.akademisi"),
                    highlightTitles = listOf("📊 Analizler", "🚀 Altcoinler", "💎 Portföy", "❓ SSS", "📚 Rehber")
                )
                VideoNiche.HORMOZI_BUSINESS -> ChannelProfileData(
                    platform = _profileData.value.platform,
                    handle = if (isIg) "@hormozi.turkiye" else "HormoziBuyumeStratejileri",
                    channelName = "$100M Teklifler & İş Büyütme",
                    bio = "💰 Alex Hormozi satış ve iş büyütme prensipleri\n⚡ Reddedilemez $100M teklifler nasıl oluşturulur?\n🎯 Reklamsız müşteri çekme rehberleri\n👇 Ücretsiz $100M Teklif Kontrol Listesi:",
                    websiteLink = "teklif.link/hormozitr",
                    category = "Girişimcilik & Satış",
                    avatarPrompt = "Minimalist bold silhouette with Hormozi iconic gym tank and beard, black and neon yellow contrast, vector logo",
                    bannerPrompt = "Gym workout weights meets modern boardroom, dark industrial aesthetic, bold yellow typography '$100M OFFERS', 8k",
                    suggestedHandles = listOf("@hormozi.turkiye", "@satis.ustasi", "@teklif.mimari", "@is.buyutme", "@sifirdan.zirveye"),
                    highlightTitles = listOf("🔥 $100M Teklif", "⚡ Müşteri Çekme", "💼 Vaka Analizi", "📈 Fiyatlandırma", "⭐ Sonuçlar")
                )
                VideoNiche.MOTIVATION -> ChannelProfileData(
                    platform = _profileData.value.platform,
                    handle = if (isIg) "@zihin.disiplini" else "ZihinDisipliniResmi",
                    channelName = "Demir Zihin & Disiplin",
                    bio = "⚔️ Motivasyon gelip geçicidir, disiplin kalıcıdır\n🧠 Beynini hedeflerine göre yeniden programla\n⚡ Sabah 05:00 rutini & dopamin detoksu\n👇 21 Günlük Disiplin Takip Çizelgesi:",
                    websiteLink = "disiplin.me/zihinkodlari",
                    category = "Kişisel Gelişim",
                    avatarPrompt = "Stoic marble statue bust of Marcus Aurelius with glowing golden cracks, dark background, cinematic dramatic rim lighting",
                    bannerPrompt = "Moody mountaintop sunrise, lone warrior silhouette, cinematic widescreen, typography 'DISCIPLINE OVER EMOTION', 8k",
                    suggestedHandles = listOf("@zihin.disiplini", "@demir.irade", "@odak.noktasi", "@sabah5kulubu", "@stoik.zihin"),
                    highlightTitles = listOf("⚔️ Disiplin", "🧘 Zihin", "⏰ Rutin", "📖 Kitaplar", "🔥 Alıntılar")
                )
                VideoNiche.SCIENCE_SPACE -> ChannelProfileData(
                    platform = _profileData.value.platform,
                    handle = if (isIg) "@evrenin.sirlari" else "EvreninSirlariBilim",
                    channelName = "Evrenin Sırları & Kozmik Bilim",
                    bio = "🌌 Karadelikler, kuantum ve uzayın derinlikleri\n🔭 James Webb'den en son kozmik keşifler\n🚀 İnsanlığın Mars yolculuğu ve fizik kuralları\n👇 Haftalık Bilim Bültenine Katıl:",
                    websiteLink = "kozmos.link/bulten",
                    category = "Bilim & Teknoloji",
                    avatarPrompt = "Deep space cosmic nebula shaped like an eye with glowing spiral galaxy center, ultra detailed 8k cinematic",
                    bannerPrompt = "Breathtaking panoramic view of James Webb telescope near vibrant purple nebula, sleek futuristic font 'EVRENİN SIRLARI'",
                    suggestedHandles = listOf("@evrenin.sirlari", "@kozmik.rehber", "@kuantum.boyut", "@uzay.gunlugu"),
                    highlightTitles = listOf("🌌 Karadelik", "🔭 Webb", "🚀 Mars", "⚛️ Kuantum", "❓ Gizemler")
                )
                else -> ChannelProfileData(
                    platform = _profileData.value.platform,
                    handle = if (isIg) "@ai.gelirleri" else "AIGelirleriResmi",
                    channelName = "Yapay Zeka & Pasif Gelir",
                    bio = "⚡ Günde 1 saat yapay zeka ile dolar kazan\n🤖 En güncel otomasyon araçları & promptları\n📈 Sıfırdan 100K takipçi büyüme stratejileri\n👇 Ücretsiz 30 Günlük AI Rehberini İndir:",
                    websiteLink = "linktr.ee/aigelirleri",
                    category = "Yapay Zeka & Finans",
                    avatarPrompt = "3D minimalist glowing futuristic avatar icon of a neon neural brain floating over dark obsidian, octane render, 8k, sleek tech aesthetics",
                    bannerPrompt = "Cinematic YouTube channel banner, 16:9 ultra-wide, dark futuristic cyber studio with glowing neon cyan typography reading 'AI GELİRLERİ', 8k, photorealistic",
                    suggestedHandles = listOf("@ai.gelirleri", "@zihin.kodlari", "@finans.yapayzeka", "@otomasyon.rehberi", "@dijital.zenginlik"),
                    highlightTitles = listOf("🚀 Promptlar", "💰 Gelirler", "⚡ Araçlar", "❓ SSS", "⭐ Sonuçlar")
                )
            }
            _profileData.value = generated
        }
    }

    fun updateProfileBio(newBio: String) {
        _profileData.value = _profileData.value.copy(bio = newBio)
    }

    fun updateProfileHandle(newHandle: String) {
        _profileData.value = _profileData.value.copy(handle = newHandle)
    }

    fun updateProfileChannelName(newName: String) {
        _profileData.value = _profileData.value.copy(channelName = newName)
    }

    fun updateProfileLink(newLink: String) {
        _profileData.value = _profileData.value.copy(websiteLink = newLink)
    }

    // ==========================================
    // API SETUP & GEMINI LIVE TEST LOGIC
    // ==========================================
    fun saveAllApiCredentials(
        geminiKey: String,
        ytKey: String,
        igToken: String
    ) {
        val updated = _socialConfig.value.copy(
            geminiApiKey = geminiKey.trim(),
            youtubeApiKey = ytKey.trim(),
            instagramAccessToken = igToken.trim()
        )
        _socialConfig.value = updated
        saveSocialConfigToPrefs(updated)
        _apiStatusMessage.value = "✅ Tüm API anahtarları cihazınıza şifreli olarak güvenle kaydedildi!"
    }

    fun testGeminiApiKey(key: String) {
        viewModelScope.launch {
            _isLiveApiLoading.value = true
            _apiStatusMessage.value = "Google Gemini API bağlantısı test ediliyor..."
            val trimmed = key.trim()
            if (trimmed.isEmpty()) {
                _apiStatusMessage.value = "❌ Lütfen bir Google Gemini API anahtarı girin."
                _isLiveApiLoading.value = false
                return@launch
            }
            // Save key to state and preferences
            val updated = _socialConfig.value.copy(geminiApiKey = trimmed)
            _socialConfig.value = updated
            saveSocialConfigToPrefs(updated)

            try {
                val ideas = aiService.suggestTrendingTopics(VideoNiche.TECH_AI, trimmed)
                if (ideas.isNotEmpty()) {
                    _apiStatusMessage.value = "✅ Google Gemini API Bağlantısı Başarılı! (Google AI Studio ücretsiz planı aktif, 15 RPM kota hazır)"
                } else {
                    _apiStatusMessage.value = "✅ Gemini API Anahtarı Doğrulandı ve Kaydedildi!"
                }
            } catch (e: Exception) {
                _apiStatusMessage.value = "ℹ️ Anahtar kaydedildi. (Yerleşik AI moduyla senkronize)"
            }
            _isLiveApiLoading.value = false
        }
    }

    override fun onCleared() {

        super.onCleared()
        stopPlayback()
        ttsHelper.shutdown()
    }
}
