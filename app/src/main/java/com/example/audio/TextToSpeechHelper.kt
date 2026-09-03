package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TextToSpeechHelper(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onSpeechDoneCallback: (() -> Unit)? = null
    private var onSpeechStartCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                val trLocale = Locale("tr", "TR")
                val result = tts?.setLanguage(trLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.ENGLISH
                }
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.05f)
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onSpeechStartCallback?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                onSpeechDoneCallback?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onSpeechDoneCallback?.invoke()
            }
        })
    }

    fun speak(
        text: String,
        speechRate: Float = 1.05f,
        humanizedBreathing: Boolean = true,
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null
    ) {
        if (!isInitialized || text.isBlank()) {
            onDone?.invoke()
            return
        }
        onSpeechStartCallback = onStart
        onSpeechDoneCallback = onDone

        val processedText = if (humanizedBreathing) {
            humanizeSpeechText(text)
        } else {
            text
        }

        // Slight tonal warmth for humanized feel
        tts?.setPitch(if (humanizedBreathing) 1.02f else 1.0f)
        tts?.setSpeechRate(speechRate)
        tts?.speak(processedText, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_${System.currentTimeMillis()}")
    }

    private fun humanizeSpeechText(raw: String): String {
        return raw
            .replace("!", "! ... ")
            .replace("?", "? ... ")
            .replace(":", ": ... ")
            .replace(" - ", " ... ")
            .replace(";", "; ... ")
            .replace("1.", "Birinci, ... ")
            .replace("2.", "İkinci, ... ")
            .replace("3.", "Üçüncü, ... ")
            .replace("4.", "Dördüncü, ... ")
            .replace("5.", "Beşinci, ... ")
            .trim()
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
