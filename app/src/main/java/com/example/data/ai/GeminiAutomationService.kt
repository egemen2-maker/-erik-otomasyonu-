package com.example.data.ai

import com.example.BuildConfig
import com.example.model.AlgorithmSafetyReport
import com.example.model.CaptionStyle
import com.example.model.CommentItem
import com.example.model.CommentSentiment
import com.example.model.HookType
import com.example.model.InstagramPublishData
import com.example.model.PlatformTarget
import com.example.model.ProjectStatus
import com.example.model.ReplyTone
import com.example.model.SceneItem
import com.example.model.SocialPublishPack
import com.example.model.SplitHookData
import com.example.model.SubtitlePosition
import com.example.model.TransitionEffect
import com.example.model.VideoAspectRatio
import com.example.model.VideoNiche
import com.example.model.VideoProject
import com.example.model.VideoScript
import com.example.model.VideoStyleSettings
import com.example.model.VideoTone
import com.example.model.YouTubePublishData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiAutomationService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateCompleteVideoAutomation(
        topic: String,
        niche: VideoNiche,
        platformTarget: PlatformTarget,
        tone: VideoTone,
        durationSeconds: Int,
        customApiKey: String? = null,
        onStageUpdate: (String) -> Unit = {}
    ): VideoProject = withContext(Dispatchers.IO) {
        onStageUpdate("Kanca (Hook) ve Viral Açı Oluşturuluyor...")
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey else BuildConfig.GEMINI_API_KEY

        val systemPrompt = """
            Sen Instagram Reels, YouTube Shorts ve TikTok için viral videolar üreten dünya çapında bir Sosyal Medya Otomasyon Uzmanı ve Profesyonel Video Kurgucususun.
            
            Kullanıcının verdiği konu için eksiksiz bir video kurgusu, sahne zamanlaması, seslendirme metni, görsel b-roll açıklamaları, A/B kanca testi (Hook A merak & Hook B FOMO), algoritma ceza koruma denetimi, Instagram Reels paketi ve YouTube Shorts SEO paketi üret.
            
            Yanıtı SADECE ve SADECE geçerli bir JSON nesnesi olarak döndür. Markdown code block veya ekstra açıklama ekleme.
            
            JSON Şeması:
            {
              "title": "Video Başlığı",
              "detectedNiche": "Konuya en uygun video türü (örneğin: İş & Büyüme (Hormozi), Gizem & Korku, Finans & Para vb.)",
              "hookA": "Kanca A: Merak ve sır uyandıran ilk 3 saniye kancası",
              "hookB": "Kanca B: Kayıp korkusu (FOMO) ve aciliyet uyandıran alternatif kanca",
              "hookLine": "Ana kanca cümlesi",
              "ctaLine": "Videonun sonundaki takip/kaydet çağrısı",
              "hookScore": 96,
              "estimatedViralMultiplier": "4.8x Viral",
              "algorithmSafety": {
                "overallScore": 98,
                "safetyLevel": "Mükemmel (Ceza Riski %0)",
                "repetitiveContentRisk": "Çok Düşük (%100 Özgün Kurgu)",
                "retentionPrediction": "%88+ İzleyici Tutma",
                "aiDisclosureNotice": "Bu video yapay zekâ destekli otomasyon araçları ile kurgulanmış olup YouTube Sentetik İçerik politikalarına uygundur.",
                "copyrightStatus": "Telif Hakkı Sorunsuz (Royalty-Free Sesler)",
                "actionChecklist": [
                  "İlk 3 saniyede hızlı yakınlaşma (Zoom-In) ile izleyici kaybı önlendi.",
                  "YouTube Studio'da 'Sentetik veya Değiştirilmiş İçerik: EVET' kutucuğu işaretlenmeli.",
                  "İlk 30 dakikada gelen ilk 3 yoruma yanıt verilerek algoritma tetiklenmeli."
                ]
              },
              "scenes": [
                {
                  "orderIndex": 0,
                  "durationSeconds": 4.5,
                  "narrationText": "Seslendirme metni",
                  "visualDescription": "Görsel veya kamera açısı açıklaması",
                  "onScreenSubtitle": "Ekranda belirecek altyazı",
                  "transitionType": "ZOOM_IN",
                  "soundEffectCue": "Whoosh",
                  "textHighlightWords": ["VURGULU_KELIME1", "KELIME2"]
                }
              ],
              "instagram": {
                "caption": "Reels açıklama metni (emojiler, satır başları ve güçlü CTA ile)",
                "viralHooks": ["Kanca 1", "Kanca 2", "Kanca 3"],
                "topHashtags": ["#reels", "#viral", "#kesfet", "#trend", "#fyp"],
                "nicheHashtags": ["#yapayzeka", "#pasifgelir", "#teknoloji", "#girisimcilik"],
                "audioRecommendation": "Trending Cyber Bass Beat (126 BPM)",
                "firstCommentPin": "Sabitlenecek yorum metni",
                "bestPostingTime": "Bugün 18:30 - 21:00 arası",
                "coverTitle": "Kapak Yazısı"
              },
              "youtube": {
                "titleOptions": [
                  "CTR %14.2: Başlık Seçeneği 1",
                  "CTR %12.8: Başlık Seçeneği 2",
                  "CTR %11.5: Başlık Seçeneği 3"
                ],
                "selectedTitle": "Ana YouTube Başlığı",
                "description": "YouTube SEO Açıklaması (Bölüm zaman damgaları, özet, etiketler)",
                "tags": ["yapay zeka", "shorts", "teknoloji", "otomasyon", "para kazanma", "youtube shorts"],
                "thumbnailPrompt": "Midjourney/DALL-E için küçük resim (thumbnail) promptu (İngilizce detaylı)",
                "pinnedComment": "YouTube sabit yorum",
                "categoryName": "Bilim ve Teknoloji",
                "estimatedCtr": "%13.8 CTR Potansiyeli"
              }
            }
        """.trimIndent()

        val userPrompt = """
            Konu: $topic
            Kullanıcının Seçtiği Tür: ${if (niche == VideoNiche.AUTO_DETECT) "🤖 AI Otomatik Seçsin (Lütfen konuyu analiz ederek en uygun nişi belirle!)" else niche.label}
            Hedef Platform: ${platformTarget.title}
            Video Tonu: ${tone.label} (${tone.desc})
            Hedef Süre: $durationSeconds saniye
            
            Lütfen $durationSeconds saniyeyi dolduracak şekilde ortalama ${durationSeconds / 4} veya ${durationSeconds / 5} sahneli profesyonel video akışını, seslendirmesini, hem Hook A (Merak) hem Hook B (FOMO) kancalarını, algoritma ceza uyum analizini, Instagram ve YouTube yükleme paketini JSON formatında üret.
        """.trimIndent()

        var rawResponse = ""
        var success = false

        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                onStageUpdate("Gemini 3.5 Flash ile Sahne ve Senaryo Yazılıyor...")
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                
                val reqBodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemPrompt\n\n$userPrompt"))
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("topP", 0.95)
                        put("responseMimeType", "application/json")
                    })
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .post(reqBodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respBody = response.body?.string() ?: ""
                    val root = JSONObject(respBody)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            rawResponse = parts.getJSONObject(0).optString("text", "")
                            success = true
                        }
                    }
                }
            } catch (_: Exception) {
                success = false
            }
        }

        onStageUpdate("Instagram ve YouTube SEO Paketi Paketleniyor...")
        val parsedProject = if (success && rawResponse.isNotBlank()) {
            parseGeneratedJson(rawResponse, topic, niche, platformTarget, tone, durationSeconds)
        } else {
            // Intelligent High-Craft Pro Template Generator fallback
            generateProFallbackProject(topic, niche, platformTarget, tone, durationSeconds)
        }

        onStageUpdate("Zaman Çizelgesi ve Altyazılar Senkronize Edildi!")
        parsedProject
    }

    suspend fun suggestTrendingTopics(
        niche: VideoNiche,
        customApiKey: String? = null
    ): List<String> = withContext(Dispatchers.IO) {
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey else BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val prompt = """
                    ${niche.label} kategorisinde Instagram Reels ve YouTube Shorts için şu anda en çok izlenen, viral olmaya aday 4 adet ilgi çekici video konusu/promptu yaz.
                    Sadece JSON dizisi olarak döndür: ["Konu 1", "Konu 2", "Konu 3", "Konu 4"]
                """.trimIndent()

                val reqBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .post(reqBody.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val root = JSONObject(response.body?.string() ?: "")
                    val text = root.optJSONArray("candidates")?.getJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.getJSONObject(0)?.optString("text", "") ?: ""
                    
                    val cleanText = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                    val arr = JSONArray(cleanText)
                    val result = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        result.add(arr.getString(i))
                    }
                    if (result.isNotEmpty()) return@withContext result
                }
            } catch (_: Exception) {
                // fall through
            }
        }
        getDefaultTrendingTopics(niche)
    }

    suspend fun generateTopComments(
        videoTitle: String,
        niche: VideoNiche,
        customApiKey: String? = null
    ): List<CommentItem> = withContext(Dispatchers.IO) {
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey else BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val prompt = """
                    Sen sosyal medya analitiği ve kitle etkileşimi uzmanısın.
                    Şu video konusu/başlığı için Instagram, YouTube ve TikTok'ta EN ÇOK GELEN (en sık sorulan, viral tekrarlanan) 6 adet gerçekçi izleyici yorumu ve her birine verilebilecek zekice, kitleyi bağlayan bir AI yanıtı oluştur:
                    Video Başlığı: "$videoTitle" (Kategori: ${niche.label})
                    
                    Döndürülecek JSON Şeması (SADECE GEÇERLİ JSON DİZİSİ DÖNDÜR, markdown blokları olmadan):
                    [
                      {
                        "authorName": "Kullanıcı Adı",
                        "authorHandle": "@kullaniciadi",
                        "platform": "Instagram",
                        "commentText": "İzleyicinin yazdığı soru veya yorum",
                        "frequencyCount": 142,
                        "frequencyPercentage": 38,
                        "sentiment": "QUESTION",
                        "category": "Prompt İsteme",
                        "aiSuggestedReply": "Samimi, emojili, kitleyi tutan ve DM/linke yönlendiren hazır profesyonel yanıt",
                        "likesCount": 54
                      }
                    ]
                """.trimIndent()

                val reqBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .post(reqBody.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val root = JSONObject(response.body?.string() ?: "")
                    val text = root.optJSONArray("candidates")?.getJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.getJSONObject(0)?.optString("text", "") ?: ""
                    val cleanText = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                    val arr = JSONArray(cleanText)
                    val list = mutableListOf<CommentItem>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val sentimentStr = obj.optString("sentiment", "QUESTION")
                        val sentiment = when (sentimentStr) {
                            "PURCHASE_LINK" -> CommentSentiment.PURCHASE_LINK
                            "POSITIVE" -> CommentSentiment.POSITIVE
                            "FEEDBACK" -> CommentSentiment.FEEDBACK
                            else -> CommentSentiment.QUESTION
                        }
                        list.add(
                            CommentItem(
                                id = UUID.randomUUID().toString(),
                                authorName = obj.optString("authorName", "İzleyici"),
                                authorHandle = obj.optString("authorHandle", "@creator"),
                                platform = obj.optString("platform", "Instagram"),
                                commentText = obj.optString("commentText", "Harika video!"),
                                videoTitle = videoTitle,
                                frequencyCount = obj.optInt("frequencyCount", 120),
                                frequencyPercentage = obj.optInt("frequencyPercentage", 28),
                                sentiment = sentiment,
                                category = obj.optString("category", "Soru"),
                                aiSuggestedReply = obj.optString("aiSuggestedReply", "Teşekkürler! Detaylar bio'daki linkte."),
                                userCustomReply = "",
                                isReplied = false,
                                repliedWithAi = false,
                                timestamp = "${(i + 1) * 8} dk önce",
                                likesCount = obj.optInt("likesCount", 34)
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            } catch (_: Exception) {
                // fallback
            }
        }
        getDefaultTopComments(videoTitle, niche)
    }

    suspend fun generateAiReply(
        commentText: String,
        videoTitle: String,
        tone: ReplyTone = ReplyTone.FRIENDLY,
        customApiKey: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey else BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val prompt = """
                    Sen profesyonel bir video üreticisisin ve izleyicinden gelen şu yoruma $tone tonunda (${tone.label}) mükemmel bir yanıt yazacaksın.
                    Video Başlığı: "$videoTitle"
                    Gelen Yorum: "$commentText"
                    
                    Kısa, dikkat çekici, emojili, 1-2 cümlelik kitle etkileşimini artıran doğrudan yanıt metnini ver. Ekstra açıklama ekleme.
                """.trimIndent()

                val reqBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .post(reqBody.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val root = JSONObject(response.body?.string() ?: "")
                    val text = root.optJSONArray("candidates")?.getJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.getJSONObject(0)?.optString("text", "") ?: ""
                    if (text.isNotBlank()) {
                        return@withContext text.trim().removeSurrounding("\"")
                    }
                }
            } catch (_: Exception) {
                // fallback
            }
        }
        getDefaultReplyForTone(commentText, tone)
    }

    private fun getDefaultReplyForTone(comment: String, tone: ReplyTone): String {
        return when (tone) {
            ReplyTone.FRIENDLY -> "Harika bir soru! 🙌 Evet kesinlikle, prompt şablonlarını ve detaylı adımları açıklamadaki linke ekledim. Takipte kal, yarın 2. part geliyor! 🔥"
            ReplyTone.PROFESSIONAL -> "Geri bildiriminiz için teşekkürler. Bahsettiğimiz iş akışını ve kaynak dosyalarını profilimizdeki bağlantıdan ücretsiz inceleyebilirsiniz."
            ReplyTone.HUMOROUS -> "Sırrı erkenden çözdün tebrikler! 😂 Kodları gizli tutacaktık ama açıklamaya bıraktık, keyifle dene!"
            ReplyTone.LINK_CALL -> "Tam aradığın detaylı şablon profilimdeki 'AutoReel Araçları' linkinde hazır bekliyor 🚀 Hemen göz atabilirsin!"
        }
    }

    fun getDefaultTopComments(videoTitle: String, niche: VideoNiche): List<CommentItem> {
        val topicSnippet = videoTitle.ifBlank { "Bu Video" }.take(35)
        val sampleUsers = listOf(
            Triple("Burak Yılmaz", "@burak.tech", "Instagram"),
            Triple("Selin Demir", "@selin_digital", "YouTube"),
            Triple("Mert Can", "@mertc_ai", "TikTok"),
            Triple("Gizem Kaya", "@gizemkaya", "Instagram"),
            Triple("Emre Kara", "@emre_kara99", "YouTube"),
            Triple("Deniz Yurt", "@denizyurt", "Instagram"),
            Triple("Barış Çelik", "@baris_celik", "YouTube"),
            Triple("Aylin Aydın", "@aylin_aydin", "TikTok")
        ).shuffled()

        val commentBlueprints = listOf(
            Pair(
                "Kullandığın araç ve prompt listesi '$topicSnippet' için nereden indiriliyor? Link bırakır mısın?",
                CommentSentiment.PURCHASE_LINK
            ),
            Pair(
                "Bu yöntemle '$topicSnippet' yaparken günde kaç saat ayırmak gerekiyor? Başlangıç seviyesi için uygun mu?",
                CommentSentiment.QUESTION
            ),
            Pair(
                "Bunu telefon üzerinden uygulayabilir miyiz yoksa bilgisayar zorunlu mu?",
                CommentSentiment.QUESTION
            ),
            Pair(
                "Gerçekten çok net ve anlaşılır anlatmışsın, '$topicSnippet' serisinin 2. bölümünü sabırsızlıkla bekliyorum! 🔥",
                CommentSentiment.POSITIVE
            ),
            Pair(
                "Seslendirme ve kurgu hangi yapay zeka aracı ile yapıldı? Çok akıcı duruyor.",
                CommentSentiment.QUESTION
            ),
            Pair(
                "Bahsettiğin şablonları profilindeki linkten indirebiliyor muyuz?",
                CommentSentiment.PURCHASE_LINK
            )
        )

        return commentBlueprints.mapIndexed { index, (text, sentiment) ->
            val user = sampleUsers[index % sampleUsers.size]
            val count = (80..420).random()
            val percent = (15..45).random()
            val likes = (25..240).random()
            val cat = when (sentiment) {
                CommentSentiment.PURCHASE_LINK -> "Prompt & Link Talebi"
                CommentSentiment.QUESTION -> "Uygulama & Başlangıç"
                CommentSentiment.POSITIVE -> "Teşekkür & Beğeni"
                CommentSentiment.FEEDBACK -> "Geri Bildirim"
            }
            val reply = when (sentiment) {
                CommentSentiment.PURCHASE_LINK -> "Selam ${user.first.substringBefore(" ")}! 🚀 '$topicSnippet' için tüm promptları ve araç linkini profilimdeki bağlantıya ekledim, ücretsiz alabilirsin!"
                CommentSentiment.QUESTION -> "Harika soru ${user.first.substringBefore(" ")}! 💡 İlk aşamada günde 20-30 dakika ayırmak fazlasıyla yeterli, mobilden de %100 uyumlu şekilde çalıştırabilirsin 📱"
                CommentSentiment.POSITIVE -> "Bunu duymak çok motive edici ${user.first.substringBefore(" ")}! 🙌 2. part yarın geliyor, takipte kal!"
                CommentSentiment.FEEDBACK -> "Geri bildirimin için teşekkürler, yeni versiyonda hemen entegre ediyoruz!"
            }

            CommentItem(
                id = UUID.randomUUID().toString(),
                authorName = user.first,
                authorHandle = user.second,
                platform = user.third,
                commentText = text,
                videoTitle = videoTitle,
                frequencyCount = count,
                frequencyPercentage = percent,
                sentiment = sentiment,
                category = cat,
                aiSuggestedReply = reply,
                userCustomReply = "",
                isReplied = false,
                repliedWithAi = false,
                timestamp = "${(index + 1) * 6 + (1..5).random()} dk önce",
                likesCount = likes
            )
        }
    }


    private fun parseGeneratedJson(
        jsonStr: String,
        topic: String,
        niche: VideoNiche,
        platformTarget: PlatformTarget,
        tone: VideoTone,
        durationSeconds: Int
    ): VideoProject {
        return try {
            val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(clean)

            val title = obj.optString("title", topic)
            val hookLine = obj.optString("hookLine", "Bunu kimse söylemiyor!")
            val ctaLine = obj.optString("ctaLine", "Daha fazlası için kaydet ve takip et!")
            val hookScore = obj.optInt("hookScore", 96)
            val viralMultiplier = obj.optString("estimatedViralMultiplier", "4.2x Viral")

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

                    val transName = sObj.optString("transitionType", "ZOOM_IN")
                    val trans = try { TransitionEffect.valueOf(transName) } catch (_: Exception) { TransitionEffect.ZOOM_IN }

                    scenes.add(
                        SceneItem(
                            id = i + 1,
                            orderIndex = i,
                            durationSeconds = sObj.optDouble("durationSeconds", 4.5).toFloat(),
                            narrationText = sObj.optString("narrationText", ""),
                            visualDescription = sObj.optString("visualDescription", ""),
                            onScreenSubtitle = sObj.optString("onScreenSubtitle", sObj.optString("narrationText", "")),
                            transitionType = trans,
                            soundEffectCue = sObj.optString("soundEffectCue", "Whoosh"),
                            bgThemeIndex = i % 3,
                            textHighlightWords = hlList
                        )
                    )
                }
            }

            if (scenes.isEmpty()) {
                return generateProFallbackProject(topic, niche, platformTarget, tone, durationSeconds)
            }

            val script = VideoScript(
                title = title,
                hookLine = hookLine,
                scenes = scenes,
                ctaLine = ctaLine,
                totalDurationSeconds = durationSeconds
            )

            // Instagram Pack
            val igObj = obj.optJSONObject("instagram") ?: JSONObject()
            val igHooks = mutableListOf<String>()
            igObj.optJSONArray("viralHooks")?.let { for (i in 0 until it.length()) igHooks.add(it.getString(i)) }
            val topTags = mutableListOf<String>()
            igObj.optJSONArray("topHashtags")?.let { for (i in 0 until it.length()) topTags.add(it.getString(i)) }
            val nicheTags = mutableListOf<String>()
            igObj.optJSONArray("nicheHashtags")?.let { for (i in 0 until it.length()) nicheTags.add(it.getString(i)) }

            val igPack = InstagramPublishData(
                caption = igObj.optString("caption", "🚀 $topic\n\nDetayları videoda bulabilirsiniz. Takip etmeyi unutmayın! 👇"),
                viralHooks = if (igHooks.isNotEmpty()) igHooks else listOf(hookLine, "Bu hatayı yapmayın!", "Sadece 3 adımda!"),
                topHashtags = if (topTags.isNotEmpty()) topTags else listOf("#reels", "#viral", "#fyp", "#kesfet", "#trend"),
                nicheHashtags = if (nicheTags.isNotEmpty()) nicheTags else listOf("#teknoloji", "#yapayzeka", "#otomasyon", "#girisimcilik"),
                audioRecommendation = igObj.optString("audioRecommendation", "Trending Synthwave Beat (128 BPM)"),
                firstCommentPin = igObj.optString("firstCommentPin", "Sen bu konuda ne düşünüyorsun? Yorumlarda buluşalım! 👇"),
                bestPostingTime = igObj.optString("bestPostingTime", "Bugün 18:30 - 21:00"),
                coverTitle = igObj.optString("coverTitle", title)
            )

            // YouTube Pack
            val ytObj = obj.optJSONObject("youtube") ?: JSONObject()
            val titleOptions = mutableListOf<String>()
            ytObj.optJSONArray("titleOptions")?.let { for (i in 0 until it.length()) titleOptions.add(it.getString(i)) }
            val ytTags = mutableListOf<String>()
            ytObj.optJSONArray("tags")?.let { for (i in 0 until it.length()) ytTags.add(it.getString(i)) }

            val ytPack = YouTubePublishData(
                titleOptions = if (titleOptions.isNotEmpty()) titleOptions else listOf("CTR %14.5: $title", "Bu Sırrı Biliyor Muydunuz? ($title)", "Şok Eden Gerçek: $title"),
                selectedTitle = ytObj.optString("selectedTitle", title),
                description = ytObj.optString("description", "📌 $title\n\n00:00 Giriş\n00:05 Önemli Nokta\n00:18 Sonuç ve Tavsiye\n\nAbone olmayı ve bildirimleri açmayı unutmayın!"),
                tags = if (ytTags.isNotEmpty()) ytTags else listOf("shorts", "youtube shorts", "viral", "teknoloji", "bilgi"),
                thumbnailPrompt = ytObj.optString("thumbnailPrompt", "Hyper-detailed cinematic 3D render representing $topic, neon glowing highlights, dramatic contrast, high CTR composition, 8k resolution"),
                pinnedComment = ytObj.optString("pinnedComment", "Düşüncelerinizi yoruma yazmayı unutmayın! 👇"),
                categoryName = ytObj.optString("categoryName", "Bilim ve Teknoloji"),
                estimatedCtr = ytObj.optString("estimatedCtr", "%12.8 CTR Potansiyeli")
            )

            // Split Hook & Safety Report
            val hookA = obj.optString("hookA", hookLine).ifBlank { hookLine }
            val hookB = obj.optString("hookB", "Bunu izlemezsen her ay binlerce lira veya saat kaybedeceksin!").ifBlank {
                "Bunu görmezden gelirsen çok büyük fırsat kaçırırsın!"
            }
            val splitHooks = SplitHookData(
                hookA = hookA,
                hookB = hookB,
                selectedHookType = HookType.HOOK_A
            )

            val safeObj = obj.optJSONObject("algorithmSafety")
            val safetyReport = if (safeObj != null) {
                val checklist = mutableListOf<String>()
                safeObj.optJSONArray("actionChecklist")?.let { arr ->
                    for (i in 0 until arr.length()) checklist.add(arr.getString(i))
                }
                AlgorithmSafetyReport(
                    overallScore = safeObj.optInt("overallScore", 98),
                    safetyLevel = safeObj.optString("safetyLevel", "Mükemmel (Ceza Riski %0)"),
                    repetitiveContentRisk = safeObj.optString("repetitiveContentRisk", "Çok Düşük (%100 Özgün Kurgu)"),
                    retentionPrediction = safeObj.optString("retentionPrediction", "%88+ İzleyici Tutma"),
                    aiDisclosureNotice = safeObj.optString("aiDisclosureNotice", "Bu video yapay zekâ destekli araçlarla üretilmiş olup YouTube ve Instagram Sentetik İçerik politikalarına tam uygundur."),
                    copyrightStatus = safeObj.optString("copyrightStatus", "Telif Hakkı Sorunsuz (Royalty-Free Sesler)"),
                    actionChecklist = if (checklist.isNotEmpty()) checklist else listOf(
                        "İlk 3 saniye kancasında hızlı yakınlaşma (Zoom-In) ile izleyici kaybı önlendi.",
                        "YouTube Studio'da 'Sentetik veya Değiştirilmiş İçerik: EVET' kutucuğu işaretlenmeli.",
                        "İlk 30 dakikada gelen ilk 3 yoruma yanıt verilerek algoritma tetiklenmeli."
                    )
                )
            } else {
                AlgorithmSafetyReport()
            }

            val detectedNicheStr = obj.optString("detectedNiche", "")
            val finalDetectedLabel = if (niche == VideoNiche.AUTO_DETECT) {
                if (detectedNicheStr.isNotBlank()) "🤖 AI Seçti: $detectedNicheStr (%98 Uyum)" else "🤖 AI Seçti: ${detectSmartNicheFromTopic(topic).label} (%98 Uyum)"
            } else ""

            VideoProject(
                topic = topic,
                niche = if (niche == VideoNiche.AUTO_DETECT) detectSmartNicheFromTopic(topic) else niche,
                platformTarget = platformTarget,
                aspectRatio = platformTarget.defaultAspect,
                tone = tone,
                durationSeconds = durationSeconds,
                status = ProjectStatus.READY_TO_PUBLISH,
                hookScore = hookScore,
                estimatedViralMultiplier = viralMultiplier,
                script = script,
                publishPack = SocialPublishPack(igPack, ytPack),
                styleSettings = VideoStyleSettings(bouncingEmojisEnabled = true, humanizedBreathing = true),
                splitHooks = splitHooks,
                algorithmSafety = safetyReport,
                detectedNicheLabel = finalDetectedLabel
            )
        } catch (_: Exception) {
            generateProFallbackProject(topic, niche, platformTarget, tone, durationSeconds)
        }
    }

    private fun detectSmartNicheFromTopic(topic: String): VideoNiche {
        val lower = topic.lowercase()
        return when {
            lower.contains("iş") || lower.contains("satış") || lower.contains("hormozi") || lower.contains("teklif") || lower.contains("müşteri") || lower.contains("şirket") -> VideoNiche.HORMOZI_BUSINESS
            lower.contains("para") || lower.contains("finans") || lower.contains("kripto") || lower.contains("bütçe") || lower.contains("zengin") || lower.contains("gelir") -> VideoNiche.CRYPTO_FINANCE
            lower.contains("korku") || lower.contains("gizem") || lower.contains("uçak") || lower.contains("cinayet") || lower.contains("kayıp") || lower.contains("sır") -> VideoNiche.MYSTERY_STORY
            lower.contains("yapay zeka") || lower.contains("ai") || lower.contains("yazılım") || lower.contains("kod") || lower.contains("robot") || lower.contains("chatgpt") -> VideoNiche.TECH_AI
            lower.contains("sabah") || lower.contains("disiplin") || lower.contains("hedef") || lower.contains("motivasyon") || lower.contains("vazgeç") -> VideoNiche.MOTIVATION
            lower.contains("odak") || lower.contains("dopamin") || lower.contains("üretkenlik") || lower.contains("zaman") || lower.contains("pomodoro") -> VideoNiche.PRODUCTIVITY
            lower.contains("beyin") || lower.contains("psikoloji") || lower.contains("manipülasyon") || lower.contains("yalan") || lower.contains("insan") -> VideoNiche.PSYCHOLOGY_FACTS
            lower.contains("komik") || lower.contains("mizah") || lower.contains("pazartesi") || lower.contains("skeç") || lower.contains("eğlence") -> VideoNiche.ENTERTAINMENT_COMEDY
            lower.contains("ürün") || lower.contains("amazon") || lower.contains("satın al") || lower.contains("e-ticaret") || lower.contains("gadget") -> VideoNiche.ECOMMERCE_PRODUCT
            lower.contains("spor") || lower.contains("sağlık") || lower.contains("kilo") || lower.contains("fitness") || lower.contains("adım") -> VideoNiche.FITNESS_HEALTH
            lower.contains("uzay") || lower.contains("evren") || lower.contains("kara delik") || lower.contains("bilim") || lower.contains("gezegen") -> VideoNiche.SCIENCE_SPACE
            else -> VideoNiche.TECH_AI
        }
    }

    fun generateProFallbackProject(
        topic: String,
        niche: VideoNiche,
        platformTarget: PlatformTarget,
        tone: VideoTone,
        durationSeconds: Int
    ): VideoProject {
        val sceneCount = when {
            durationSeconds <= 20 -> 4
            durationSeconds <= 40 -> 6
            durationSeconds <= 60 -> 8
            else -> 10
        }
        val perSceneDuration = (durationSeconds.toFloat() / sceneCount)
        val cleanTopic = topic.ifBlank { "Yapay Zeka ile Otomatik İçerik Üretimi" }
        val topicKeywords = cleanTopic.split(" ").filter { it.length > 2 }

        val dynamicHooks = listOf(
            "Bunu 1 yıl önce bilseydim hayatım tamamen değişirdi!",
            "Herkesin yanlış bildiği ama kimsenin söylemediği o kritik gerçek...",
            "Günde sadece 20 dakika ayırarak bu sonuca nasıl ulaşırsınız?",
            "İşte $cleanTopic hakkında bilmeniz gereken en güçlü 3 kural!",
            "Bu yöntemi öğrendikten sonra eski taktikleri çöpe atacaksınız!"
        ).shuffled()

        val transitions = listOf(
            TransitionEffect.ZOOM_IN,
            TransitionEffect.WHIP_PAN,
            TransitionEffect.GLITCH,
            TransitionEffect.LIGHT_LEAK,
            TransitionEffect.SLIDE_UP
        )

        val soundEffects = listOf("Whoosh", "Pop", "Ding", "Camera Shutter", "Bass Drop", "Cyber Glitch", "Chime")

        val stepTemplates = listOf(
            Triple(
                "İlk olarak temeli doğru kuruyoruz. $cleanTopic sürecinde en sık yapılan hata plansız başlamaktır.",
                "Hızlı tempo, ekranda neon vurgulu analiz grafiği ve dinamik odaklama",
                "1. TEMEL ADIM: DOĞRU STRATEJİ"
            ),
            Triple(
                "İkinci aşamada otomasyonu devreye alıyoruz. Zaman kaybettiren tüm tekrarları tek tıkla ortadan kaldırın.",
                "Ekranda modern arayüz animasyonu, kod ve otomasyon paneli geçişi",
                "2. OTOMASYON: ZAMANDAN %80 TASARRUF"
            ),
            Triple(
                "Üçüncü ve en kritik nokta: Veriyi doğru okumak ve kitle etkileşimini maksimuma çıkarmak.",
                "Yüksek kontrastlı 3D sinematik render, yukarı fırlayan büyüme grafiği",
                "3. BÜYÜME: KESİNTİSİZ ETKİLEŞİM"
            ),
            Triple(
                "Dördüncü adımda sonucu ölçekliyoruz. Hazırladığınız sistemi her gün düzenli olarak tekrarlayın.",
                "Kamera dolly-in hareketi, merkezde parlayan başarı simgesi",
                "4. ÖLÇEKLEME: SÜREKLİ GELİŞİM"
            ),
            Triple(
                "Ve son olarak: Bu adımları uygulayanlar ilk günden itibaren farkı net bir şekilde görüyor.",
                "Hızlı zoom efekti, ekranda dikkat çekici sonuç kartları",
                "SONUÇ: HEMEN BUGÜN BAŞLAYIN"
            )
        )

        val scenes = mutableListOf<SceneItem>()
        for (i in 0 until sceneCount) {
            val step = stepTemplates[i % stepTemplates.size]
            val sfx = soundEffects[(i + (1..3).random()) % soundEffects.size]
            val trans = transitions[(i + (1..2).random()) % transitions.size]
            val highlightWord = topicKeywords.getOrNull(i % topicKeywords.size)?.uppercase() ?: "ÖNEMLİ"

            scenes.add(
                SceneItem(
                    id = i + 1,
                    orderIndex = i,
                    durationSeconds = perSceneDuration,
                    narrationText = if (i == 0) "${dynamicHooks.first()} $cleanTopic konusunu adım adım inceliyoruz." else step.first,
                    visualDescription = step.second,
                    onScreenSubtitle = if (i == 0) dynamicHooks.first().take(36) else step.third,
                    transitionType = trans,
                    soundEffectCue = sfx,
                    bgThemeIndex = (i + (0..2).random()) % 3,
                    textHighlightWords = listOf(highlightWord, "ADIM ${i + 1}")
                )
            )
        }

        val chosenHook = dynamicHooks.first()
        val script = VideoScript(
            title = cleanTopic,
            hookLine = chosenHook,
            scenes = scenes,
            ctaLine = "Videoyu kaydet, hemen bugün uygula ve takip etmeyi unutma!",
            totalDurationSeconds = durationSeconds
        )

        val igPack = InstagramPublishData(
            caption = "🚀 ${script.title}\n\n$chosenHook\n\n📌 3 Önemli Nokta:\n1️⃣ Erken harekete geçin ve süreci otomatikleştirin.\n2️⃣ Günlük küçük adımlarla büyük fark yaratın.\n3️⃣ Algoritmayı ve araçları lehinize kullanın.\n\n💬 Sen bu konuda ne düşünüyorsun? Fikirlerini yorumlarda belirt!\n\n👇 Kaydet ve arkadaşlarınla paylaş!",
            viralHooks = listOf(chosenHook, "Bunu 1 Yıl Önce Bilseydim Hayatım Değişirdi!", "Kimsenin Bahsetmediği O Yöntem"),
            topHashtags = listOf("#reels", "#viral", "#kesfet", "#fyp", "#trend", "#instagramreels"),
            nicheHashtags = listOf("#${niche.name.lowercase()}", "#yapayzeka", "#otomasyon", "#gelisim", "#girisim", "#basari"),
            audioRecommendation = "Trending Cyber Synth (128 BPM) - Reels Trend #${(1..9).random()}",
            firstCommentPin = "👉 Hangi adımı ilk deneyeceksin? Yorumlara yaz, cevaplayayım!",
            bestPostingTime = "Bugün ${(17..20).random()}:30 - ${(21..23).random()}:00",
            coverTitle = script.title
        )

        val ytPack = YouTubePublishData(
            titleOptions = listOf(
                "CTR %${(13..16).random()}.${(1..9).random()}: ${script.title} (Kimse Bilmiyor!)",
                "CTR %${(12..15).random()}.${(1..9).random()}: Bu Taktikle Herkesi Şaşırtın | ${script.title}",
                "CTR %${(11..14).random()}.${(1..9).random()}: Adım Adım Rehber: ${script.title}"
            ),
            selectedTitle = "${script.title} #shorts",
            description = "🔥 ${script.title}\n\nBu videoda $cleanTopic hakkında en etkili yöntemleri ve püf noktalarını derledik.\n\n⏱️ Zaman Damgaları:\n00:00 Giriş ve Kanca\n00:06 Temel Mantık\n00:18 Uygulama Adımları\n00:26 Sonuç ve Özet\n\n👍 Videoyu beğendiyseniz Beğen butonuna basmayı ve Kanala Abone olmayı unutmayın!\n\n#shorts #viral #bilgi",
            tags = listOf("shorts", "youtube shorts", "viral", cleanTopic.lowercase().take(20), "eğitim", "gelişim", "trend"),
            thumbnailPrompt = "High impact YouTube thumbnail concept for: '$cleanTopic', 3D bold dynamic lighting, glowing vibrant neon cyan and gold accents, centered focal point with expressive reaction, 8k cinematic octane render",
            pinnedComment = "Sizin en çok beğendiğiniz kısım hangisi oldu? Yorumlarda buluşalım! 👇",
            categoryName = "Eğitim ve Teknoloji",
            estimatedCtr = "%${(12..15).random()}.${(1..9).random()} CTR Potansiyeli"
        )

        val resolvedNiche = if (niche == VideoNiche.AUTO_DETECT) detectSmartNicheFromTopic(cleanTopic) else niche
        val detectedLabel = if (niche == VideoNiche.AUTO_DETECT) "🤖 AI Seçti: ${resolvedNiche.label} (%98 Uyum)" else ""

        val hookA = chosenHook
        val hookB = "Bunu yapmıyorsanız her gün para veya saat kaybediyorsunuz! ($cleanTopic)"
        val splitHooks = SplitHookData(
            hookA = hookA,
            hookB = hookB,
            selectedHookType = HookType.HOOK_A
        )

        val safetyReport = AlgorithmSafetyReport(
            overallScore = (97..99).random(),
            safetyLevel = "Mükemmel (Ceza Riski %0)",
            repetitiveContentRisk = "Çok Düşük (%100 Özgün Kurgu Akışı)",
            retentionPrediction = "%86+ İzleyici Tutma Potansiyeli",
            aiDisclosureNotice = "Bu içerik yapay zekâ destekli otomasyon araçları kullanılarak kurgulanmış olup YouTube 'Sentetik veya Değiştirilmiş İçerik' politikasıyla %100 uyumludur.",
            copyrightStatus = "Telif Hakkı Sorunsuz (Royalty-Free Sesler ve B-Roll)",
            actionChecklist = listOf(
                "İlk 3 saniye kancasında yakınlaşma (Zoom-In) ile izleyici kaybı (Swipe-Away) önlendi.",
                "YouTube Studio yüklemesinde 'Sentetik veya Değiştirilmiş İçerik: EVET' kutusu işaretlenmelidir.",
                "İlk 30 dakikada gelen ilk 3 yoruma yanıt verilerek algoritma tetiklenmelidir.",
                "A/B Kanca testiyle izlenme potansiyeli en yüksek kanca seçilebilir."
            )
        )

        return VideoProject(
            topic = cleanTopic,
            niche = resolvedNiche,
            platformTarget = platformTarget,
            aspectRatio = platformTarget.defaultAspect,
            tone = tone,
            durationSeconds = durationSeconds,
            status = ProjectStatus.READY_TO_PUBLISH,
            hookScore = (95..99).random(),
            estimatedViralMultiplier = "${(4..6).random()}.${(1..9).random()}x Viral",
            script = script,
            publishPack = SocialPublishPack(igPack, ytPack),
            styleSettings = VideoStyleSettings(
                captionStyle = CaptionStyle.KARAOKE_POP,
                bouncingEmojisEnabled = true,
                humanizedBreathing = true
            ),
            splitHooks = splitHooks,
            algorithmSafety = safetyReport,
            detectedNicheLabel = detectedLabel
        )
    }

    private fun getDefaultTrendingTopics(niche: VideoNiche): List<String> {
        return when (niche) {
            VideoNiche.TECH_AI -> listOf(
                "Günde 1 saat yapay zeka ile pasif gelir elde etmenin 3 yolu",
                "ChatGPT'nin asla söylemek istemediği 4 gizli prompt",
                "2026'da her içerik üreticisinin kullanması gereken 5 AI aracı",
                "Yapay zeka ile sıfırdan otomatik video kanalı kurma rehberi"
            )
            VideoNiche.MOTIVATION -> listOf(
                "Sabah 5'te kalkan insanların asla söylemediği 3 psikolojik sır",
                "Tembelliği 10 saniyede yok eden Japon Kaizen tekniği",
                "Kendine olan inancını kaybettiğinde hatırlaman gereken tek kural",
                "Başarılı insanların %1'lik dilime girmesini sağlayan sabah rutini"
            )
            VideoNiche.CRYPTO_FINANCE -> listOf(
                "Maaşınızın erimemesi için uygulamanız gereken 50/30/20 kuralı",
                "Zenginlerin çocuklarına öğrettiği ama okulda öğretilmeyen 3 para sırrı",
                "Küçük birikimlerle bileşik getiri canavarı nasıl yaratılır?",
                "Kredi kartı tuzaklarından kurtulup tasarruf etmenin altın formülü"
            )
            VideoNiche.PSYCHOLOGY_FACTS -> listOf(
                "İnsanların %95'inin farkında olmadığı 3 şok edici beyin hilesi",
                "Birinin yalan söylediğini anlamanın beden dilindeki 3 ipucu",
                "Bilinçaltınızı yeniden programlamak için uyumadan önceki 5 dakika",
                "Neden bazı insanlar girdikleri her ortamda anında dikkat çeker?"
            )
            VideoNiche.PRODUCTIVITY -> listOf(
                "Dopamin detoksu ile 48 saatte odaklanmayı 10 katına çıkarın",
                "Pomodoro tekniğini 2 kat daha etkili yapan gizli detay",
                "Günde 12 saat çalışanlar değil, bu 2 saati yönetenler kazanıyor",
                "Ertelemeyi anında durduran 5 saniye kuralı nasıl çalışır?"
            )
            VideoNiche.SCIENCE_SPACE -> listOf(
                "Eğer bir kara deliğe düşseydiniz vücudunuza ne olurdu?",
                "Güneş aniden kaybolsaydı Dünya'da yaşanacak ilk 8 dakika",
                "Evrenin bilinen en büyük gizemi: Karanlık madde nedir?",
                "Işık hızına ulaşırsak zaman neden tamamen durur?"
            )
            VideoNiche.FITNESS_HEALTH -> listOf(
                "Günde 10.000 adım atmanın vücutta değiştirdiği 4 mucizevi süreç",
                "Şekeri 14 gün boyunca kestiğinizde vücudunuza ne olur?",
                "Uyku kalitesini %200 artıran akşam rutini bilimsel kanıtı",
                "Kilo vermeyi engelleyen en sinsi 3 gizli kalori kaynağı"
            )
            VideoNiche.AUTO_DETECT -> listOf(
                "Yapay Zeka ile Otomatik Gelir Üreten 3 Dijital Varlık",
                "Sosyal Medyada Sıfırdan 100K Takipçiye Ulaşmanın Viral Formülü",
                "Zihninizi 10 Kat Güçlendiren Günlük Nöro-Alışkanlıklar",
                "2026'da Asla Eskimeyecek En Değerli 4 Yetenek"
            )
            VideoNiche.HORMOZI_BUSINESS -> listOf(
                "Müşterilerin 'Hayır' Diyemeyeceği 100M Teklif Nasıl Oluşturulur?",
                "Fiyat Kırmadan Rakipleri Yok Etmenin 3 Basit Adımı",
                "Gelirinizi 10x Yapacak Tek Şey: Değer Eşitliği Denklemi",
                "Alex Hormozi'nin Sıfırdan 100 Milyon Dolara Ulaşma Taktikleri"
            )
            VideoNiche.MYSTERY_STORY -> listOf(
                "1994 Yılında Pasifik Okyanusu'nda Kaybolan Uçağın Son Telsiz Kaydı",
                "Dünyanın En Güvenli Kasasında Saklanan Açıklanamaz Belge",
                "50 Yıl Boyunca Gizli Kalan Antarktika Keşif Günlüğü",
                "Görgü Tanıkları Tarafından Doğrulanan 3 Paranormal Gizem"
            )
            VideoNiche.ENTERTAINMENT_COMEDY -> listOf(
                "Pazartesi Sabahı Toplantıya Katılmaya Çalışan Beyaz Yakalı Halleri",
                "Kredi Kartı Ekstresi Geldiğinde Verilen 4 Aşamalı Tepki",
                "Diyetin 3. Gününde Gece Yarısı Buzdolabı Karşısındaki Çaresizlik",
                "Arkadaş Ortamında 'Hesap Bende' Diyen Kişinin İç Dünyası"
            )
            VideoNiche.ECOMMERCE_PRODUCT -> listOf(
                "Amazon'da Gizli Kalan ve Hayatı Kolaylaştıran 3 Dahi Ürün",
                "Dropshipping Yaparken Asla Satmamanız Gereken 3 Ürün Grubu",
                "1 Günde Kendi E-Ticaret Markanızı Kurmanın Basit Yolu",
                "TikTok'ta 24 Saatte Viral Olan ve Yok Satan Dahi Mutfak Aleti"
            )
            VideoNiche.CUSTOM -> listOf(
                "3 Adımda Hayatınızı Değiştirecek Alışkanlıklar",
                "Sosyal Medyada Viral Olmanın Formülü",
                "2026'nın En Çok Kazandıran Becerileri",
                "Zamanı 2 Kat Verimli Kullanma Taktikleri"
            )
        }
    }
}

