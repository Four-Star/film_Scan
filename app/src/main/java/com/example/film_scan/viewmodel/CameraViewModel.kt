package com.example.film_scan

import android.content.Context
import android.hardware.camera2.*
import android.view.TextureView
import androidx.lifecycle.ViewModel


// ViewModel 레이어
class CameraViewModel : ViewModel() {
    private var cameraRepository: CameraRepository = CameraRepositoryImpl()

    fun initCamera(context: Context, textureView: TextureView) {
        cameraRepository.startCamera(context, textureView)
    }

    fun captureRAW() {
        cameraRepository.captureRAW()
    }

    fun isCameraReady(): Boolean {
        return cameraRepository.isCameraReady()
    }
}