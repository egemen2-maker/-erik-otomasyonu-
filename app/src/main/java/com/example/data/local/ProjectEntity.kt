package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CaptionStyle
import com.example.model.InstagramPublishData
import com.example.model.PlatformTarget
import com.example.model.ProjectStatus
import com.example.model.SceneItem
import com.example.model.SocialPublishPack
import com.example.model.SubtitlePosition
import com.example.model.TransitionEffect
import com.example.model.VideoAspectRatio
import com.example.model.VideoNiche
import com.example.model.VideoProject
import com.example.model.VideoScript
import com.example.model.VideoStyleSettings
import com.example.model.VideoTone
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "video_projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topic: String,
    val niche: String,
    val platformTarget: String,
    val aspectRatio: String,
    val tone: String,
    val durationSeconds: Int,
    val status: String,
    val createdAt: Long,
    val hookScore: Int,
    val estimatedViralMultiplier: String,
    val scriptJson: String,
    val publishPackJson: String,
    val styleSettingsJson: String
)

fun VideoProject.toEntity(): ProjectEntity {
    return ProjectEntity(
        id = this.id,
        topic = this.topic,
        niche = this.niche.name,
        platformTarget = this.platformTarget.name,
        aspectRatio = this.aspectRatio.name,
        tone = this.tone.name,
        durationSeconds = this.durationSeconds,
        status = this.status.name,
        createdAt = this.createdAt,
        hookScore = this.hookScore,
        estimatedViralMultiplier = this.estimatedViralMultiplier,
        scriptJson = serializeScript(this.script),
        publishPackJson = serializePublishPack(this.publishPack),
        styleSettingsJson = serializeStyleSettings(this.styleSettings)
    )
}

fun ProjectEntity.toProject(): VideoProject {
    return VideoProject(
        id = this.id,
        topic = this.topic,
        niche = try { VideoNiche.valueOf(this.niche) } catch (_: Exception) { VideoNiche.TECH_AI },
        platformTarget = try { PlatformTarget.valueOf(this.platformTarget) } catch (_: Exception) { PlatformTarget.ALL_IN_ONE },
        aspectRatio = try { VideoAspectRatio.valueOf(this.aspectRatio) } catch (_: Exception) { VideoAspectRatio.PORTRAIT_9_16 },
        tone = try { VideoTone.valueOf(this.tone) } catch (_: Exception) { VideoTone.ENERGETIC },
        durationSeconds = this.durationSeconds,
        status = try { ProjectStatus.valueOf(this.status) } catch (_: Exception) { ProjectStatus.READY_TO_PUBLISH },
        createdAt = this.createdAt,
        hookScore = this.hookScore,
        estimatedViralMultiplier = this.estimatedViralMultiplier,
        script = deserializeScript(this.scriptJson),
        publishPack = deserializePublishPack(this.publishPackJson),
        styleSettings = deserializeStyleSettings(this.styleSettingsJson)
    )
}

// JSON Serialization Helpers
private fun serializeScript(script: VideoScript): String {
    val obj = JSONObject()
    obj.put("title", script.title)
    obj.put("hookLine", script.hookLine)
    obj.put("ctaLine", script.ctaLine)
    obj.put("totalDurationSeconds", script.totalDurationSeconds)

    val scenesArray = JSONArray()
    script.scenes.forEach { scene ->
        val sObj = JSONObject()
        sObj.put("id", scene.id)
        sObj.put("orderIndex", scene.orderIndex)
        sObj.put("durationSeconds", scene.durationSeconds.toDouble())
        sObj.put("narrationText", scene.narrationText)
        sObj.put("visualDescription", scene.visualDescription)
        sObj.put("onScreenSubtitle", scene.onScreenSubtitle)
        sObj.put("transitionType", scene.transitionType.name)
        sObj.put("soundEffectCue", scene.soundEffectCue)
        sObj.put("bgThemeIndex", scene.bgThemeIndex)

        val hlArray = JSONArray()
        scene.textHighlightWords.forEach { hlArray.put(it) }
        sObj.put("textHighlightWords", hlArray)

        scenesArray.put(sObj)
    }
    obj.put("scenes", scenesArray)
    return obj.toString()
}