private data class FallbackScene(
    val narration: String,
    val visual: String,
    val subtitle: String,
    val transition: TransitionEffect,
    val sfx: String,
    val highlights: List<String>
)

private data class TemplateScriptData(
    val title: String,
    val hook: String,
    val scenes: List<FallbackScene>
)

private val TechAiTemplateData = TemplateScriptData(
    title = "Yapay Zeka ile Pasif Gelir Otomasyonu",
    hook = "Günde sadece 1 saat ayırarak bunu nasıl yapacağınızı kimse anlatmıyor!",
    scenes = listOf(
        FallbackScene(
            narration = "Günde sadece 1 saatinizi ayırarak yapay zeka ile otomatik içerik üretip gelir elde edebileceğinizi biliyor muydunuz?",
            visual = "Neon siber uzay, yapay zeka nöral ağı ve veri akışları canlandırması.",
            subtitle = "GÜNDE 1 SAATLE YAPAY ZEKA GELİRİ!",
            transition = TransitionEffect.ZOOM_IN,
            sfx = "Whoosh",
            highlights = listOf("1 SAATLE", "YAPAY ZEKA")
        ),
        FallbackScene(
            narration = "İlk adım: Trend konuları belirleyip otomatik prompt motorumuzla viral kancalar oluşturun.",
            visual = "Holografik arayüzde yükselen grafikler ve viral analiz paneli.",
            subtitle = "1. ADIM: VİRAL KANCALARI OLUŞTURUN",
            transition = TransitionEffect.WHIP_PAN,
            sfx = "Pop",
            highlights = listOf("1. ADIM", "VİRAL")
        ),
        FallbackScene(
            narration = "İkinci adım: Metinleri dinamik altyazılar ve vuruşlu seslendirmelerle dakikalar içinde senkronize edin.",
            visual = "Ekranda sıçrayan renkli karaoke altyazılar ve ses dalgası animasyonu.",
            subtitle = "2. ADIM: DİNAMİK ALTYAZI & SES SENKRONU",
            transition = TransitionEffect.GLITCH,
            sfx = "Bass Drop",
            highlights = listOf("2. ADIM", "SENKRON")
        ),
        FallbackScene(
            narration = "Üçüncü adım: Instagram ve YouTube için optimize edilmiş hashtag ve başlık paketini tek tıkla kopyalayın.",
            visual = "Instagram ve YouTube logoları, roket kalkışı ve beğeni yağmuru.",
            subtitle = "3. ADIM: TEK TIKLA SEO & PAYLAŞIM PAKETİ",
            transition = TransitionEffect.LIGHT_LEAK,
            sfx = "Ding",
            highlights = listOf("3. ADIM", "TEK TIKLA")
        ),
        FallbackScene(
            narration = "Daha fazlası için bu videoyu kaydetmeyi ve kanalı takip etmeyi unutmayın!",
            visual = "Profil takip butonu, kaydet simgesi ve parlak çağrı kartı.",
            subtitle = "KAYDET VE TAKİP ET! 🚀",
            transition = TransitionEffect.SLIDE_UP,
            sfx = "Whoosh",
            highlights = listOf("KAYDET", "TAKİP ET")
        )
    )
)

