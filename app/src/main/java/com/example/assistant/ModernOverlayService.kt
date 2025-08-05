// ModernOverlayService.kt - Professional System-wide AI Assistant
package com.example.assistant

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.assistant.ChatMessage
import kotlinx.coroutines.launch

class ModernOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingButton: View? = null
    private var chatInterface: View? = null
    private var isExpanded = false
    private var isAnimating = false

    private lateinit var chatService: ChatService
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private val messages = mutableListOf<ChatMessage>()
    private var messagesContainer: LinearLayout? = null
    private var messagesScrollView: ScrollView? = null
    private var inputField: EditText? = null
    private var sendButton: ImageView? = null

    companion object {
        private const val TAG = "ModernOverlayService"

        // Cool color scheme
        private const val PRIMARY_COLOR = "#6366F1"      // Indigo
        private const val PRIMARY_DARK = "#4F46E5"       // Darker indigo
        private const val SECONDARY_COLOR = "#10B981"    // Emerald
        private const val SURFACE_COLOR = "#FFFFFF"      // White
        private const val ON_SURFACE = "#1F2937"         // Dark gray
        private const val SURFACE_VARIANT = "#F9FAFB"    // Light gray
        private const val OUTLINE = "#E5E7EB"            // Border gray
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Modern System Assistant starting...")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        chatService = ChatService(this)

        chatService.onProgressUpdate = { progress, status ->
            Log.d(TAG, "UI Progress Update: $progress% - $status")

            // Update any UI elements if needed
            if (progress == 100) {
                addSystemMessage("Gemma 3N AI Assistant ready! How can I help you?")
            } else if (progress > 0) {
                addSystemMessage("Setting up AI: $status ($progress%)")
            }
        }

        // Initialize AI service
        serviceScope.launch {
            Log.d(TAG, "🤖 Initializing AI capabilities...")
            val success = chatService.initialize()
            if (success) {
                Log.d(TAG, "✅ AI ready for system-wide assistance")
                addSystemMessage("System AI Assistant ready! I can help you with anything.")
            } else {
                Log.e(TAG, "❌ AI initialization failed")
                addSystemMessage("AI is having trouble starting. Please restart the assistant.")
            }
        }

        createModernFloatingButton()
    }

    private fun createModernFloatingButton() {
        Log.d(TAG, "🎨 Creating modern floating assistant...")

        try {
            // Create modern floating button with elevation and animation
            floatingButton = FrameLayout(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    dpToPx(64), dpToPx(64)
                )
            }

            // Create the actual button view
            val buttonView = createStylizedButton()
            (floatingButton as FrameLayout).addView(buttonView)

            val buttonParams = WindowManager.LayoutParams(
                dpToPx(64), dpToPx(64),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = dpToPx(20)
                y = dpToPx(100)
            }

            // Add touch handlers
            floatingButton?.setOnClickListener {
                if (!isAnimating) {
                    Log.d(TAG, "🎯 Assistant button tapped")
                    animateButtonPress()
                    toggleChatInterface()
                }
            }

            floatingButton?.setOnTouchListener(ModernTouchListener())

            windowManager.addView(floatingButton, buttonParams)

            // Entrance animation
            animateButtonEntrance()

            Log.d(TAG, "✅ Modern floating assistant created")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating floating button", e)
        }
    }

    private fun createStylizedButton(): View {
        return ImageView(this).apply {
            // Create modern gradient background
            val gradientDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                gradientType = GradientDrawable.LINEAR_GRADIENT
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                colors = intArrayOf(
                    Color.parseColor(PRIMARY_COLOR),
                    Color.parseColor(PRIMARY_DARK)
                )
                setStroke(dpToPx(2), Color.WHITE)
            }

            background = gradientDrawable
            scaleType = ImageView.ScaleType.CENTER
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))

            // Set AI assistant icon (using Unicode for now, replace with custom icon)
            if (this is TextView) {
                text = "🤖"
                textSize = 20f
                setTextColor(Color.WHITE)
            } else {
                // For ImageView, you'd set a proper vector drawable here
                setColorFilter(Color.WHITE)
            }

            elevation = dpToPx(8).toFloat()
        }
    }

    private fun animateButtonEntrance() {
        floatingButton?.let { button ->
            button.scaleX = 0f
            button.scaleY = 0f
            button.alpha = 0f

            val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 0f, 1f)
            val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 0f, 1f)
            val alpha = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, alpha)
                duration = 400
                interpolator = OvershootInterpolator()
                start()
            }
        }
    }

    private fun animateButtonPress() {
        floatingButton?.let { button ->
            isAnimating = true

            val scaleDown = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.9f),
                    ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.9f)
                )
                duration = 100
                interpolator = AccelerateDecelerateInterpolator()
            }

            val scaleUp = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(button, "scaleX", 0.9f, 1f),
                    ObjectAnimator.ofFloat(button, "scaleY", 0.9f, 1f)
                )
                duration = 100
                interpolator = OvershootInterpolator()
            }

            scaleDown.start()
            serviceScope.launch {
                delay(100)
                scaleUp.start()
                delay(100)
                isAnimating = false
            }
        }
    }

    private inner class ModernTouchListener : View.OnTouchListener {
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
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()

                    if (Math.abs(deltaX) > 30 || Math.abs(deltaY) > 30) {
                        isDragging = true
                        val params = view.layoutParams as WindowManager.LayoutParams
                        params.x = initialX + deltaX
                        params.y = initialY + deltaY

                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating button position", e)
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        view.performClick()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun toggleChatInterface() {
        if (isExpanded) {
            hideChatInterface()
        } else {
            showChatInterface()
        }
    }

    private fun showChatInterface() {
        if (chatInterface != null) return

        Log.d(TAG, "💬 Opening AI chat interface...")

        try {
            chatInterface = createModernChatInterface()

            val chatParams = WindowManager.LayoutParams(
                dpToPx(340), dpToPx(500),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = dpToPx(30)
                y = dpToPx(180)
            }

            windowManager.addView(chatInterface, chatParams)
            animateChatInterfaceIn()

            isExpanded = true
            updateButtonState()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating chat interface", e)
        }
    }

    private fun createModernChatInterface(): View {
        // Main card container
        val cardView = CardView(this).apply {
            radius = dpToPx(20).toFloat()
            elevation = dpToPx(16).toFloat()
            setCardBackgroundColor(Color.parseColor(SURFACE_COLOR))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Main container
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Header
        val header = createChatHeader()
        mainContainer.addView(header)

        // Messages area
        val messagesArea = createMessagesArea()
        mainContainer.addView(messagesArea)

        // Input area
        val inputArea = createInputArea()
        mainContainer.addView(inputArea)

        cardView.addView(mainContainer)
        return cardView
    }

    private fun createChatHeader(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor(PRIMARY_COLOR))
            setPadding(dpToPx(16), dpToPx(12), dpToPx(12), dpToPx(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Title
            val title = TextView(this@ModernOverlayService).apply {
                text = "AI Assistant"
                setTextColor(Color.WHITE)
                textSize = 16f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }
            addView(title)

            // Close button
            val closeButton = ImageView(this@ModernOverlayService).apply {
                val closeDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#FFFFFF33"))
                    setStroke(dpToPx(1), Color.WHITE)
                }
                background = closeDrawable
                setColorFilter(Color.WHITE)
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(32))

                setOnClickListener {
                    hideChatInterface()
                }
            }
            addView(closeButton)
        }
    }

    private fun createMessagesArea(): View {
        messagesScrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
            setBackgroundColor(Color.parseColor(SURFACE_VARIANT))
            isVerticalScrollBarEnabled = false
        }

        messagesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(12), dpToPx(16), dpToPx(12), dpToPx(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        messagesScrollView?.addView(messagesContainer)

        // Add existing messages
        for (message in messages) {
            addMessageBubble(message)
        }

        return messagesScrollView!!
    }

    private fun createInputArea(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor(SURFACE_COLOR))
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Input field container
            val inputContainer = FrameLayout(this@ModernOverlayService).apply {
                val inputBg = GradientDrawable().apply {
                    cornerRadius = dpToPx(25).toFloat()
                    setColor(Color.parseColor(SURFACE_VARIANT))
                    setStroke(dpToPx(1), Color.parseColor(OUTLINE))
                }
                background = inputBg
                layoutParams = LinearLayout.LayoutParams(
                    0, dpToPx(50), 1f
                ).apply {
                    rightMargin = dpToPx(8)
                }
            }

            inputField = EditText(this@ModernOverlayService).apply {
                hint = "Ask me anything..."
                setHintTextColor(Color.parseColor("#6B7280"))
                setTextColor(Color.parseColor(ON_SURFACE))
                textSize = 14f
                background = null
                setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setSingleLine(true)
            }

            inputContainer.addView(inputField)
            addView(inputContainer)

            // Send button
            sendButton = ImageView(this@ModernOverlayService).apply {
                val sendBg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    gradientType = GradientDrawable.LINEAR_GRADIENT
                    orientation = GradientDrawable.Orientation.TOP_BOTTOM
                    colors = intArrayOf(
                        Color.parseColor(SECONDARY_COLOR),
                        Color.parseColor("#059669")
                    )
                }
                background = sendBg
                setColorFilter(Color.WHITE)
                setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
                layoutParams = LinearLayout.LayoutParams(dpToPx(50), dpToPx(50))
                elevation = dpToPx(4).toFloat()

                setOnClickListener {
                    sendMessage()
                }
            }
            addView(sendButton)
        }
    }

    private fun sendMessage() {
        val messageText = inputField?.text?.toString()?.trim()
        if (messageText.isNullOrEmpty()) return

        Log.d(TAG, "📤 Sending message: $messageText")

        // Add user message
        val userMessage = ChatMessage(messageText, true)
        addMessage(userMessage)
        inputField?.setText("")

        // Show typing indicator
        val typingMessage = ChatMessage("AI is thinking...", false, isTyping = true)
        addMessage(typingMessage)

        // Get AI response
        serviceScope.launch {
            try {
                val response = chatService.sendMessage(messageText)

                // Remove typing indicator
                removeMessage(typingMessage)

                // Add AI response
                val aiMessage = ChatMessage(response, false)
                addMessage(aiMessage)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting AI response", e)
                removeMessage(typingMessage)

                val errorMessage = ChatMessage(
                    "I'm having trouble processing that. Please try again.",
                    false
                )
                addMessage(errorMessage)
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        addMessageBubble(message)
        scrollToBottom()
    }

    private fun addSystemMessage(text: String) {
        val message = ChatMessage(text, false)
        addMessage(message)
    }

    private fun removeMessage(message: ChatMessage) {
        val index = messages.indexOf(message)
        if (index >= 0) {
            messages.removeAt(index)
            messagesContainer?.let { container ->
                if (index < container.childCount) {
                    container.removeViewAt(index)
                }
            }
        }
    }

    private fun addMessageBubble(message: ChatMessage) {
        messagesContainer?.let { container ->
            val bubbleView = createMessageBubble(message)
            container.addView(bubbleView)
        }
    }

    private fun createMessageBubble(message: ChatMessage): View {
        val bubbleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
                bottomMargin = dpToPx(4)
            }
        }

        val bubble = TextView(this).apply {
            text = message.text
            textSize = 14f
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))

            val bubbleDrawable = GradientDrawable().apply {
                cornerRadius = dpToPx(18).toFloat()
                if (message.isFromUser) {
                    gradientType = GradientDrawable.LINEAR_GRADIENT
                    orientation = GradientDrawable.Orientation.TOP_BOTTOM
                    colors = intArrayOf(
                        Color.parseColor(PRIMARY_COLOR),
                        Color.parseColor(PRIMARY_DARK)
                    )
                    setTextColor(Color.WHITE)
                } else {
                    setColor(Color.WHITE)
                    setStroke(dpToPx(1), Color.parseColor(OUTLINE))
                    setTextColor(Color.parseColor(ON_SURFACE))
                }
            }

            background = bubbleDrawable
            elevation = dpToPx(2).toFloat()

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (message.isFromUser) {
                    gravity = Gravity.END
                    leftMargin = dpToPx(40)
                } else {
                    gravity = Gravity.START
                    rightMargin = dpToPx(40)
                }
            }
            layoutParams = params

            // Add typing animation for AI responses
            if (message.isTyping) {
                alpha = 0.7f
                val pulseAnimation = ValueAnimator.ofFloat(0.7f, 1f, 0.7f).apply {
                    duration = 1000
                    repeatCount = ValueAnimator.INFINITE
                    addUpdateListener { animator ->
                        alpha = animator.animatedValue as Float
                    }
                }
                pulseAnimation.start()
            }
        }

        if (message.isFromUser) {
            bubbleContainer.addView(View(this), LinearLayout.LayoutParams(0, 0, 1f))
            bubbleContainer.addView(bubble)
        } else {
            bubbleContainer.addView(bubble)
            bubbleContainer.addView(View(this), LinearLayout.LayoutParams(0, 0, 1f))
        }

        return bubbleContainer
    }

    private fun scrollToBottom() {
        messagesScrollView?.post {
            messagesScrollView?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun animateChatInterfaceIn() {
        chatInterface?.let { chat ->
            chat.scaleX = 0.8f
            chat.scaleY = 0.8f
            chat.alpha = 0f

            val scaleX = ObjectAnimator.ofFloat(chat, "scaleX", 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(chat, "scaleY", 0.8f, 1f)
            val alpha = ObjectAnimator.ofFloat(chat, "alpha", 0f, 1f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, alpha)
                duration = 300
                interpolator = OvershootInterpolator()
                start()
            }
        }
    }

    private fun hideChatInterface() {
        chatInterface?.let { chat ->
            val scaleX = ObjectAnimator.ofFloat(chat, "scaleX", 1f, 0.8f)
            val scaleY = ObjectAnimator.ofFloat(chat, "scaleY", 1f, 0.8f)
            val alpha = ObjectAnimator.ofFloat(chat, "alpha", 1f, 0f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, alpha)
                duration = 200
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        try {
                            windowManager.removeView(chat)
                            chatInterface = null
                            isExpanded = false
                            updateButtonState()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error removing chat interface", e)
                        }
                    }
                })
                start()
            }
        }
    }

    private fun updateButtonState() {
        // You could change the button appearance here based on state
        // For example, change color or icon when chat is open
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 Modern System Assistant shutting down...")

        try {
            floatingButton?.let { windowManager.removeView(it) }
            chatInterface?.let { windowManager.removeView(it) }
            chatService.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
