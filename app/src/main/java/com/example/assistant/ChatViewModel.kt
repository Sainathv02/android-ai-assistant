// Enhanced ChatViewModel.kt - State Management for Chat
package com.example.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.assistant.ChatMessage

class ChatViewModel(private val chatService: ChatService) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _statusMessage = MutableStateFlow("Initializing...")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        initializeChat()
    }

    private fun initializeChat() {
        viewModelScope.launch {
            _statusMessage.value = "Loading Gemma AI model..."

            val success = chatService.initialize()
            _isInitialized.value = success

            if (success) {
                _statusMessage.value = "Gemma AI ready!"
                // Add welcome message
                addMessage(ChatMessage(
                    text = "Hello! I'm powered by Gemma AI. How can I help you today?",
                    isFromUser = false
                ))
            } else {
                _statusMessage.value = chatService.getStatus()
                addMessage(ChatMessage(
                    text = "I'm having trouble starting up. ${chatService.getStatus()}",
                    isFromUser = false
                ))
            }
        }
    }

    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return

        // Add user message immediately
        addMessage(ChatMessage(messageText, true))

        // Set loading state
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chatService.sendMessage(messageText)
                addMessage(ChatMessage(response, false))
            } catch (e: Exception) {
                addMessage(ChatMessage(
                    "Sorry, I encountered an error: ${e.message}",
                    false
                ))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun retryInitialization() {
        initializeChat()
    }

    override fun onCleared() {
        super.onCleared()
        chatService.shutdown()
    }
}