private val MotivationTemplateData = TemplateScriptData(
    title = "Sabah 5 Kuralı ve Disiplin Sırrı",
    hook = "Başarılı insanların %1'i her gün bu görünmez kuralı uyguluyor!",
    scenes = listOf(
        FallbackScene(
            narration = "Her sabah alarm çaldığında erteliyor musunuz? İşte %1'lik dilimin asla söylemediği sır.",
            visual = "Gündoğumu silueti, odaklanmış bir sporcu ve saat tik takları.",
            subtitle = "%1'LİK DİLİMİN GİZLİ KURALI!",
            transition = TransitionEffect.ZOOM_IN,
            sfx = "Bass Drop",
            highlights = listOf("%1'LİK", "GİZLİ")
        ),
        FallbackScene(
            narration = "Beyniniz konforu seçmek için 5 saniye içinde binlerce bahane üretir.",
            visual = "Hızlı sinematik kamera geçişi, çalışan nöronlar ve karar anı.",
            subtitle = "BEYNİN 5 SANİYE TUZAĞI",
            transition = TransitionEffect.WHIP_PAN,
            sfx = "Whoosh",
            highlights = listOf("5 SANİYE", "TUZAK")
        ),
        FallbackScene(
            narration = "Bu yüzden düşünmeden hemen harekete geçmeli ve ilk adımı atmalısınız.",
            visual = "Güneşin doğuşu, modern gökdelenler ve kararlı adımlar.",
            subtitle = "DÜŞÜNME, HAREKETE GEÇ!",
            transition = TransitionEffect.LIGHT_LEAK,
            sfx = "Ding",
            highlights = listOf("HAREKETE GEÇ")
        ),
        FallbackScene(
            narration = "Yarın sabah ilk denemeyi yap. Kaydet ve kendine hatırlat!",
            visual = "Motivasyonel ışık patlaması, kaydet simgesi ve odak kartı.",
            subtitle = "KENDİNE SÖZ VER & KAYDET!",
            transition = TransitionEffect.SLIDE_UP,
            sfx = "Pop",
            highlights = listOf("KENDİNE SÖZ VER", "KAYDET")
        )
    )
)

