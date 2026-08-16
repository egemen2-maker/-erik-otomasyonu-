package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TextToSpeechHelper
import com.example.data.ai.GeminiAutomationService
import com.example.data.local.AppDatabase
import com.example.data.local.ProjectRepository
import com.example.model.CaptionStyle
import com.example.model.CommentItem
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
            _suggestedIdeas.value = aiService.suggestTrendingTopics(niche)
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
            try {
                val generated = aiService.generateCompleteVideoAutomation(
                    topic = topic,
                    niche = niche,
                    platformTarget = platformTarget,
                    tone = tone,
                    durationSeconds = durationSeconds,
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

                // Speak scene text via TTS
                ttsHelper.speak(
                    text = scene.narrationText,
                    speechRate = project.styleSettings.voiceSpeed
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
            val comments = aiService.generateTopComments(title, niche)
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
            val generatedReply = aiService.generateAiReply(target.commentText, videoTitle, tone)

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
            val aiReply = aiService.generateAiReply(commentText, videoTitle, _activeReplyTone.value)
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

    override fun onCleared() {

        super.onCleared()
        stopPlayback()
        ttsHelper.shutdown()
    }
}
