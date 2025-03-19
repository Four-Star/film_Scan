package com.example.film_scan

import android.content.Context
import android.view.TextureView

interface CameraRepository {
    fun startCamera(context: Context, textureView: TextureView)
    fun captureRAW()
    fun isCameraReady(): Boolean
}