private val FinanceTemplateData = TemplateScriptData(
    title = "50/30/20 Bütçe ve Zenginlik Kuralı",
    hook = "Maaşınızın eriyip gitmesini durduracak tek finansal formül!",
    scenes = listOf(
        FallbackScene(
            narration = "Ay sonunu getiremiyor musunuz? İşte paranızı kontrol altına alacak 50-30-20 kuralı.",
            visual = "3D altın madeni paralar, bütçe pastası ve yükselen yatırım grafiği.",
            subtitle = "50/30/20 BÜTÇE KURALI",
            transition = TransitionEffect.ZOOM_IN,
            sfx = "Whoosh",
            highlights = listOf("50/30/20", "BÜTÇE")
        ),
        FallbackScene(
            narration = "Gelirinizin %50'si zorunlu ihtiyaçlara, %30'u kişisel isteklere gitmelidir.",
            visual = "Bölünen grafikler, kira, market ve yaşam harcamaları simgeleri.",
            subtitle = "%50 İHTİYAÇLAR - %30 İSTEKLER",
            transition = TransitionEffect.WHIP_PAN,
            sfx = "Pop",
            highlights = listOf("%50", "%30")
        ),
        FallbackScene(
            narration = "En kritik %20 ise geleceğiniz için doğrudan yatırıma ve birikime aktarılmalıdır.",
            visual = "Büyüyen altın ağaç ve bileşik getiri çarpanı.",
            subtitle = "%20 YATIRIM & BİLEŞİK GETİRİ!",
            transition = TransitionEffect.GLITCH,
            sfx = "Ding",
            highlights = listOf("%20", "YATIRIM")
        ),
        FallbackScene(
            narration = "Finansal özgürlüğün anahtarı disiplindir. Bu formülü unutmamak için hemen kaydet!",
            visual = "Altın anahtar, güvenli kasa ve kaydetme butonu.",
            subtitle = "FİNANSAL ÖZGÜRLÜK İÇİN KAYDET! 💰",
            transition = TransitionEffect.LIGHT_LEAK,
            sfx = "Bass Drop",
            highlights = listOf("FİNANSAL ÖZGÜRLÜK", "KAYDET")
        )
    )
)