private fun deserializeScript(jsonStr: String): VideoScript {
    return try {
        val obj = JSONObject(jsonStr)
        val title = obj.optString("title", "Otomasyon Video")
        val hookLine = obj.optString("hookLine", "Bunu asla duymadınız!")
        val ctaLine = obj.optString("ctaLine", "Takip etmeyi ve kaydetmeyi unutmayın!")
        val totalDurationSeconds = obj.optInt("totalDurationSeconds", 30)

        val scenesArray = obj.optJSONArray("scenes")
        val scenes = mutableListOf<SceneItem>()
        if (scenesArray != null) {
            for (i in 0 until scenesArray.length()) {
                val sObj = scenesArray.getJSONObject(i)
                val hlList = mutableListOf<String>()
                val hlArray = sObj.optJSONArray("textHighlightWords")
                if (hlArray != null) {
                    for (j in 0 until hlArray.length()) {
                        hlList.add(hlArray.getString(j))
                    }
                }
                scenes.add(
                    SceneItem(
                        id = sObj.optInt("id", i),
                        orderIndex = sObj.optInt("orderIndex", i),
                        durationSeconds = sObj.optDouble("durationSeconds", 4.0).toFloat(),
                        narrationText = sObj.optString("narrationText", ""),
                        visualDescription = sObj.optString("visualDescription", ""),
                        onScreenSubtitle = sObj.optString("onScreenSubtitle", ""),
                        transitionType = try {
                            TransitionEffect.valueOf(sObj.optString("transitionType", "ZOOM_IN"))
                        } catch (_: Exception) { TransitionEffect.ZOOM_IN },
                        soundEffectCue = sObj.optString("soundEffectCue", "Whoosh"),
                        bgThemeIndex = sObj.optInt("bgThemeIndex", 0),
                        textHighlightWords = hlList
                    )
                )
            }
        }
        VideoScript(title, hookLine, scenes, ctaLine, totalDurationSeconds)
    } catch (_: Exception) {
        VideoScript("Video", "", emptyList(), "")
    }
}

private fun serializePublishPack(pack: SocialPublishPack): String {
    val root = JSONObject()
    
    // Instagram
    val ig = JSONObject()
    ig.put("caption", pack.instagramPack.caption)
    ig.put("audioRecommendation", pack.instagramPack.audioRecommendation)
    ig.put("firstCommentPin", pack.instagramPack.firstCommentPin)
    ig.put("bestPostingTime", pack.instagramPack.bestPostingTime)
    ig.put("coverTitle", pack.instagramPack.coverTitle)
    
    val igHooks = JSONArray()
    pack.instagramPack.viralHooks.forEach { igHooks.put(it) }
    ig.put("viralHooks", igHooks)

    val igTopTags = JSONArray()
    pack.instagramPack.topHashtags.forEach { igTopTags.put(it) }
    ig.put("topHashtags", igTopTags)

    val igNicheTags = JSONArray()
    pack.instagramPack.nicheHashtags.forEach { igNicheTags.put(it) }
    ig.put("nicheHashtags", igNicheTags)
    
    root.put("instagram", ig)

    // YouTube
    val yt = JSONObject()
    yt.put("selectedTitle", pack.youtubePack.selectedTitle)
    yt.put("description", pack.youtubePack.description)
    yt.put("thumbnailPrompt", pack.youtubePack.thumbnailPrompt)
    yt.put("pinnedComment", pack.youtubePack.pinnedComment)
    yt.put("categoryName", pack.youtubePack.categoryName)
    yt.put("estimatedCtr", pack.youtubePack.estimatedCtr)

    val ytTitles = JSONArray()
    pack.youtubePack.titleOptions.forEach { ytTitles.put(it) }
    yt.put("titleOptions", ytTitles)

    val ytTags = JSONArray()
    pack.youtubePack.tags.forEach { ytTags.put(it) }
    yt.put("tags", ytTags)

    root.put("youtube", yt)

    return root.toString()
}

