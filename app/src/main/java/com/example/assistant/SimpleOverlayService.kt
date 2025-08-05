// Updated SimpleOverlayService.kt with better visibility and debugging
package com.example.assistant

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SimpleOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var chatView: View? = null
    private var isExpanded = false

    private lateinit var chatService: ChatService
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val TAG = "SimpleOverlayService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 SimpleOverlayService onCreate() called")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        chatService = ChatService(this)

        // Initialize chat service
        serviceScope.launch {
            Log.d(TAG, "🤖 Initializing ChatService...")
            val success = chatService.initialize()
            Log.d(TAG, "🤖 ChatService initialized: $success")
        }

        createFloatingButton()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🚀 onStartCommand called")
        return START_STICKY
    }

    private fun createFloatingButton() {
        Log.d(TAG, "🎯 Creating floating button...")

        try {
            // Create a more visible floating button
            floatingView = TextView(this).apply {
                text = "🤖"  // Robot emoji as button
                textSize = 24f
                setTextColor(Color.WHITE)
                setPadding(20, 20, 20, 20)

                // Create bright blue circular background
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#2196F3")) // Bright blue
                    setStroke(4, Color.WHITE) // White border
                }
                background = drawable

                // Make it clickable
                isClickable = true
                isFocusable = false
            }

            val buttonParams = WindowManager.LayoutParams(
                120, 120, // Larger size for visibility
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 50  // Closer to edge
                y = 200 // More visible position
            }

            // Simple click listener first
            floatingView?.setOnClickListener {
                Log.d(TAG, "🎯 Floating button clicked!")
                Toast.makeText(this@SimpleOverlayService, "🤖 Floating button works!", Toast.LENGTH_SHORT).show()
                toggleChat()
            }

            // Add touch listener for dragging
            floatingView?.setOnTouchListener(FloatingTouchListener())

            windowManager.addView(floatingView, buttonParams)
            Log.d(TAG, "✅ Floating button added to window manager")

            // Show confirmation toast
            Toast.makeText(this, "🎯 Floating button created! Look for 🤖", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating floating button", e)
            Toast.makeText(this, "❌ Error creating floating button: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private inner class FloatingTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var isDragging = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val params = view.layoutParams as WindowManager.LayoutParams
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    Log.d(TAG, "👆 Touch DOWN at (${event.rawX}, ${event.rawY})")
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()

                    if (Math.abs(deltaX) > 20 || Math.abs(deltaY) > 20) {
                        isDragging = true
                        val params = view.layoutParams as WindowManager.LayoutParams
                        params.x = initialX + deltaX
                        params.y = initialY + deltaY

                        try {
                            windowManager.updateViewLayout(view, params)
                            Log.d(TAG, "📱 Button moved to (${params.x}, ${params.y})")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error moving button", e)
                        }
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    Log.d(TAG, "👆 Touch UP - isDragging: $isDragging")
                    if (!isDragging) {
                        // It was a click, not a drag
                        Log.d(TAG, "🎯 Button clicked!")
                        view.performClick()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun toggleChat() {
        Log.d(TAG, "🔄 Toggle chat - isExpanded: $isExpanded")

        if (isExpanded) {
            // Hide chat
            chatView?.let {
                try {
                    windowManager.removeView(it)
                    Log.d(TAG, "❌ Chat view removed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing chat view", e)
                }
                chatView = null
            }
            isExpanded = false

            // Change button back to robot
            (floatingView as? TextView)?.text = "🤖"

        } else {
            // Show chat
            createChatInterface()
            isExpanded = true

            // Change button to X
            (floatingView as? TextView)?.text = "❌"
        }
    }

    private fun createChatInterface() {
        Log.d(TAG, "💬 Creating chat interface...")

        try {
            // Create chat interface using traditional Views
            val chatLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.WHITE)
                setPadding(16, 16, 16, 16)

                // Add border
                val drawable = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    setStroke(4, Color.parseColor("#2196F3"))
                    cornerRadius = 20f
                }
                background = drawable
            }

            // Header
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(Color.parseColor("#2196F3"))
                setPadding(16, 16, 16, 16)
            }

            val titleText = TextView(this).apply {
                text = "🤖 System Gemma Chat"
                setTextColor(Color.WHITE)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val closeButton = Button(this).apply {
                text = "❌"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.TRANSPARENT)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    Log.d(TAG, "❌ Close button clicked")
                    toggleChat()
                }
            }

            header.addView(titleText)
            header.addView(closeButton)

            // Messages area
            val messagesScroll = ScrollView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                setBackgroundColor(Color.parseColor("#F5F5F5"))
            }

            val messagesContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            messagesScroll.addView(messagesContainer)

            // Add welcome message
            addMessageToContainer(messagesContainer, "🚀 System-wide Gemma ready! Ask me anything!", false)

            // Input area
            val inputLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.parseColor("#FAFAFA"))
            }

            val inputField = EditText(this).apply {
                hint = "Ask me anything..."
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setSingleLine(true)
                setPadding(16, 16, 16, 16)

                val drawable = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    setStroke(2, Color.parseColor("#E0E0E0"))
                    cornerRadius = 25f
                }
                background = drawable
            }

            val sendButton = Button(this).apply {
                text = "➤"
                textSize = 18f
                setTextColor(Color.WHITE)

                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#2196F3"))
                }
                background = drawable

                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    leftMargin = 16
                }

                setOnClickListener {
                    val message = inputField.text.toString().trim()
                    Log.d(TAG, "📝 Send button clicked: $message")

                    if (message.isNotEmpty()) {
                        // Add user message
                        addMessageToContainer(messagesContainer, message, true)
                        inputField.setText("")

                        // Show "thinking" message
                        val thinkingView = addMessageToContainer(messagesContainer, "🤖 Thinking...", false)

                        // Get AI response
                        serviceScope.launch {
                            try {
                                Log.d(TAG, "🤖 Getting AI response...")
                                val response = chatService.sendMessage(message)
                                Log.d(TAG, "✅ AI response received: ${response.take(50)}...")

                                // Remove thinking message and add response
                                messagesContainer.removeView(thinkingView)
                                addMessageToContainer(messagesContainer, response, false)

                                // Scroll to bottom
                                messagesScroll.post {
                                    messagesScroll.fullScroll(ScrollView.FOCUS_DOWN)
                                }

                            } catch (e: Exception) {
                                Log.e(TAG, "❌ AI response error", e)
                                messagesContainer.removeView(thinkingView)
                                addMessageToContainer(messagesContainer, "❌ Error: ${e.message}", false)
                            }
                        }
                    }
                }
            }

            inputLayout.addView(inputField)
            inputLayout.addView(sendButton)

            // Assemble chat layout
            chatLayout.addView(header)
            chatLayout.addView(messagesScroll)
            chatLayout.addView(inputLayout)

            chatView = chatLayout

            val chatParams = WindowManager.LayoutParams(
                800, 600, // Width, Height
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            windowManager.addView(chatView, chatParams)
            Log.d(TAG, "✅ Chat interface created and added")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating chat interface", e)
            Toast.makeText(this, "❌ Error creating chat: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMessageToContainer(container: LinearLayout, message: String, isUser: Boolean): View {
        val messageView = TextView(this).apply {
            text = message
            textSize = 14f
            setPadding(16, 12, 16, 12)

            val drawable = GradientDrawable().apply {
                cornerRadius = 20f
                if (isUser) {
                    setColor(Color.parseColor("#2196F3"))
                } else {
                    setColor(Color.parseColor("#E0E0E0"))
                }
            }
            background = drawable

            if (isUser) {
                setTextColor(Color.WHITE)
            } else {
                setTextColor(Color.BLACK)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
                if (isUser) gravity = Gravity.END else gravity = Gravity.START
            }
            layoutParams = params
        }

        container.addView(messageView)
        return messageView
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 Service onDestroy() called")

        try {
            floatingView?.let {
                windowManager.removeView(it)
                Log.d(TAG, "🗑️ Floating button removed")
            }
            chatView?.let {
                windowManager.removeView(it)
                Log.d(TAG, "🗑️ Chat view removed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}