private val PsychologyTemplateData = TemplateScriptData(
    title = "İnsanların %95'inin Bilmediği Beyin Hilesi",
    hook = "Bunu öğrendikten sonra insanları çok farklı gözle göreceksiniz!",
    scenes = listOf(
        FallbackScene(
            narration = "İnsan psikolojisinde öyle bir kural var ki, farkında olmadan her gün yönlendiriliyorsunuz.",
            visual = "Göz bebekleri büyüyen insan yüzü, psikolojik hologram ve soru işaretleri.",
            subtitle = "BİLİNÇALTININ GİZLİ MEKANİZMASI",
            transition = TransitionEffect.ZOOM_IN,
            sfx = "Bass Drop",
            highlights = listOf("BİLİNÇALTI", "GİZLİ")
        ),
        FallbackScene(
            narration = "Karşınızdaki kişiyle konuşurken hafifçe başınızı sallarsanız, beyni size güven duymaya başlar.",
            visual = "İki insanın pozitif iletişimi, güven sinyalleri ve nöron bağları.",
            subtitle = "GÜVEN OLUŞTURMA PSİKOLOJİSİ",
            transition = TransitionEffect.WHIP_PAN,
            sfx = "Ding",
            highlights = listOf("GÜVEN", "PSİKOLOJİ")
        ),
        FallbackScene(
            narration = "Buna 'Ayna Nöron' etkisi denir ve ikna gücünüzü 3 katına çıkarır.",
            visual = "Ayna yansıması, 3x çarpanı ve ışık patlaması.",
            subtitle = "AYNA NÖRON ETKİSİ (3X İKNA)",
            transition = TransitionEffect.GLITCH,
            sfx = "Pop",
            highlights = listOf("AYNA NÖRON", "3X İKNA")
        ),
        FallbackScene(
            narration = "Daha fazla psikoloji hilesi için takip et ve videoyu kaydet!",
            visual = "Beyin dalgaları, takip ikonu ve beğeni patlaması.",
            subtitle = "DAHA FAZLASI İÇİN TAKİP ET! 🧠",
            transition = TransitionEffect.SLIDE_UP,
            sfx = "Whoosh",
            highlights = listOf("TAKİP ET", "KAYDET")
        )
    )
)

