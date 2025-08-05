// Complete Ultra-Fast ChatScreen.kt
package com.example.assistant

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Comment  // Using Comment instead of Chat
import androidx.compose.material.icons.filled.Message  // Alternative icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Custom colors for ultra-fast theme
private val UltraFastBlue = Color(0xFF2196F3)
private val SpeedGreen = Color(0xFF4CAF50)
private val WarningOrange = Color(0xFFFF9800)

@Composable
fun ChatScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Ultra-fast service and state
    val chatService = remember { ChatService(context) }

    // UI State
    var isChatOpen by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var currentMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Initializing...") }
    var useExtremeSpeed by remember { mutableStateOf(false) } // Keep for UI but don't use

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Initialize ultra-fast service
    LaunchedEffect(Unit) {
        isLoading = true
        statusMessage = "🚀 Loading Ultra-Fast Gemma..."

        val success = chatService.initialize()
        isInitialized = success
        isLoading = false

        statusMessage = chatService.getStatus()

        if (success) {
            messages = messages + ChatMessage(
                "🏃‍♂️ Ultra-Fast Gemma Ready!\n\nOptimized for speed with 64-token responses and TopK=1.\nToggle extreme speed mode for 5-second responses.",
                false
            )
        } else {
            messages = messages + ChatMessage(
                "❌ Ultra-fast initialization failed: ${chatService.getStatus()}",
                false
            )
        }
    }

    // Auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main content with ultra-fast branding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ultra-fast header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = UltraFastBlue
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏃‍♂️ Ultra-Fast Gemma Assistant",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Optimized for 5-15 second responses • 64 tokens • TopK=1",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Speed toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (useExtremeSpeed) Color.Red.copy(alpha = 0.1f) else SpeedGreen.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (useExtremeSpeed) "⚡ EXTREME SPEED MODE" else "🏃‍♂️ ULTRA-FAST MODE",
                        fontWeight = FontWeight.Bold,
                        color = if (useExtremeSpeed) Color.Red else SpeedGreen,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = useExtremeSpeed,
                        onCheckedChange = { useExtremeSpeed = it }
                    )
                }
                if (useExtremeSpeed) {
                    Text(
                        text = "⚡ Target: 5-8 seconds, very short responses",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = Color.Red
                    )
                } else {
                    Text(
                        text = "🏃‍♂️ Target: 10-15 seconds, 64-token responses",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = SpeedGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status indicator
            UltraFastStatusCard(
                isInitialized = isInitialized,
                isLoading = isLoading,
                statusMessage = statusMessage
            )
        }

        // Ultra-fast floating chat button
        FloatingActionButton(
            onClick = { isChatOpen = !isChatOpen },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp),
            containerColor = UltraFastBlue,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isChatOpen) Icons.Default.Close else Icons.Default.Comment,
                contentDescription = if (isChatOpen) "Close Ultra-Fast Chat" else "Open Ultra-Fast Chat",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Ultra-fast chat interface
        AnimatedVisibility(
            visible = isChatOpen,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            UltraFastChatInterface(
                messages = messages,
                currentMessage = currentMessage,
                onMessageChange = { currentMessage = it },
                onSendMessage = {
                    if (currentMessage.isNotBlank() && !isLoading && isInitialized) {
                        val userMessage = ChatMessage(currentMessage, true)
                        messages = messages + userMessage

                        val messageToSend = currentMessage
                        currentMessage = ""
                        isLoading = true

                        coroutineScope.launch {
                            try {
                                val startTime = System.currentTimeMillis()

                                val response = chatService.sendMessage(messageToSend)

                                val responseTime = System.currentTimeMillis() - startTime
                                val speedIndicator = when {
                                    responseTime < 8000 -> "⚡"
                                    responseTime < 15000 -> "🏃‍♂️"
                                    else -> "🐌"
                                }

                                val assistantMessage = ChatMessage(
                                    "$speedIndicator $response\n\n${speedIndicator} Response time: ${responseTime}ms",
                                    false
                                )
                                messages = messages + assistantMessage

                            } catch (e: Exception) {
                                val errorMessage = ChatMessage(
                                    "❌ Ultra-fast error: ${e.message}",
                                    false
                                )
                                messages = messages + errorMessage
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                isLoading = isLoading,
                isInitialized = isInitialized,
                useExtremeSpeed = useExtremeSpeed,
                listState = listState
            )
        }
    }
}

@Composable
fun UltraFastChatInterface(
    messages: List<ChatMessage>,
    currentMessage: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean,
    isInitialized: Boolean,
    useExtremeSpeed: Boolean,
    listState: LazyListState
) {
    Card(
        modifier = Modifier
            .width(380.dp)
            .height(500.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Ultra-fast header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        UltraFastBlue,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = if (useExtremeSpeed) "⚡ Extreme Speed Chat" else "🏃‍♂️ Ultra-Fast Chat",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (useExtremeSpeed) "Target: 5-8 seconds" else "Target: 10-15 seconds",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    UltraFastMessageBubble(message = message)
                }

                if (isLoading) {
                    item {
                        UltraFastMessageBubble(
                            message = ChatMessage(
                                if (useExtremeSpeed) "⚡ Extreme speed generation..." else "🏃‍♂️ Ultra-fast generation...",
                                false
                            ),
                            isTyping = true
                        )
                    }
                }
            }

            // Input section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentMessage,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            if (isInitialized) {
                                if (useExtremeSpeed) "⚡ Ask quickly..." else "🏃‍♂️ Ask ultra-fast Gemma..."
                            } else {
                                "Loading ultra-fast mode..."
                            }
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading && isInitialized,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSendMessage,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (currentMessage.isNotBlank() && !isLoading && isInitialized)
                                UltraFastBlue
                            else
                                Color.Gray
                        ),
                    enabled = currentMessage.isNotBlank() && !isLoading && isInitialized
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun UltraFastMessageBubble(
    message: ChatMessage,
    isTyping: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    message.isFromUser -> UltraFastBlue
                    isTyping -> Color.Gray.copy(alpha = 0.3f)
                    else -> Color.Gray.copy(alpha = 0.1f)
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isFromUser) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun UltraFastStatusCard(
    isInitialized: Boolean,
    isLoading: Boolean,
    statusMessage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isInitialized -> SpeedGreen.copy(alpha = 0.1f)
                isLoading -> WarningOrange.copy(alpha = 0.1f)
                else -> Color.Red.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when {
                isInitialized -> "🏃‍♂️"
                isLoading -> "🚀"
                else -> "❌"
            }

            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column {
                Text(
                    text = when {
                        isInitialized -> "Ultra-Fast Ready!"
                        isLoading -> "Loading Ultra-Fast Mode..."
                        else -> "Ultra-Fast Setup Failed"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = when {
                        isInitialized -> SpeedGreen
                        isLoading -> WarningOrange
                        else -> Color.Red
                    }
                )

                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Note: ChatMessage data class should be defined in a separate file or your existing file
// Remove this if you already have ChatMessage defined elsewhere