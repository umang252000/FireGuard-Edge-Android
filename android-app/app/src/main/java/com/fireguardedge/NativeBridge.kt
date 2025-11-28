package com.fireguardedge.app

object NativeBridge {
    init {
        System.loadLibrary("fireguard_native") // name from CMake target
    }

    external fun nativeInitModels(): Boolean
    external fun nativeRunVision(imageData: ByteArray, width: Int, height: Int): FloatArray
    external fun nativeRunAudio(audioData: FloatArray, length: Int): FloatArray
}