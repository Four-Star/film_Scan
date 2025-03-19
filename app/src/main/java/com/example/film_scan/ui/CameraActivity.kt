package com.example.film_scan.ui

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.example.film_scan.CameraViewModel
import com.example.film_scan.R

class CameraActivity : AppCompatActivity() {
    private val viewModel: CameraViewModel by viewModels()
    private lateinit var textureView: TextureView
    private lateinit var captureButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        textureView = findViewById(R.id.textureView)
        captureButton = findViewById(R.id.captureButton)

        // TextureView가 준비될 때까지 대기
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                viewModel.initCamera(this@CameraActivity, textureView)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        captureButton.setOnClickListener {
            viewModel.captureRAW()
        }
    }
}


