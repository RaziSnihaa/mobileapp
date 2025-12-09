package com.example.eyescroll

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

/**
 * Simple remote control service: listens on TCP port 8080 for plain text commands:
 * - "scroll up"
 * - "scroll down"
 *
 * For production, secure this (authentication, encryption)!
 */
class RemoteControlService : Service() {

    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startServer()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serverJob?.cancel()
    }

    private fun startServer() {
        serverJob = scope.launch {
            try {
                val serverSocket = ServerSocket(8080)
                Log.i("RemoteSvc", "TCP server listening on 8080")
                while (!serverSocket.isClosed) {
                    val socket = serverSocket.accept()
                    launch {
                        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                        val cmd = reader.readLine()
                        Log.i("RemoteSvc", "cmd=$cmd")
                        if (cmd != null) handleCommand(cmd.trim())
                        socket.close()
                    }
                }
            } catch (e: Exception) {
                Log.e("RemoteSvc", "server error", e)
            }
        }
    }

    private fun handleCommand(cmd: String) {
        val svc = ScrollAccessibilityService.currentInstance
        if (svc == null) {
            Log.w("RemoteSvc", "AccessibilityService not connected")
            return
        }

        when (cmd.lowercase()) {
            "scroll up" -> svc.performScroll(ScrollAccessibilityService.Direction.UP)
            "scroll down" -> svc.performScroll(ScrollAccessibilityService.Direction.DOWN)
            else -> Log.w("RemoteSvc", "unknown cmd: $cmd")
        }
    }
}