private fun deserializePublishPack(jsonStr: String): SocialPublishPack {
    return try {
        val root = JSONObject(jsonStr)
        val ig = root.optJSONObject("instagram") ?: JSONObject()
        val yt = root.optJSONObject("youtube") ?: JSONObject()

        val igHooks = mutableListOf<String>()
        ig.optJSONArray("viralHooks")?.let { arr -> for (i in 0 until arr.length()) igHooks.add(arr.getString(i)) }

        val igTopTags = mutableListOf<String>()
        ig.optJSONArray("topHashtags")?.let { arr -> for (i in 0 until arr.length()) igTopTags.add(arr.getString(i)) }

        val igNicheTags = mutableListOf<String>()
        ig.optJSONArray("nicheHashtags")?.let { arr -> for (i in 0 until arr.length()) igNicheTags.add(arr.getString(i)) }

        val igPack = InstagramPublishData(
            caption = ig.optString("caption", ""),
            viralHooks = igHooks,
            topHashtags = igTopTags,
            nicheHashtags = igNicheTags,
            audioRecommendation = ig.optString("audioRecommendation", "Trending Synthwave Beat"),
            firstCommentPin = ig.optString("firstCommentPin", ""),
            bestPostingTime = ig.optString("bestPostingTime", "Bugün 18:00 - 21:00"),
            coverTitle = ig.optString("coverTitle", "")
        )

        val ytTitles = mutableListOf<String>()
        yt.optJSONArray("titleOptions")?.let { arr -> for (i in 0 until arr.length()) ytTitles.add(arr.getString(i)) }

        val ytTags = mutableListOf<String>()
        yt.optJSONArray("tags")?.let { arr -> for (i in 0 until arr.length()) ytTags.add(arr.getString(i)) }

        val ytPack = com.example.model.YouTubePublishData(
            titleOptions = ytTitles,
            selectedTitle = yt.optString("selectedTitle", ""),
            description = yt.optString("description", ""),
            tags = ytTags,
            thumbnailPrompt = yt.optString("thumbnailPrompt", ""),
            pinnedComment = yt.optString("pinnedComment", ""),
            categoryName = yt.optString("categoryName", "Bilim ve Teknoloji"),
            estimatedCtr = yt.optString("estimatedCtr", "%12.4 CTR")
        )

        SocialPublishPack(igPack, ytPack)
    } catch (_: Exception) {
        SocialPublishPack(
            InstagramPublishData(caption = ""),
            com.example.model.YouTubePublishData()
        )
    }
}

private fun serializeStyleSettings(settings: VideoStyleSettings): String {
    val obj = JSONObject()
    obj.put("captionStyle", settings.captionStyle.name)
    obj.put("subtitlePosition", settings.subtitlePosition.name)
    obj.put("fontSizeSp", settings.fontSizeSp)
    obj.put("bgMusicName", settings.bgMusicName)
    obj.put("voiceType", settings.voiceType)
    obj.put("voiceSpeed", settings.voiceSpeed.toDouble())
    obj.put("showHookBanner", settings.showHookBanner)
    obj.put("showWaveform", settings.showWaveform)
    return obj.toString()
}

private fun deserializeStyleSettings(jsonStr: String): VideoStyleSettings {
    return try {
        val obj = JSONObject(jsonStr)
        VideoStyleSettings(
            captionStyle = try {
                CaptionStyle.valueOf(obj.optString("captionStyle", "KARAOKE_POP"))
            } catch (_: Exception) { CaptionStyle.KARAOKE_POP },
            subtitlePosition = try {
                SubtitlePosition.valueOf(obj.optString("subtitlePosition", "CENTER"))
            } catch (_: Exception) { SubtitlePosition.CENTER },
            fontSizeSp = obj.optInt("fontSizeSp", 22),
            bgMusicName = obj.optString("bgMusicName", "Cyberpunk Pulse"),
            voiceType = obj.optString("voiceType", "Derin & Karizmatik"),
            voiceSpeed = obj.optDouble("voiceSpeed", 1.0).toFloat(),
            showHookBanner = obj.optBoolean("showHookBanner", true),
            showWaveform = obj.optBoolean("showWaveform", true)
        )
    } catch (_: Exception) {
        VideoStyleSettings()
    }
}
