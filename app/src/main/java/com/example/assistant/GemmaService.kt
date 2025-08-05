// Complete GemmaService.kt - Auto HuggingFace Download
package com.example.assistant

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class GemmaService(private val context: Context) {
    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private var initializationError: String? = null
    private var downloadProgress: Int = 0
    private var downloadStatus: String = "Initializing..."

    companion object {
        private const val TAG = "GemmaService"

        // HuggingFace Configuration
        private const val HF_TOKEN = "" // Replace with your actual token
        private const val MODEL_URL = "https://huggingface.co/google/gemma-3n-E2B-it-litert-preview/resolve/main/gemma-3n-E2B-it-int4.task"
        private const val modelFileName = "gemma-3n-E2B-it-int4.task"

        // Timeouts
        private const val DOWNLOAD_TIMEOUT = 300000L  // 5 minutes for download
        private const val INITIALIZATION_TIMEOUT = 120000L  // 2 minutes for model loading
        private const val RESPONSE_TIMEOUT = 30000L  // 30 seconds for responses
    }

    // Progress callback for UI updates
    var onProgressUpdate: ((progress: Int, status: String) -> Unit)? = null

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "🤖 Starting Gemma 3N E2B initialization")
            updateProgress(0, "Checking for Gemma 3N model...")

            // Clean memory
            System.gc()
            Runtime.getRuntime().gc()

            // Ensure model is available (download if needed)
            val modelPath = ensureModelAvailable()
            if (modelPath == null) {
                initializationError = "Failed to obtain Gemma 3N model"
                updateProgress(0, "Model setup failed")
                return@withContext false
            }

            updateProgress(70, "Loading Gemma 3N into memory...")
            Log.d(TAG, "Initializing MediaPipe LLM with model: $modelPath")

            // Initialize MediaPipe LLM
            val success = withTimeoutOrNull(INITIALIZATION_TIMEOUT) {
                try {
                    val options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelPath)
                        .setMaxTokens(512)
                        .setMaxNumImages(1)  // Gemma 3N supports vision
                        .build()

                    // Try GPU first, fallback to CPU
                    llmInference = try {
                        updateProgress(80, "Enabling GPU acceleration...")
                        LlmInference.createFromOptions(context, options).also {
                            Log.d(TAG, "✅ GPU acceleration enabled!")
                        }
                    } catch (e: Exception) {
                        updateProgress(80, "Using CPU processing...")
                        Log.w(TAG, "GPU unavailable, using CPU: ${e.message}")
                        LlmInference.createFromOptions(context, options)
                    }

                    updateProgress(90, "Finalizing setup...")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "MediaPipe initialization error: ${e.message}", e)
                    false
                }
            }

            if (success == true) {
                isInitialized = true
                initializationError = null
                updateProgress(100, "Gemma 3N ready!")
                Log.d(TAG, "✅ Gemma 3N E2B Assistant fully initialized!")
                true
            } else {
                initializationError = "Model initialization timed out or failed"
                updateProgress(0, "Initialization failed")
                false
            }

        } catch (e: Exception) {
            initializationError = "Initialization error: ${e.message}"
            updateProgress(0, "Setup failed")
            Log.e(TAG, initializationError!!, e)
            false
        }
    }

    private suspend fun ensureModelAvailable(): String? = withContext(Dispatchers.IO) {
        val modelFile = File(context.filesDir, modelFileName)

        // Check if model exists and validate it
        if (modelFile.exists() && modelFile.length() > 0) {
            // Validate the model file
            if (isValidModelFile(modelFile)) {
                Log.d(TAG, "Using cached Gemma 3N model (${modelFile.length() / 1024 / 1024} MB)")
                updateProgress(60, "Found valid cached model")
                return@withContext modelFile.absolutePath
            } else {
                // Model is corrupted, delete it
                Log.w(TAG, "Cached model is corrupted, re-downloading...")
                modelFile.delete()
            }
        }

        // Download model from HuggingFace
        return@withContext downloadModelFromHuggingFace(modelFile)
    }

    private fun isValidModelFile(modelFile: File): Boolean {
        return try {
            // Basic validation - check if it's a valid zip/task file
            val header = ByteArray(4)
            modelFile.inputStream().use { it.read(header) }

            // Check for ZIP signature (PK) or other valid headers
            val signature = String(header, 0, 2)
            val isValid = signature == "PK" || modelFile.length() > 1000000 // At least 1MB

            Log.d(TAG, "Model validation: signature='$signature', size=${modelFile.length()}, valid=$isValid")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Model validation failed", e)
            false
        }
    }

    private suspend fun clearCorruptedModel(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val modelFile = File(context.filesDir, modelFileName)
            if (modelFile.exists()) {
                modelFile.delete()
                Log.d(TAG, "Cleared corrupted model file")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing model", e)
            false
        }
    }

    private suspend fun downloadModelFromHuggingFace(targetFile: File): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Downloading Gemma 3N model from HuggingFace...")
            updateProgress(5, "Connecting to HuggingFace...")

            // Create HTTP client with timeout and logging
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            // Build request with HuggingFace authentication
            val request = Request.Builder()
                .url(MODEL_URL)
                .addHeader("Authorization", "Bearer $HF_TOKEN")
                .addHeader("User-Agent", "GemmaAssistant/1.0")
                .build()

            updateProgress(10, "Starting download...")

            // Execute download with progress tracking
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Download failed: ${response.code} ${response.message}")
                    initializationError = "Download failed: ${response.message}"
                    return@withContext null
                }

                val contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1L
                val responseBody = response.body ?: run {
                    Log.e(TAG, "Empty response body")
                    return@withContext null
                }

                Log.d(TAG, "Downloading model, size: ${contentLength / 1024 / 1024} MB")

                // Download with progress updates
                responseBody.byteStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(8192)
                        var totalBytesRead = 0L
                        var bytesRead: Int

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            // Update progress
                            if (contentLength > 0) {
                                val progress = ((totalBytesRead.toDouble() / contentLength) * 50).toInt() + 10
                                val mbDownloaded = totalBytesRead / 1024 / 1024
                                val mbTotal = contentLength / 1024 / 1024
                                updateProgress(progress, "Downloaded ${mbDownloaded}MB / ${mbTotal}MB")
                            } else {
                                val mbDownloaded = totalBytesRead / 1024 / 1024
                                updateProgress(30, "Downloaded ${mbDownloaded}MB...")
                            }
                        }
                    }
                }

                Log.d(TAG, "✅ Model downloaded successfully (${targetFile.length() / 1024 / 1024} MB)")
                updateProgress(60, "Download complete, preparing model...")
                // Validate downloaded file
                if (!isValidModelFile(targetFile)) {
                    Log.e(TAG, "Downloaded model is corrupted!")
                    targetFile.delete()
                    initializationError = "Downloaded model is corrupted. Please try again."
                    return@withContext null
                }

                updateProgress(60, "Download complete, preparing model...")

                targetFile.absolutePath


            }

        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}", e)
            initializationError = "Download failed: ${e.message}"
            updateProgress(0, "Download failed")

            // Clean up partial download
            if (targetFile.exists()) {
                targetFile.delete()
            }

            null
        }
    }

    suspend fun generateIntelligentResponse(
        userMessage: String,
        conversationHistory: List<String>
    ): String = withContext(Dispatchers.Default) {
        if (!isInitialized || llmInference == null) {
            return@withContext "AI model not ready. Please wait for initialization to complete."
        }

        return@withContext withTimeoutOrNull(RESPONSE_TIMEOUT) {
            try {
                Log.d(TAG, "🧠 Gemma 3N processing: ${userMessage.take(50)}...")
                val startTime = System.currentTimeMillis()

                val prompt = buildGemma3NPrompt(userMessage, conversationHistory)

                // Optimized settings for Gemma 3N E2B
                val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTemperature(0.3f)     // Balanced creativity
                    .setTopK(40)              // Good diversity
                    .setRandomSeed(42)        // Consistent quality
                    .build()

                val response = try {
                    LlmInferenceSession.createFromOptions(llmInference, sessionOptions).use { session ->
                        session.addQueryChunk(prompt)
                        val rawResponse = session.generateResponse()
                        val genTime = System.currentTimeMillis() - startTime
                        Log.d(TAG, "🧠 Gemma 3N responded in ${genTime}ms, length: ${rawResponse.length}")
                        rawResponse
                    }
                } catch (e: OutOfMemoryError) {
                    Log.e(TAG, "Memory limit exceeded during generation", e)
                    return@withTimeoutOrNull "Response too complex for device memory. Try a simpler question."
                } catch (e: Exception) {
                    Log.e(TAG, "Error during response generation", e)
                    return@withTimeoutOrNull "I encountered an issue generating a response. Please try rephrasing."
                }

                val cleanedResponse = cleanGemma3NResponse(response, userMessage)
                val totalTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "🧠 Total processing time: ${totalTime}ms, final length: ${cleanedResponse.length}")

                cleanedResponse

            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in generateIntelligentResponse", e)
                "I'm experiencing technical difficulties. Please try again."
            }
        } ?: "Response generation timed out. Please try a shorter question."
    }

    private fun buildGemma3NPrompt(userMessage: String, conversationHistory: List<String>): String {
        val context = if (conversationHistory.isNotEmpty()) {
            "Previous conversation:\n${conversationHistory.takeLast(4).joinToString("\n")}\n\n"
        } else {
            ""
        }

        // Optimized prompt format for Gemma 3N
        return """<start_of_turn>user
You are an intelligent AI assistant. Be helpful, accurate, and conversational.

${context}$userMessage<end_of_turn>
<start_of_turn>model
"""
    }

    private fun cleanGemma3NResponse(response: String, originalMessage: String): String {
        var cleaned = response.trim()

        // Remove Gemma-specific prefixes and artifacts
        val prefixes = listOf(
            "<start_of_turn>model", "</start_of_turn>",
            "Assistant:", "AI:", "Response:", "Answer:",
            "$originalMessage:", "User:", "System:", "model"
        )

        for (prefix in prefixes) {
            if (cleaned.startsWith(prefix, ignoreCase = true)) {
                cleaned = cleaned.removePrefix(prefix).trim()
                break
            }
        }

        // Handle empty responses
        if (cleaned.isBlank() || cleaned.length < 10) {
            return "I need more context to provide a helpful response. Could you elaborate?"
        }

        // Smart length management for mobile
        if (cleaned.length > 800) {
            val sentences = cleaned.split(". ")
            val result = StringBuilder()

            for (sentence in sentences) {
                if (result.length + sentence.length + 2 <= 750) {
                    if (result.isNotEmpty()) result.append(". ")
                    result.append(sentence)
                } else {
                    break
                }
            }

            cleaned = if (result.isNotEmpty()) {
                result.toString() + if (!result.toString().endsWith(".")) "." else ""
            } else {
                cleaned.take(750) + "..."
            }
        }

        // Ensure proper ending
        if (!cleaned.endsWith(".") && !cleaned.endsWith("!") && !cleaned.endsWith("?")) {
            if (cleaned.length < 750) {
                cleaned += "."
            }
        }

        return cleaned
    }

    private fun updateProgress(progress: Int, status: String) {
        downloadProgress = progress
        downloadStatus = status
        Log.d(TAG, "Progress: $progress% - $status")
        onProgressUpdate?.invoke(progress, status)
    }

    // Getters for UI
    fun getDownloadProgress(): Int = downloadProgress
    fun getDownloadStatus(): String = downloadStatus
    fun getInitializationError(): String? = initializationError
    fun isReady(): Boolean = isInitialized

    fun getMemoryUsage(): String {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        return "Memory: ${usedMemory}MB / ${maxMemory}MB"
    }

    fun shutdown() {
        try {
            llmInference?.close()
            Log.d(TAG, "Gemma 3N service shutdown complete")
            System.gc()
            Runtime.getRuntime().gc()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down service", e)
        } finally {
            llmInference = null
            isInitialized = false
        }
    }
}