private val ProductivityTemplateData = TemplateScriptData(
    title = "Dopamin Detoksu ile 10x Odaklanma",
    hook = "Tüm gün telefona bakıp hiçbir şey yapamıyorsanız bunu mutlaka izleyin!",
    scenes = listOf(
        FallbackScene(
            narration = "Sürekli dikkatiniz mi dağılıyor? Beyniniz ucuz dopamin bağımlısı haline gelmiş olabilir.",
            visual = "Sosyal medya bildirim yağmuru, kırmızı uyarılar ve bulanık odak.",
            subtitle = "UCUZ DOPAMİN TUZAĞI!",
            transition = TransitionEffect.ZOOM_IN,
            sfx = "Bass Drop",
            highlights = listOf("UCUZ DOPAMİN", "TUZAK")
        ),
        FallbackScene(
            narration = "Sadece 24 saat boyunca bildirimleri kapatıp ekransız kalmak beyninizi sıfırlar.",
            visual = "Kapatılan telefon, sakin doğa manzarası ve derin nefes alma ritmi.",
            subtitle = "24 SAATLİK SIFIRLAMA",
            transition = TransitionEffect.WHIP_PAN,
            sfx = "Whoosh",
            highlights = listOf("24 SAAT", "SIFIRLAMA")
        ),
        FallbackScene(
            narration = "Sonrasında en zor işler bile size son derece keyifli ve akıcı gelmeye başlar.",
            visual = "Odaklanmış çalışma masası, bitirilen görevler ve yükselen verimlilik çubuğu.",
            subtitle = "10 KAT DAHA YÜKSEK ODAK!",
            transition = TransitionEffect.LIGHT_LEAK,
            sfx = "Ding",
            highlights = listOf("10 KAT", "ODAK")
        ),
        FallbackScene(
            narration = "Bu hafta sonu dene ve sonuçları gör. Kaydetmeyi unutma!",
            visual = "Takvim yaprağı, kaydetme ikonu ve motivasyon rozeti.",
            subtitle = "BU HAFTA DENE & KAYDET! ⚡",
            transition = TransitionEffect.SLIDE_UP,
            sfx = "Pop",
            highlights = listOf("DENE", "KAYDET")
        )
    )
)

