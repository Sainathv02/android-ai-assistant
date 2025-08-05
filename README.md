# 🤖 Android AI Assistant - System-Wide Gemma 3N Integration

**Revolutionary system-wide AI assistant that brings Google's Gemma 3N directly into Android's interface layer**

![Competition Entry](https://img.shields.io/badge/Competition-Google%20Gemma%203N%20Hackathon-blue)
![Platform](https://img.shields.io/badge/Platform-Android-green)
![AI Model](https://img.shields.io/badge/AI-Gemma%203N%20E2B-orange)

## 🏆 Competition Innovation

This project represents a **first-of-its-kind** system-level AI integration on Android. Instead of creating another chat app, we've built an AI assistant that lives in the Android system itself, accessible from any app through a floating overlay interface.

### 🎯 **The Problem We Solved**
- Traditional AI apps require constant app switching
- Users lose context when moving between apps and AI
- No seamless integration with the Android workflow

### 💡 **Our Solution**
- **System overlay technology** - AI button floats over ALL apps
- **One-tap access** - Instant AI without leaving your current task
- **Context preservation** - Maintains conversation history across apps
- **Zero friction** - No app switching, no workflow interruption

## ✨ Key Features

### 🌐 **System-Wide Integration**
- Floating AI button accessible from any Android app
- Works in games, browsers, social media, productivity apps
- Persistent across the entire Android experience

### 🤖 **Powered by Gemma 3N**
- Google's latest Gemma 3N E2B model (2B effective parameters)
- Automatic model download and setup
- GPU acceleration with CPU fallback
- Optimized for mobile performance

### 🔒 **Privacy-First Architecture**
- 100% on-device processing
- No data sent to external servers
- All conversations stay on your device
- HuggingFace integration for model downloads only

### 🎨 **Modern Android UI**
- Material 3 design language
- Beautiful gradient animations
- Professional progress indicators
- Responsive touch interactions

## 🛠️ Technical Architecture

### **Core Components**
```
📱 ModernOverlayService     - System overlay management
🧠 GemmaService            - AI model integration  
💬 ChatService             - Conversation handling
🎨 MainActivity            - Setup and permissions
🔧 SystemAssistantUI       - Modern interface components
```

### **Technology Stack**
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Android Views
- **AI Integration**: MediaPipe LLM Inference API
- **Architecture**: MVVM with Coroutines
- **Model**: Google Gemma 3N E2B (INT4 quantized)
- **Build System**: Gradle with Kotlin DSL

### **Key Android Technologies**
- `WindowManager` for system overlays
- `TYPE_APPLICATION_OVERLAY` for floating windows
- `MediaPipe Tasks GenAI` for on-device inference
- `OkHttp` for model downloading
- `Coroutines` for async operations

## 🚀 Setup Instructions

### **Prerequisites**
- Android Studio Arctic Fox or later
- Android device/emulator with API 23+ (Android 6.0+)
- 4GB+ RAM recommended for AI model
- Internet connection for initial model download

### **1. Clone Repository**
```bash
git clone https://github.com/Sainathv02/android-ai-assistant.git
cd android-ai-assistant
```

### **2. Open in Android Studio**
1. Open Android Studio
2. File → Open → Select the cloned directory
3. Wait for Gradle sync to complete

### **3. Configure HuggingFace Token (Required)**
1. Go to [HuggingFace Settings](https://huggingface.co/settings/tokens)
2. Create a new token with "Read" permissions
3. Open `app/src/main/java/com/example/assistant/GemmaService.kt`
4. Replace `"hf_your_token_here"` with your actual token:
```kotlin
private const val HF_TOKEN = "hf_your_actual_token_here"
```

### **4. Build and Install**
```bash
# Build the project
./gradlew assembleDebug

# Install via ADB (if device connected)
adb install app/build/outputs/apk/debug/app-debug.apk
```

### **5. Grant Permissions**
1. Open the app
2. Tap "Grant Permission" 
3. Enable "Display over other apps" permission
4. Return to app and tap "Start System Assistant"

## 📱 ADB Commands Reference

### **Basic ADB Setup**
```bash
# Check if device is connected
adb devices

# Enable developer options on device:
# Settings → About Phone → Tap "Build Number" 7 times
# Settings → Developer Options → Enable "USB Debugging"
```

### **Installation Commands**
```bash
# Install the app
adb install app/build/outputs/apk/debug/app-debug.apk

# Reinstall (if already installed)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Install and grant overlay permission automatically (API 23+)
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant com.example.assistant android.permission.SYSTEM_ALERT_WINDOW
```

### **Debugging Commands**
```bash
# View real-time logs
adb logcat | grep "GemmaService\|ChatService\|ModernOverlay"

# Check app status
adb shell dumpsys activity | grep com.example.assistant

# Clear app data (reset)
adb shell pm clear com.example.assistant

# Uninstall
adb uninstall com.example.assistant
```

### **Model File Management**
```bash
# Check if model downloaded
adb shell ls -la /data/data/com.example.assistant/files/

# Remove corrupted model (force re-download)
adb shell rm /data/data/com.example.assistant/files/gemma-3n-E2B-it-int4.task

# Check storage space
adb shell df -h /data
```

## 🎮 How to Use

### **First Launch**
1. **Install** and open the app
2. **Grant overlay permission** when prompted
3. **Start the system assistant**
4. **Wait for model download** (3-5 minutes, one-time setup)
5. **Look for floating AI button** - blue circle with 🤖

### **Daily Usage**
1. **From any app** - Tap the floating AI button
2. **Chat interface opens** - Ask questions, get help
3. **Drag the button** - Move it anywhere on screen
4. **Close chat** - Tap the X or tap outside
5. **AI remembers context** - Continues conversations


## 🧠 AI Model Details

### **Gemma 3N E2B Specifications**
- **Parameters**: 2B effective (5B total with parameter efficiency)
- **Quantization**: INT4 for mobile optimization
- **Model Size**: ~3GB download
- **Context Length**: 32K tokens
- **Capabilities**: Text generation, conversation, reasoning

### **Performance Expectations**
- **GPU Mode**: 2-5 seconds per response
- **CPU Mode**: 5-15 seconds per response  
- **Memory Usage**: 2-4GB RAM
- **Storage**: 3GB for model + 1GB cache

### **Model Download Process**
1. App checks for cached model
2. Downloads from HuggingFace if missing
3. Validates file integrity
4. Loads into MediaPipe inference engine
5. Ready for system-wide use

## 🔧 Development Setup

### **Project Structure**
```
app/src/main/java/com/example/assistant/
├── MainActivity.kt              # Permission & setup UI
├── ModernOverlayService.kt      # Main overlay service
├── ChatService.kt               # Conversation management
├── GemmaService.kt              # AI model integration
├── ChatMessage.kt               # Data classes
└── ui/theme/                    # Material 3 theming
```

### **Key Configuration Files**
```
app/
├── build.gradle.kts             # Dependencies & build config
├── src/main/AndroidManifest.xml # Permissions & services
└── proguard-rules.pro          # Code optimization rules
```

### **Important Dependencies**
```kotlin
// AI and HTTP
implementation 'com.google.mediapipe:tasks-genai:0.10.14'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'

// UI and Architecture  
implementation 'androidx.compose.material3:material3'
implementation 'androidx.compose.material:material-icons-extended'
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android'
```

## 🐛 Troubleshooting

### **Common Issues**

#### **"Permission Denied" Error**
```bash
# Solution: Grant overlay permission manually
adb shell appops set com.example.assistant SYSTEM_ALERT_WINDOW allow
```

#### **"Model Download Failed"**
1. Check internet connection
2. Verify HuggingFace token is valid
3. Check device storage (need 4GB+ free)
4. Clear app data and retry

#### **"AI Responses Too Slow"**
- Device may be using CPU instead of GPU
- Close other memory-intensive apps
- Restart the assistant service

#### **"Floating Button Not Visible"**
1. Check overlay permission is granted
2. Look in screen corners (button may be off-screen)
3. Restart the service
4. Check logs: `adb logcat | grep ModernOverlay`

### **Debug Mode**
Enable verbose logging by changing log level in code:
```kotlin
// In GemmaService.kt
private const val DEBUG_MODE = true
```

## 📄 License

MIT License - This project is open source and available for educational, research, and commercial use.

## 👨‍💻 Developer

**Sainath** - *Android Developer & AI Enthusiast*
- GitHub: [@Sainathv02](https://github.com/Sainathv02)
- Project: [android-ai-assistant](https://github.com/Sainathv02/android-ai-assistant)

---

*Built with ❤️ for the Google Gemma 3N Hackathon - Pushing the boundaries of mobile AI integration*
