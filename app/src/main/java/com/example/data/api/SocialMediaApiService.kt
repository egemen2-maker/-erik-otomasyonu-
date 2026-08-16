package com.example.data.api

import com.example.model.CommentItem
import com.example.model.CommentSentiment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class SocialApiConfig(
    val youtubeApiKey: String = "",
    val youtubeVideoIdOrUrl: String = "",
    val youtubeOAuthToken: String = "",
    val instagramAccessToken: String = "",
    val instagramMediaIdOrUrl: String = "",
    val isYouTubeConnected: Boolean = false,
    val isInstagramConnected: Boolean = false
)

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T, val message: String = "") : ApiResult<T>()
    data class Error(val errorMessage: String) : ApiResult<Nothing>()
}

class SocialMediaApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Extracts video ID from YouTube URL or returns clean ID
     */
    fun extractYouTubeVideoId(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.contains("youtu.be/") -> trimmed.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
            trimmed.contains("watch?v=") -> trimmed.substringAfter("watch?v=").substringBefore("&")
            trimmed.contains("shorts/") -> trimmed.substringAfter("shorts/").substringBefore("?").substringBefore("&")
            else -> trimmed
        }
    }

    /**
     * Extracts Instagram Media ID from URL or returns clean ID
     */
    fun extractInstagramMediaId(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.contains("instagram.com/reel/") -> trimmed.substringAfter("reel/").substringBefore("/").substringBefore("?")
            trimmed.contains("instagram.com/p/") -> trimmed.substringAfter("p/").substringBefore("/").substringBefore("?")
            else -> trimmed
        }
    }

    /**
     * Fetches real live comments from YouTube Data API v3
     */
    suspend fun fetchLiveYouTubeComments(
        apiKey: String,
        videoInput: String
    ): ApiResult<List<CommentItem>> = withContext(Dispatchers.IO) {
        val videoId = extractYouTubeVideoId(videoInput)
        if (videoId.isBlank()) {
            return@withContext ApiResult.Error("Lütfen geçerli bir YouTube Video ID veya Video/Shorts linki girin.")
        }
        if (apiKey.isBlank()) {
            return@withContext ApiResult.Error("YouTube Data API v3 anahtarı eksik. Lütfen Google Cloud API anahtarınızı girin.")
        }

        try {
            val url = "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&videoId=$videoId&maxResults=30&order=relevance&key=$apiKey"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val root = JSONObject(responseBody)
                    root.optJSONObject("error")?.optString("message", "YouTube API hatası (${response.code})")
                        ?: "HTTP ${response.code}: $responseBody"
                } catch (_: Exception) {
                    "YouTube API Hatası (${response.code})"
                }
                return@withContext ApiResult.Error(errorMsg)
            }

            val root = JSONObject(responseBody)
            val itemsArray = root.optJSONArray("items") ?: return@withContext ApiResult.Success(emptyList(), "Bu videoda henüz yorum yok.")

            val comments = mutableListOf<CommentItem>()
            for (i in 0 until itemsArray.length()) {
                val item = itemsArray.getJSONObject(i)
                val topLevelSnippet = item.optJSONObject("snippet")?.optJSONObject("topLevelComment")?.optJSONObject("snippet")
                if (topLevelSnippet != null) {
                    val authorName = topLevelSnippet.optString("authorDisplayName", "İzleyici")
                    val authorHandle = "@" + authorName.lowercase().replace(" ", "").filter { it.isLetterOrDigit() }
                    val textOriginal = topLevelSnippet.optString("textOriginal", "")
                    val likeCount = topLevelSnippet.optInt("likeCount", 0)
                    val publishedAt = topLevelSnippet.optString("publishedAt", "Bugün")

                    val isQuestion = textOriginal.contains("?") || textOriginal.contains("nasıl", ignoreCase = true) || textOriginal.contains("nerede", ignoreCase = true)
                    val isLinkReq = textOriginal.contains("link", ignoreCase = true) || textOriginal.contains("prompt", ignoreCase = true) || textOriginal.contains("kod", ignoreCase = true)

                    val sentiment = when {
                        isLinkReq -> CommentSentiment.PURCHASE_LINK
                        isQuestion -> CommentSentiment.QUESTION
                        textOriginal.contains("teşekkür", ignoreCase = true) || textOriginal.contains("harika", ignoreCase = true) -> CommentSentiment.POSITIVE
                        else -> CommentSentiment.QUESTION
                    }

                    comments.add(
                        CommentItem(
                            id = item.optString("id", UUID.randomUUID().toString()),
                            authorName = authorName,
                            authorHandle = authorHandle,
                            platform = "YouTube",
                            commentText = textOriginal,
                            videoTitle = "YouTube Canlı Video",
                            frequencyCount = (likeCount * 4 + 12).coerceAtLeast(1),
                            frequencyPercentage = (15..45).random(),
                            sentiment = sentiment,
                            category = if (isLinkReq) "Link & Araç Talebi" else if (isQuestion) "Kullanıcı Sorusu" else "Genel Yorum",
                            aiSuggestedReply = "Teşekkürler $authorName! Detayları ve kaynakları açıklamaya ekledim, abone olmayı unutma! 🚀",
                            userCustomReply = "",
                            isReplied = false,
                            repliedWithAi = false,
                            timestamp = publishedAt.take(10),
                            likesCount = likeCount
                        )
                    )
                }
            }

            ApiResult.Success(comments, "${comments.size} adet gerçek YouTube izleyici yorumu başarıyla çekildi.")
        } catch (e: Exception) {
            ApiResult.Error("Bağlantı hatası: ${e.localizedMessage ?: "Bilinmeyen hata"}")
        }
    }

    /**
     * Posts a real reply to a YouTube comment using OAuth Token
     */
    suspend fun postLiveYouTubeReply(
        oAuthToken: String,
        parentId: String,
        replyText: String
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        if (oAuthToken.isBlank()) {
            return@withContext ApiResult.Error("YouTube'a doğrudan yorum göndermek için Google OAuth erişim yetkisi gereklidir.")
        }

        try {
            val url = "https://www.googleapis.com/youtube/v3/comments?part=snippet"
            val bodyJson = JSONObject().apply {
                put("snippet", JSONObject().apply {
                    put("parentId", parentId)
                    put("textOriginal", replyText)
                })
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $oAuthToken")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                ApiResult.Success(replyText, "Yanıtınız YouTube'a canlı olarak başarıyla yayınlandı! ✅")
            } else {
                val errorBody = response.body?.string() ?: ""
                ApiResult.Error("YouTube yanıt gönderilemedi (${response.code}): $errorBody")
            }
        } catch (e: Exception) {
            ApiResult.Error("Yanıt gönderme hatası: ${e.localizedMessage}")
        }
    }

    /**
     * Fetches real live comments from Instagram Meta Graph API
     */
    suspend fun fetchLiveInstagramComments(
        accessToken: String,
        mediaInput: String
    ): ApiResult<List<CommentItem>> = withContext(Dispatchers.IO) {
        val mediaId = extractInstagramMediaId(mediaInput)
        if (mediaId.isBlank()) {
            return@withContext ApiResult.Error("Lütfen geçerli bir Instagram Reels / Post Media ID veya Linki girin.")
        }
        if (accessToken.isBlank()) {
            return@withContext ApiResult.Error("Instagram Meta Graph API Access Token eksik. Lütfen developers.facebook.com erişim belirtecinizi girin.")
        }

        try {
            val url = "https://graph.facebook.com/v19.0/$mediaId/comments?fields=id,text,username,timestamp,like_count&access_token=$accessToken"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val root = JSONObject(responseBody)
                    root.optJSONObject("error")?.optString("message", "Instagram API Hatası (${response.code})")
                        ?: "HTTP ${response.code}: $responseBody"
                } catch (_: Exception) {
                    "Instagram API Hatası (${response.code})"
                }
                return@withContext ApiResult.Error(errorMsg)
            }

            val root = JSONObject(responseBody)
            val dataArray = root.optJSONArray("data") ?: return@withContext ApiResult.Success(emptyList(), "Bu gönderide henüz yorum yok.")

            val comments = mutableListOf<CommentItem>()
            for (i in 0 until dataArray.length()) {
                val obj = dataArray.getJSONObject(i)
                val author = obj.optString("username", "instagram_user")
                val text = obj.optString("text", "")
                val likeCount = obj.optInt("like_count", 0)
                val id = obj.optString("id", UUID.randomUUID().toString())

                val isLink = text.contains("link", ignoreCase = true) || text.contains("dm", ignoreCase = true) || text.contains("fiyat", ignoreCase = true)
                val sentiment = if (isLink) CommentSentiment.PURCHASE_LINK else CommentSentiment.QUESTION

                comments.add(
                    CommentItem(
                        id = id,
                        authorName = author,
                        authorHandle = "@$author",
                        platform = "Instagram",
                        commentText = text,
                        videoTitle = "Instagram Reels Gönderisi",
                        frequencyCount = (likeCount * 5 + 18).coerceAtLeast(1),
                        frequencyPercentage = (20..50).random(),
                        sentiment = sentiment,
                        category = if (isLink) "DM & Link İsteme" else "Reels Etkileşimi",
                        aiSuggestedReply = "Selam @$author! 🚀 İlgili linki ve detayları bio'ya ekledim, profilden ulaşabilirsin!",
                        userCustomReply = "",
                        isReplied = false,
                        repliedWithAi = false,
                        timestamp = "Son 24 saat",
                        likesCount = likeCount
                    )
                )
            }

            ApiResult.Success(comments, "${comments.size} adet gerçek Instagram Reels yorumu başarıyla çekildi.")
        } catch (e: Exception) {
            ApiResult.Error("Instagram bağlantı hatası: ${e.localizedMessage ?: "Bilinmeyen hata"}")
        }
    }

    /**
     * Posts a real reply to an Instagram comment using Meta Graph API
     */
    suspend fun postLiveInstagramReply(
        accessToken: String,
        commentId: String,
        replyMessage: String
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank() || commentId.isBlank()) {
            return@withContext ApiResult.Error("Erişim belirteci veya yorum ID eksik.")
        }

        try {
            val url = "https://graph.facebook.com/v19.0/$commentId/replies?message=${java.net.URLEncoder.encode(replyMessage, "UTF-8")}&access_token=$accessToken"
            val emptyBody = "".toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(emptyBody).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                ApiResult.Success(replyMessage, "Yanıtınız Instagram'a canlı olarak yayınlandı! ✅")
            } else {
                val errorBody = response.body?.string() ?: ""
                ApiResult.Error("Instagram yanıt gönderilemedi: $errorBody")
            }
        } catch (e: Exception) {
            ApiResult.Error("Instagram yanıtlama hatası: ${e.localizedMessage}")
        }
    }
}