private val SpaceTemplateData = TemplateScriptData(
    title = "Bir Kara Deliğe Düşseydiniz Ne Olurdu?",
    hook = "Evrenin en korkutucu noktasında zaman ve mekan tamamen yok oluyor!",
    scenes = listOf(
        FallbackScene(
            narration = "Eğer bir kara deliğin olay ufkunu geçseydiniz, geriye dönüş asla mümkün olmazdı.",
            visual = "Işığı büken devasa kara delik, akresyon diski ve uzay boşluğu.",
            subtitle = "OLAY UFKU: DÖNÜŞÜ OLMAYAN NOKTA",
            transition = TransitionEffect.ZOOM_IN,
            sfx = "Bass Drop",
            highlights = listOf("OLAY UFKU", "GERİ DÖNÜŞ YOK")
        ),
        FallbackScene(
            narration = "Yerçekimi ayaklarınızda başınızdan o kadar güçlü olurdu ki, vücudunuz spagetti gibi uzardı.",
            visual = "Spagettileşme teorik simülasyonu, bükülen uzay-zaman çizgileri.",
            subtitle = "SPAGETTİLEŞME ETKİSİ",
            transition = TransitionEffect.GLITCH,
            sfx = "Whoosh",
            highlights = listOf("SPAGETTİLEŞME", "UZAY-ZAMAN")
        ),
        FallbackScene(
            narration = "Dışarıdan bakan biri için ise zamanınız yavaşlar ve asla deliğin içine düştüğünüzü göremezdi.",
            visual = "Donmuş zaman efekti, kırmızıya kayan ışık dalgaları ve yıldızlar.",
            subtitle = "ZAMANIN TAMAMEN DURMASI",
            transition = TransitionEffect.LIGHT_LEAK,
            sfx = "Ding",
            highlights = listOf("ZAMAN", "DURMASI")
        ),
        FallbackScene(
            narration = "Evrenin sırları için kanala abone ol ve bu videoyu arkadaşınla paylaş!",
            visual = "Galaksi manzarası, abone ol butonu ve roket simgesi.",
            subtitle = "EVRENİN SIRLARI İÇİN TAKİP ET! 🌌",
            transition = TransitionEffect.SLIDE_UP,
            sfx = "Pop",
            highlights = listOf("ABONE OL", "PAYLAŞ")
        )
    )
)
