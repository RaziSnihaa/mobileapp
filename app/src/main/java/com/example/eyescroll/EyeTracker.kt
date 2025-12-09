package com.example.eyescroll

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * EyeTracker: lightweight implementation using ML Kit Face Detection to estimate vertical gaze.
 * - Calls `onGaze(direction)` with "UP" | "DOWN" | "CENTER".
 * - For higher accuracy, replace with MediaPipe Face Mesh integration (instructions in README).
 */
class EyeTracker(private val ctx: Context, private val onGaze: (String) -> Unit) {

    private var cameraProviderJob: Job? = null
    private var analysis: ImageAnalysis? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }

    fun start() {
        cameraProviderJob = scope.launch {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val cameraProvider = cameraProviderFuture.get()

            analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis?.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                processImage(imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                cameraProvider.bindToLifecycle(/* lifecycleOwner= */ ctx as androidx.lifecycle.LifecycleOwner, cameraSelector, analysis)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        try {
            analysis?.clearAnalyzer()
        } catch (e: Exception) {
            // ignore
        }
        cameraProviderJob?.cancel()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    handleFaces(faces)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun handleFaces(faces: List<Face>) {
        if (faces.isEmpty()) return
        val face = faces[0]

        // Simple vertical gaze estimation heuristic:
        // Compare eye-center vertical position relative to face bounding box center.
        val bbox = face.boundingBox
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)

        if (leftEye != null && rightEye != null) {
            val eyesCenterY = (leftEye.position.y + rightEye.position.y) / 2f
            val faceCenterY = bbox.centerY().toFloat()
            val delta = eyesCenterY - faceCenterY

            val sensitivity = SensitivitySettings.getSensitivity(ctx) / 100.0f // 0..1
            val threshold = 15f * (1.0f - sensitivity) + 5f // adaptive

            if (delta < -threshold) onGaze("UP")
            else if (delta > threshold) onGaze("DOWN")
            else onGaze("CENTER")
        }
    }
}
