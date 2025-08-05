// Enhanced ChatService.kt - Professional System-wide Assistant
package com.example.assistant

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatService(private val context: Context) {
    private val gemmaService = GemmaService(context)
    private var isGemmaReady = false
    private var initializationAttempts = 0
    private val conversationHistory = mutableListOf<String>()

    var onProgressUpdate: ((progress: Int, status: String) -> Unit)? = null

    companion object {
        private const val TAG = "ChatService"
        private const val MAX_INIT_ATTEMPTS = 3
        private const val MAX_HISTORY_SIZE = 6 // Keep last 3 exchanges
    }

    suspend fun initialize(): Boolean {
        return try {
            initializationAttempts++
            Log.d(TAG, "🤖 Initializing Gemma System Assistant (attempt $initializationAttempts)")

            gemmaService.onProgressUpdate = { progress, status ->
                Log.d(TAG, "Setup progress: $progress% - $status")
            }

            isGemmaReady = gemmaService.initialize()

            if (isGemmaReady) {
                Log.d(TAG, "✅ System Assistant ready!")
                Log.d(TAG, gemmaService.getMemoryUsage())
            } else {
                Log.e(TAG, "❌ Assistant initialization failed: ${gemmaService.getInitializationError()}")
            }

            isGemmaReady
        } catch (e: Exception) {
            Log.e(TAG, "System Assistant initialization error", e)
            false
        }
    }

    suspend fun sendMessage(message: String): String = withContext(Dispatchers.Default) {
        return@withContext try {
            Log.d(TAG, "🧠 Processing intelligent request: ${message.take(50)}...")

            if (!isGemmaReady) {
                return@withContext getIntelligentFallbackResponse(message)
            }

            // Add to conversation history
            addToHistory("User: $message")

            // Generate contextual response
            val response = gemmaService.generateIntelligentResponse(message, conversationHistory)

            // Add response to history
            addToHistory("Assistant: $response")

            response

        } catch (e: Exception) {
            Log.e(TAG, "Intelligent processing error", e)
            "I encountered an issue processing your request. Please try again or restart the assistant."
        }
    }

    private fun addToHistory(entry: String) {
        conversationHistory.add(entry)
        if (conversationHistory.size > MAX_HISTORY_SIZE) {
            conversationHistory.removeAt(0)
        }
    }

    private fun getIntelligentFallbackResponse(message: String): String {
        val lowerMessage = message.lowercase()

        return when {
            lowerMessage.contains("hello") || lowerMessage.contains("hi") ->
                "Hello! I'm your system-wide AI assistant. I'm starting up and will be ready shortly."

            lowerMessage.contains("help") ->
                "I'm here to help with questions, tasks, and information. Currently initializing my AI capabilities."

            lowerMessage.contains("code") || lowerMessage.contains("program") ->
                "I can help with coding and programming once I'm fully loaded. Please wait a moment."

            lowerMessage.contains("write") || lowerMessage.contains("create") ->
                "I'll be able to help you write and create content as soon as I'm ready."

            lowerMessage.length > 100 ->
                "I see you have a detailed request. Please give me a moment to initialize so I can provide a comprehensive response."

            else ->
                "I'm getting ready to assist you. ${getStatusMessage()}"
        }
    }

    private fun getStatusMessage(): String {
        return when {
            initializationAttempts >= MAX_INIT_ATTEMPTS ->
                "Please restart the app to retry initialization."
            gemmaService.getInitializationError() != null ->
                "Initialization issue: ${gemmaService.getInitializationError()?.take(80) ?: "Unknown error"}"
            else ->
                "Please try again in a moment."
        }
    }

    fun isReady(): Boolean = isGemmaReady

    fun getStatus(): String {
        return when {
            isGemmaReady -> "System Assistant Ready • ${gemmaService.getMemoryUsage()}"
            gemmaService.getInitializationError() != null ->
                "Error: ${gemmaService.getInitializationError()}"
            else -> "Loading AI Model... (attempt $initializationAttempts/$MAX_INIT_ATTEMPTS)"
        }
    }

    fun canRetry(): Boolean = initializationAttempts < MAX_INIT_ATTEMPTS

    fun clearHistory() {
        conversationHistory.clear()
    }

    fun shutdown() {
        gemmaService.shutdown()
        isGemmaReady = false
        conversationHistory.clear()
    }
}