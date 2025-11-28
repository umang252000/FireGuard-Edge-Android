package com.fireguardedge.app

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class CameraAnalyzer(
    private val onFrameReady: (imageBytes: ByteArray, width: Int, height: Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val TAG = "CameraAnalyzer"

    override fun analyze(image: ImageProxy) {
        // Convert YUV to RGB using a simple method (here we convert to Bitmap then to bytes)
        val bitmap: Bitmap? = image.toBitmap() // helper extension (provided below)
        if (bitmap != null) {
            val resized = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
            val bytes = bitmapToRGBByteArray(resized)
            onFrameReady(bytes, resized.width, resized.height)
            resized.recycle()
        }
        image.close()
    }

    private fun bitmapToRGBByteArray(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val intValues = IntArray(width * height)
        bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
        val bytes = ByteArray(width * height * 3)
        var offset = 0
        for (i in intValues.indices) {
            val v = intValues[i]
            bytes[offset++] = ((v shr 16) and 0xFF).toByte() // R
            bytes[offset++] = ((v shr 8) and 0xFF).toByte()  // G
            bytes[offset++] = (v and 0xFF).toByte()         // B
        }
        return bytes
    }
}