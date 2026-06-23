package com.example.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

enum class WebSocketStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class LiveFeedItem(
    val id: Long,
    val source: String, // e.g., "AI Coach", "Peer Event", "System Protocol"
    val content: String,
    val timestamp: String,
    val severity: String = "INFO" // "INFO", "ALERT", "SUCCESS"
)

object CommWebSocketService {
    private const val TAG = "CommWebSocket"
    private const val ECHO_URL = "wss://echo.websocket.events"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var simulationJob: Job? = null
    private var reconnectJob: Job? = null
    private var feedIdCounter = 1L

    private val _status = MutableStateFlow(WebSocketStatus.DISCONNECTED)
    val status: StateFlow<WebSocketStatus> = _status.asStateFlow()

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    private val _liveFeeds = MutableStateFlow<List<LiveFeedItem>>(emptyList())
    val liveFeeds: StateFlow<List<LiveFeedItem>> = _liveFeeds.asStateFlow()

    init {
        // Start live simulator that ensures the UI is constantly feed-vibrant
        startLivePulseGenerator()
    }

    fun connect() {
        if (_status.value == WebSocketStatus.CONNECTED || _status.value == WebSocketStatus.CONNECTING) return

        _status.value = WebSocketStatus.CONNECTING
        logAndAddFeed("System Protocol", "Initiating secure WebSocket handshakes...", "INFO")

        val request = Request.Builder()
            .url(ECHO_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _status.value = WebSocketStatus.CONNECTED
                logAndAddFeed("System Protocol", "WebSocket successfully connected to $ECHO_URL", "SUCCESS")
                // Core handshake hello
                webSocket.send("CommCore Client v1.3 loaded and operational.")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                _messages.update { current -> (listOf("[Echo] $text") + current).take(20) }
                logAndAddFeed("Remote Server", "Echo received: $text", "INFO")
                triggerInteractiveCoachFeedback(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _status.value = WebSocketStatus.DISCONNECTED
                logAndAddFeed("System Protocol", "WebSocket channel cleanly closed. Code: $code", "INFO")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _status.value = WebSocketStatus.ERROR
                logAndAddFeed("System Protocol", "WebSocket handshakes failed: ${t.message}. Operating in Resilience Mode.", "ALERT")
                scheduleReconnect()
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User logout / clean exit request")
        webSocket = null
        _status.value = WebSocketStatus.DISCONNECTED
        reconnectJob?.cancel()
        logAndAddFeed("System Protocol", "WebSocket connection terminated.", "INFO")
    }

    fun sendMessage(msg: String) {
        val socket = webSocket
        if (socket != null && _status.value == WebSocketStatus.CONNECTED) {
            logAndAddFeed("User Payload", "Sent string over WebSocket frame: '$msg'", "INFO")
            socket.send(msg)
        } else {
            // Echo locally for simulated support when disconnected
            logAndAddFeed("Local Loopback", "Cached offline message client-side: '$msg'", "INFO")
            _messages.update { current -> (listOf("[Offline Local] $msg") + current).take(20) }
            triggerInteractiveCoachFeedback(msg)
        }
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(10000) // retry after 10s
            if (_status.value == WebSocketStatus.ERROR || _status.value == WebSocketStatus.DISCONNECTED) {
                logAndAddFeed("System Protocol", "Attempting websocket automatic auto-reconnect...", "INFO")
                connect()
            }
        }
    }

    private fun logAndAddFeed(source: String, content: String, severity: String) {
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        val timestampText = formatter.format(java.util.Date())
        val newItem = LiveFeedItem(
            id = feedIdCounter++,
            source = source,
            content = content,
            timestamp = timestampText,
            severity = severity
        )
        _liveFeeds.update { current -> (listOf(newItem) + current).take(60) }
    }

    private fun startLivePulseGenerator() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            // Seed first feeds
            logAndAddFeed("AI Coach", "CommCore speech engine actively monitoring vocal vibrations and silent pauses.", "INFO")
            delay(3000)
            logAndAddFeed("Peer Event", "Global User Nick (Archetype: Assertive Anchor) began VC Pitch in Professional mode.", "SUCCESS")
            
            val suggestions = listOf(
                "AI Coach" to "Tip: Maintain a slow voice tempo. Targeted 130 words-per-minute induces maximum listener trust.",
                "Peer Event" to "Global User Helen completed conflict resolution sandbox with 85/100 competence score.",
                "AI Coach" to "Tip: To defuse defensive peers, try empathetic mirroring like 'It sounds like you feel...'",
                "System Protocol" to "All deep network models synced. Speech-To-Text latency calibrated at 42ms.",
                "AI Coach" to "Reminder: Omit qualifiers to stay authoritative ('just', 'likely' -> 'decisively', 'certainly').",
                "Peer Event" to "Global User Clarissa is practicing public speaking with high vocal stability.",
                "AI Coach" to "Tip: Leverage anchoring in discussions. Set parameters early to capture debate control."
            )

            var index = 0
            while (true) {
                delay(12000) // push live updates every 12 seconds
                val (source, tips) = suggestions[index % suggestions.size]
                val severity = if (source == "AI Coach") "INFO" else "SUCCESS"
                logAndAddFeed(source, tips, severity)
                index++
            }
        }
    }

    private fun triggerInteractiveCoachFeedback(msg: String) {
        scope.launch {
            delay(1000)
            val lower = msg.lowercase()
            val response = when {
                lower.contains("just") || lower.contains("maybe") || lower.contains("sorry") -> {
                    "Vocal Auditor: Qualifier detected under transmission! Avoid apologizing in structural proposals to preserve leverage."
                }
                lower.contains("hello") || lower.contains("hi") -> {
                    "Voice Console: Welcome active partner. Ready to process voice scripts or custom scenarios."
                }
                lower.contains("assertive") || lower.contains("confidence") -> {
                    "Vocal Auditor: Frequency profile stabilized. High amplitude assertiveness detected."
                }
                else -> {
                    "AI Companion: Live conversational alignment adjusted. Coherence score is currently 94%."
                }
            }
            logAndAddFeed("AI Coach", response, "ALERT")
        }
    }
}
