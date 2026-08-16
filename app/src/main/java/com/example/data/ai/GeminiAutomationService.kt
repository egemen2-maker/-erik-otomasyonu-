package com.example.data.ai

import com.example.BuildConfig
import com.example.model.CaptionStyle
import com.example.model.CommentItem
import com.example.model.CommentSentiment
import com.example.model.InstagramPublishData
import com.example.model.PlatformTarget
import com.example.model.ProjectStatus
import com.example.model.ReplyTone
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
        onStageUpdate: (String) -> Unit = {}
    ): VideoProject = withContext(Dispatchers.IO) {
        onStageUpdate("Kanca (Hook) ve Viral Açı Oluşturuluyor...")
        val apiKey = BuildConfig.GEMINI_API_KEY

        val systemPrompt = """
            Sen Instagram Reels, YouTube Shorts ve TikTok için viral videolar üreten dünya çapında bir Sosyal Medya Otomasyon Uzmanı ve Profesyonel Video Kurgucususun.
            
            Kullanıcının verdiği konu için eksiksiz bir video kurgusu, sahne zamanlaması, seslendirme metni, görsel b-roll açıklamaları, Instagram Reels paketi ve YouTube Shorts SEO paketi üret.
            
            Yanıtı SADECE ve SADECE geçerli bir JSON nesnesi olarak döndür. Markdown code block veya ekstra açıklama ekleme.
            
            JSON Şeması:
            {
              "title": "Video Başlığı",
              "hookLine": "İlk 3 saniye vurucu kanca cümlesi",
              "ctaLine": "Videonun sonundaki takip/kaydet çağrısı",
              "hookScore": 95,
              "estimatedViralMultiplier": "4.5x Viral",
              "scenes": [
                {
                  "orderIndex": 0,
                  "durationSeconds": 4.5,
                  "narrationText": "Seslendirme metni",
                  "visualDescription": "Görsel veya kamera açısı açıklaması",
                  "onScreenSubtitle": "Ekranda belirecek altyazı",
                  "transitionType": "ZOOM_IN" veya "WHIP_PAN" veya "GLITCH" veya "LIGHT_LEAK" veya "SLIDE_UP",
                  "soundEffectCue": "Whoosh" veya "Pop" veya "Ding" veya "Camera Shutter" veya "Bass Drop",
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
            Kategori: ${niche.label}
            Hedef Platform: ${platformTarget.title}
            Video Tonu: ${tone.label} (${tone.desc})
            Hedef Süre: $durationSeconds saniye
            
            Lütfen $durationSeconds saniyeyi dolduracak şekilde ortalama ${durationSeconds / 4} veya ${durationSeconds / 5} sahneli profesyonel video akışını, seslendirmesini, Instagram ve YouTube yükleme paketini JSON formatında üret.
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

    suspend fun suggestTrendingTopics(niche: VideoNiche): List<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
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
        niche: VideoNiche
    ): List<CommentItem> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank()) {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val prompt = """
                    Sen sosyal medya analitiği ve kitle etkileşimi uzmanısın.
                    Şu video konusu/başlığı için Instagram, YouTube ve TikTok'ta EN ÇOK GELEN (en sık sorulan, viral tekrarlanan) 6 adet gerçekçi izleyici yorumu ve her birine verilebilecek zekice, kitleyi bağlayan bir AI yanıtı oluştur:
                    Video Başlığı: "$videoTitle" (Kategori: ${niche.label})
                    
                    Döndürülecek JSON Şeması (SADECE GEÇERLİ JSON DİZİSİ DÖNDÜR, markdown blokları olmadan):
                    [
                      {
                        "authorName": "Kullanıcı Adı",
                        "authorHandle": "@kullaniciadi",
                        "platform": "Instagram" (veya "YouTube" veya "TikTok"),
                        "commentText": "İzleyicinin yazdığı soru veya yorum",
                        "frequencyCount": 142 (Benzer yorum sayısı, örn: 80 - 450 arası),
                        "frequencyPercentage": 38 (Yorumların yüzde kaçı bunu sordu, örn: 15 - 45),
                        "sentiment": "QUESTION" (veya "PURCHASE_LINK" veya "POSITIVE" veya "FEEDBACK"),
                        "category": "Prompt İsteme" (veya "Nasıl Yapılır", "Araç İsmi", "Tavsiye"),
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
        tone: ReplyTone = ReplyTone.FRIENDLY
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank()) {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
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
        return when (niche) {
            VideoNiche.TECH_AI -> listOf(
                CommentItem(
                    id = "c1",
                    authorName = "Burak Yılmaz",
                    authorHandle = "@burak.tech",
                    platform = "Instagram",
                    commentText = "Kullandığın yapay zeka aracının adı ve prompt listesi nedir? DM atar mısın?",
                    videoTitle = videoTitle,
                    frequencyCount = 384,
                    frequencyPercentage = 44,
                    sentiment = CommentSentiment.PURCHASE_LINK,
                    category = "Prompt & Araç İsmi",
                    aiSuggestedReply = "Selam Burak! 🚀 Videoda kullandığım tüm promptları ve araç linkini profilimdeki 'AI Araç Seti' bağlantısına ekledim, ücretsiz alabilirsin!",
                    timestamp = "5 dk önce",
                    likesCount = 142
                ),
                CommentItem(
                    id = "c2",
                    authorName = "Selin Demir",
                    authorHandle = "@selin_digital",
                    platform = "YouTube",
                    commentText = "Günde 1 saat gerçekten yetiyor mu? Başlangıç seviyesi için hangi adımla başlamalıyız?",
                    videoTitle = videoTitle,
                    frequencyCount = 215,
                    frequencyPercentage = 26,
                    sentiment = CommentSentiment.QUESTION,
                    category = "Uygulama & Başlangıç",
                    aiSuggestedReply = "Harika soru Selin! 💡 İlk hafta günde sadece 30 dk ile 1 numaralı otomasyon şablonunu kurman fazlasıyla yeterli. Part 2 videosunda adım adım gösteriyorum!",
                    timestamp = "18 dk önce",
                    likesCount = 89
                ),
                CommentItem(
                    id = "c3",
                    authorName = "Mert Can",
                    authorHandle = "@mertc_ai",
                    platform = "TikTok",
                    commentText = "Bunu mobil telefondan yapabilir miyiz yoksa bilgisayar şart mı?",
                    videoTitle = videoTitle,
                    frequencyCount = 176,
                    frequencyPercentage = 19,
                    sentiment = CommentSentiment.QUESTION,
                    category = "Mobil Uyumluluk",
                    aiSuggestedReply = "Kesinlikle! %100 mobil uyumlu, telefonundaki tarayıcı veya AutoReel üzerinden tek tıkla yürütebilirsin 📱",
                    timestamp = "32 dk önce",
                    likesCount = 67
                ),
                CommentItem(
                    id = "c4",
                    authorName = "Gizem Kaya",
                    authorHandle = "@gizemkaya",
                    platform = "Instagram",
                    commentText = "Bu sayfa harika içerikler üretiyor, sayende ilk projemi başlattım teşekkürler! ❤️",
                    videoTitle = videoTitle,
                    frequencyCount = 98,
                    frequencyPercentage = 11,
                    sentiment = CommentSentiment.POSITIVE,
                    category = "Başarı & Teşekkür",
                    aiSuggestedReply = "Bunu duymak inanılmaz motive edici Gizem! 👏 İlk sonuçlarını bana DM'den gönder mutlaka inceleyeyim, başarılar!",
                    timestamp = "1 saat önce",
                    likesCount = 45
                ),
                CommentItem(
                    id = "c5",
                    authorName = "Emre Kara",
                    authorHandle = "@emre_kara99",
                    platform = "YouTube",
                    commentText = "Seslendirmeyi hangi yapay zeka ile yaptın? Çok doğal duruyor.",
                    videoTitle = videoTitle,
                    frequencyCount = 84,
                    frequencyPercentage = 9,
                    sentiment = CommentSentiment.QUESTION,
                    category = "Ses & Dublaj",
                    aiSuggestedReply = "AutoReel içindeki entegre doğal TTS motorunu kullandım! Tonlama ve hızı doğrudan video kurgu sekmesinden ayarlayabiliyorsun 🎙️",
                    timestamp = "2 saat önce",
                    likesCount = 31
                )
            )
            else -> listOf(
                CommentItem(
                    id = "c10",
                    authorName = "Ahmet Y.",
                    authorHandle = "@ahmetyildiz",
                    platform = "Instagram",
                    commentText = "Part 2 ne zaman gelecek? Kaydettim bekliyorum!",
                    videoTitle = videoTitle,
                    frequencyCount = 290,
                    frequencyPercentage = 41,
                    sentiment = CommentSentiment.QUESTION,
                    category = "Devam Videosu",
                    aiSuggestedReply = "Part 2 yarın saat 18:00'de yayında olacak! Bildirimleri açmayı unutma 🔔",
                    timestamp = "12 dk önce",
                    likesCount = 118
                ),
                CommentItem(
                    id = "c11",
                    authorName = "Zeynep B.",
                    authorHandle = "@zeynep_b",
                    platform = "YouTube",
                    commentText = "Bahsettiğin kaynakların PDF listesi var mı acaba?",
                    videoTitle = videoTitle,
                    frequencyCount = 194,
                    frequencyPercentage = 27,
                    sentiment = CommentSentiment.PURCHASE_LINK,
                    category = "Kaynak & Link",
                    aiSuggestedReply = "Evet! Açıklamadaki ücretsiz indirme bağlantısından tüm PDF dökümanına erişebilirsin 📄",
                    timestamp = "25 dk önce",
                    likesCount = 76
                ),
                CommentItem(
                    id = "c12",
                    authorName = "Caner K.",
                    authorHandle = "@caner_k",
                    platform = "TikTok",
                    commentText = "Çok net ve akıcı anlatım olmuş, emeğine sağlık 👏",
                    videoTitle = videoTitle,
                    frequencyCount = 130,
                    frequencyPercentage = 18,
                    sentiment = CommentSentiment.POSITIVE,
                    category = "Övgü",
                    aiSuggestedReply = "Çok teşekkürler Caner! Beğendiğine çok sevindim, yeni içerikler yolda 🚀",
                    timestamp = "45 dk önce",
                    likesCount = 52
                )
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

            VideoProject(
                topic = topic,
                niche = niche,
                platformTarget = platformTarget,
                aspectRatio = platformTarget.defaultAspect,
                tone = tone,
                durationSeconds = durationSeconds,
                status = ProjectStatus.READY_TO_PUBLISH,
                hookScore = hookScore,
                estimatedViralMultiplier = viralMultiplier,
                script = script,
                publishPack = SocialPublishPack(igPack, ytPack),
                styleSettings = VideoStyleSettings()
            )
        } catch (_: Exception) {
            generateProFallbackProject(topic, niche, platformTarget, tone, durationSeconds)
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

        val sampleScriptData = when (niche) {
            VideoNiche.TECH_AI -> TechAiTemplateData
            VideoNiche.MOTIVATION -> MotivationTemplateData
            VideoNiche.CRYPTO_FINANCE -> FinanceTemplateData
            VideoNiche.PSYCHOLOGY_FACTS -> PsychologyTemplateData
            VideoNiche.PRODUCTIVITY -> ProductivityTemplateData
            VideoNiche.SCIENCE_SPACE -> SpaceTemplateData
            else -> TechAiTemplateData
        }

        val scenes = mutableListOf<SceneItem>()
        for (i in 0 until sceneCount) {
            val templateScene = sampleScriptData.scenes.getOrElse(i) {
                FallbackScene(
                    narration = "$topic konusunda kritik adım ${i + 1}.",
                    visual = "Dinamik kamera hareketi, $topic üzerine odaklanan sinematik sahne.",
                    subtitle = "ADIM ${i + 1}: ${topic.take(25)}",
                    transition = TransitionEffect.ZOOM_IN,
                    sfx = "Whoosh",
                    highlights = listOf("ADIM", "KRİTİK")
                )
            }

            scenes.add(
                SceneItem(
                    id = i + 1,
                    orderIndex = i,
                    durationSeconds = perSceneDuration,
                    narrationText = templateScene.narration,
                    visualDescription = templateScene.visual,
                    onScreenSubtitle = templateScene.subtitle,
                    transitionType = templateScene.transition,
                    soundEffectCue = templateScene.sfx,
                    bgThemeIndex = i % 3,
                    textHighlightWords = templateScene.highlights
                )
            )
        }

        val script = VideoScript(
            title = topic.ifBlank { sampleScriptData.title },
            hookLine = sampleScriptData.hook,
            scenes = scenes,
            ctaLine = "Videoyu kaydet, hemen bugün uygula ve takip etmeyi unutma!",
            totalDurationSeconds = durationSeconds
        )

        val igPack = InstagramPublishData(
            caption = "🚀 ${script.title}\n\n${script.hookLine}\n\n📌 3 Önemli Nokta:\n1️⃣ Erken harekete geçin ve süreci otomatikleştirin.\n2️⃣ Günlük küçük adımlarla büyük fark yaratın.\n3️⃣ Algoritmayı ve araçları lehinize kullanın.\n\n💬 Sen bu konuda ne düşünüyorsun? Fikirlerini yorumlarda belirt!\n\n👇 Kaydet ve arkadaşlarınla paylaş!",
            viralHooks = listOf(script.hookLine, "Bunu 1 Yıl Önce Bilseydim Hayatım Değişirdi!", "Kimsenin Bahsetmediği O Yöntem"),
            topHashtags = listOf("#reels", "#viral", "#kesfet", "#fyp", "#trend", "#instagramreels"),
            nicheHashtags = listOf("#${niche.name.lowercase()}", "#yapayzeka", "#otomasyon", "#gelisim", "#girisim", "#basari"),
            audioRecommendation = "Trending Cyber Synth (128 BPM) - Reels Trend #4",
            firstCommentPin = "👉 Hangi adımı ilk deneyeceksin? Yorumlara yaz, cevaplayayım!",
            bestPostingTime = "Bugün 18:30 - 21:00",
            coverTitle = script.title
        )

        val ytPack = YouTubePublishData(
            titleOptions = listOf(
                "CTR %14.8: ${script.title} (Kimse Bilmiyor!)",
                "CTR %13.2: Bu Taktikle Herkesi Şaşırtın | ${script.title}",
                "CTR %11.9: Adım Adım Rehber: ${script.title}"
            ),
            selectedTitle = "${script.title} #shorts",
            description = "🔥 ${script.title}\n\nBu videoda ${topic} hakkında en etkili yöntemleri ve püf noktalarını derledik.\n\n⏱️ Zaman Damgaları:\n00:00 Giriş ve Kanca\n00:06 Temel Mantık\n00:18 Uygulama Adımları\n00:26 Sonuç ve Özet\n\n👍 Videoyu beğendiyseniz Beğen butonuna basmayı ve Kanala Abone olmayı unutmayın!\n\n#shorts #viral #bilgi",
            tags = listOf("shorts", "youtube shorts", "viral", topic.lowercase(), "eğitim", "gelişim", "trend"),
            thumbnailPrompt = "High impact YouTube thumbnail concept for: '$topic', 3D bold dynamic lighting, glowing vibrant neon cyan and gold accents, centered focal point with expressive reaction, 8k cinematic octane render",
            pinnedComment = "Sizin en çok beğendiğiniz kısım hangisi oldu? Yorumlarda buluşalım! 👇",
            categoryName = "Eğitim ve Teknoloji",
            estimatedCtr = "%13.4 CTR Potansiyeli"
        )

        return VideoProject(
            topic = topic.ifBlank { "Viral Otomasyon Videosu" },
            niche = niche,
            platformTarget = platformTarget,
            aspectRatio = platformTarget.defaultAspect,
            tone = tone,
            durationSeconds = durationSeconds,
            status = ProjectStatus.READY_TO_PUBLISH,
            hookScore = 96,
            estimatedViralMultiplier = "4.6x Viral",
            script = script,
            publishPack = SocialPublishPack(igPack, ytPack),
            styleSettings = VideoStyleSettings(captionStyle = CaptionStyle.KARAOKE_POP)
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
