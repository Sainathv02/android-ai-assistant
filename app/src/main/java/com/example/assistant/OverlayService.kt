// OverlayService.kt - System-wide floating assistant
package com.example.assistant

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import kotlinx.coroutines.launch


class OverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var chatView: ComposeView? = null
    private var isExpanded = false

    // Lifecycle components
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingWidget()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return START_STICKY
    }

    private fun createFloatingWidget() {
        // Create floating button
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget, null)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 100
        layoutParams.y = 100

        // Add touch listener for dragging and clicking
        floatingView?.setOnTouchListener(FloatingOnTouchListener())

        // Add click listener to expand/collapse
        floatingView?.findViewById<ImageView>(R.id.floating_icon)?.setOnClickListener {
            toggleChat()
        }

        windowManager.addView(floatingView, layoutParams)
    }

    private fun toggleChat() {
        if (isExpanded) {
            // Collapse - remove chat view
            chatView?.let {
                windowManager.removeView(it)
                chatView = null
            }
            isExpanded = false
        } else {
            // Expand - show chat interface
            showChatInterface()
            isExpanded = true
        }
    }

    private fun showChatInterface() {
        chatView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    SystemOverlayChatInterface(
                        onClose = { toggleChat() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        val chatLayoutParams = WindowManager.LayoutParams(
            800, // Width
            600, // Height
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        chatLayoutParams.gravity = Gravity.CENTER

        windowManager.addView(chatView, chatLayoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        floatingView?.let { windowManager.removeView(it) }
        chatView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Touch listener for dragging the floating button
    private inner class FloatingOnTouchListener : View.OnTouchListener {
        private var x = 0
        private var y = 0
        private var px = 0f
        private var py = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = view.layoutParams.let { it as WindowManager.LayoutParams }.x
                    y = view.layoutParams.let { it as WindowManager.LayoutParams }.y
                    px = event.rawX
                    py = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val layoutParams = view.layoutParams as WindowManager.LayoutParams
                    layoutParams.x = x + (event.rawX - px).toInt()
                    layoutParams.y = y + (event.rawY - py).toInt()
                    windowManager.updateViewLayout(view, layoutParams)
                }
            }
            return false
        }
    }
}

// SystemOverlayChatInterface.kt - Overlay-optimized chat UI
@Composable
fun SystemOverlayChatInterface(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val chatService = remember { ChatService(context) }

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var currentMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Initialize service
    LaunchedEffect(Unit) {
        val success = chatService.initialize()
        isInitialized = success
        if (success) {
            messages = messages + ChatMessage(
                "🚀 System-wide Gemma Assistant ready! Ask me anything from anywhere.",
                false
            )
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌐 System Assistant",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            // Chat messages
            LazyColumn(
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
                            message = ChatMessage("🤖 Processing...", false),
                            isTyping = true
                        )
                    }
                }
            }

            // Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentMessage,
                    onValueChange = { currentMessage = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask system-wide assistant...") },
                    enabled = isInitialized && !isLoading,
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (currentMessage.isNotBlank() && isInitialized && !isLoading) {
                            val userMsg = ChatMessage(currentMessage, true)
                            messages = messages + userMsg

                            val msgToSend = currentMessage
                            currentMessage = ""
                            isLoading = true

                            coroutineScope.launch {
                                try {
                                    val response = chatService.sendMessage(msgToSend)
                                    val aiMsg = ChatMessage(response, false)
                                    messages = messages + aiMsg
                                } catch (e: Exception) {
                                    val errorMsg = ChatMessage("Error: ${e.message}", false)
                                    messages = messages + errorMsg
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = currentMessage.isNotBlank() && isInitialized && !isLoading
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}
