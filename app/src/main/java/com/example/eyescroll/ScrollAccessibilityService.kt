package com.example.eyescroll

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * AccessibilityService that performs vertical scroll gestures in any app.
 * Call `ScrollAccessibilityService.currentInstance?.performScroll(Direction.UP)`
 */
class ScrollAccessibilityService : AccessibilityService() {

    companion object {
        var currentInstance: ScrollAccessibilityService? = null
    }

    enum class Direction { UP, DOWN }

    override fun onServiceConnected() {
        super.onServiceConnected()
        currentInstance = this
        Log.i("ScrollSvc", "connected")
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (currentInstance == this) currentInstance = null
    }

    fun performScroll(direction: Direction) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val startX = (width * 0.5f)
        val startY = if (direction == Direction.UP) (height * 0.7f) else (height * 0.3f)
        val endY = if (direction == Direction.UP) (height * 0.3f) else (height * 0.7f)

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(startX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                // completed
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
            }
        }, null)
    }
}
