// Updated MainActivity.kt - Professional System Assistant Setup
package com.example.assistant

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assistant.ui.theme.AssistantTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay

class MainActivity : ComponentActivity() {

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1000
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d(TAG, "🚀 System Assistant Control Panel")

        setContent {
            AssistantTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SystemAssistantControlPanel(
                        onRequestPermission = {
                            Log.d(TAG, "📱 Requesting overlay permission")
                            requestOverlayPermission()
                        },
                        onStartService = {
                            Log.d(TAG, "🚀 Starting system assistant")
                            startSystemAssistant()
                        },
                        onStopService = {
                            Log.d(TAG, "🛑 Stopping system assistant")
                            stopSystemAssistant()
                        },
                        hasPermission = hasOverlayPermission()
                    )
                }
            }
        }
    }

    private fun hasOverlayPermission(): Boolean {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
        Log.d(TAG, "🔍 Has overlay permission: $hasPermission")
        return hasPermission
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    private fun startSystemAssistant() {
        if (hasOverlayPermission()) {
            Log.d(TAG, "✅ Permission granted, starting system assistant")

            val intent = Intent(this, ModernOverlayService::class.java)
            startService(intent)

            Toast.makeText(
                this,
                "🚀 System AI Assistant activated! Look for the floating button",
                Toast.LENGTH_LONG
            ).show()

        } else {
            Log.e(TAG, "❌ No overlay permission")
            Toast.makeText(
                this,
                "❌ Please grant overlay permission first",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun stopSystemAssistant() {
        Log.d(TAG, "🛑 Stopping system assistant")
        val intent = Intent(this, ModernOverlayService::class.java)
        stopService(intent)
        Toast.makeText(this, "🛑 System Assistant stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "📱 onActivityResult: $requestCode")

        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (hasOverlayPermission()) {
                Toast.makeText(
                    this,
                    "✅ Permission granted! You can now start the system assistant.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "❌ Permission denied. The system assistant requires overlay permission to work.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
fun ModelDownloadScreen(
    onDownloadComplete: () -> Unit
) {
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadStatus by remember { mutableStateOf("Checking for Gemma 3N model...") }

    LaunchedEffect(Unit) {
        // Check and download model
        downloadStatus = "Downloading Gemma 3N model..."
        // Update progress...
        onDownloadComplete()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🤖 Setting up Gemma 3N")
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(progress = downloadProgress / 100f)
        Text(downloadStatus)
    }
}

@Composable
fun SystemAssistantControlPanel(
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    hasPermission: Boolean
) {
    val primaryColor = Color(0xFF6366F1)
    val secondaryColor = Color(0xFF10B981)
    val surfaceColor = Color(0xFFF9FAFB)

    val context = LocalContext.current
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadStatus by remember { mutableStateOf("Ready to start") }
    var isDownloading by remember { mutableStateOf(false) }
    var isServiceRunning by remember { mutableStateOf(false) }

    // Create ChatService instance for progress tracking
    val chatService = remember { ChatService(context) }

    // Set up progress tracking
    LaunchedEffect(Unit) {
        chatService.onProgressUpdate = { progress, status ->
            downloadProgress = progress
            downloadStatus = status
            isDownloading = progress in 1..99

            if (progress == 100) {
                // Small delay to show completion
//                delay(1000)
                isDownloading = false
                downloadStatus = "System Assistant Ready!"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        surfaceColor,
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Header Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = primaryColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🤖",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "System AI Assistant",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Powered by Gemma 3N",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Permission Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasPermission) {
                        secondaryColor.copy(alpha = 0.1f)
                    } else {
                        Color.Red.copy(alpha = 0.1f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.Settings,
                        contentDescription = null,
                        tint = if (hasPermission) secondaryColor else Color.Red,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasPermission) "Overlay Permission: Granted" else "Overlay Permission: Required",
                            fontWeight = FontWeight.Bold,
                            color = if (hasPermission) secondaryColor else Color.Red
                        )

                        Text(
                            text = if (hasPermission) {
                                "System assistant can appear over other apps"
                            } else {
                                "Required for system-wide AI assistance"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Section
            if (!hasPermission) {
                // Permission Request Button
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grant Permission",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Main Control Section
                if (isDownloading) {
                    // Progress Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔄 Setting up AI Assistant",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            LinearProgressIndicator(
                                progress = downloadProgress / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = secondaryColor,
                                trackColor = secondaryColor.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "$downloadProgress%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )

                            Text(
                                text = downloadStatus,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    // Start/Stop Buttons
                    Column {
                        Button(
                            onClick = {
                                isServiceRunning = true
                                isDownloading = true
                                downloadProgress = 0
                                downloadStatus = "Starting system assistant..."

                                // Start the overlay service
                                onStartService()

                                // Initialize ChatService to trigger progress
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        chatService.initialize()
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "ChatService init error", e)
                                        isDownloading = false
                                        downloadStatus = "Setup failed. Please try again."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = secondaryColor
                            ),
                            shape = RoundedCornerShape(28.dp),
                            enabled = !isServiceRunning
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isServiceRunning) "Assistant Running" else "Start System Assistant",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                isServiceRunning = false
                                isDownloading = false
                                downloadProgress = 0
                                downloadStatus = "Ready to start"
                                onStopService()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            enabled = isServiceRunning
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Stop Assistant",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Features Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "✨ Features",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val features = listOf(
                        "🌐 System-wide AI access from any app",
                        "🤖 Powered by Google Gemma 3N AI",
                        "🔒 Complete privacy - all processing on-device",
                        "💬 Intelligent conversations with context",
                        "⚡ Auto-downloads AI model on first use",
                        "🎨 Modern, intuitive floating interface"
                    )

                    features.forEach { feature ->
                        Text(
                            text = feature,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 3.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = if (isDownloading) {
                    "Please wait while we set up your AI assistant. This may take a few minutes on first launch."
                } else {
                    "After starting, look for the floating AI button. Tap it to chat with your assistant from anywhere!"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}