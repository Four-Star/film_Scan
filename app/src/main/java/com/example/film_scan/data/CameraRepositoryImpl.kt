package com.example.film_scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat


// CameraRepositoryImpl - Camera2 API를 활용한 구현
class CameraRepositoryImpl : CameraRepository {
    private var cameraDevice: CameraDevice? = null
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var imageReader: ImageReader
    private val handlerThread = HandlerThread("CameraThread").apply { start() }
    private val backgroundHandler = Handler(handlerThread.looper)

    override fun startCamera(context: Context, textureView: TextureView) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera

                // ✅ imageReader 초기화 확인
                if (!::imageReader.isInitialized) {
                    imageReader = ImageReader.newInstance(4000, 3000, ImageFormat.RAW_SENSOR, 2)
                }

                if (textureView.isAvailable) {
                    createCameraPreview(textureView)
                } else {
                    textureView.surfaceTextureListener =
                        object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(
                                surface: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {
                                createCameraPreview(textureView)
                            }

                            override fun onSurfaceTextureSizeChanged(
                                surface: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {
                            }

                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean =
                                false

                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                        }
                }
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
            }
        }, backgroundHandler)
    }


    private fun createCameraPreview(textureView: TextureView) {
        val surfaceTexture = textureView.surfaceTexture ?: run {
            Log.e("CameraRepositoryImpl", "createCameraPreview: surfaceTexture is null!")
            return
        }

        val surface = Surface(surfaceTexture)

        // ✅ imageReader 초기화 추가
        if (!::imageReader.isInitialized) {
            imageReader = ImageReader.newInstance(4000, 3000, ImageFormat.RAW_SENSOR, 2)
        }

        captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)  // ✅ TextureView의 Surface 추가

        // ✅ 카메라 세션 생성 시 TextureView의 Surface와 imageReader의 Surface 추가
        cameraDevice!!.createCaptureSession(
            listOf(surface, imageReader.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (cameraDevice == null) return  // ✅ 카메라가 닫혔으면 실행하지 않음

                    cameraCaptureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)
                    } catch (e: CameraAccessException) {
                        Log.e("CameraRepositoryImpl", "CameraAccessException: ${e.message}")
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e("CameraRepositoryImpl", "Camera preview configuration failed!")
                }
            }, backgroundHandler
        )
    }



    override fun captureRAW() {
        if (!::imageReader.isInitialized) {
            Log.e("CameraRepositoryImpl", "captureRAW: imageReader is not initialized!")
            return
        }

        if (cameraDevice == null) {
            Log.e("CameraRepositoryImpl", "captureRAW: cameraDevice is null")
            return
        }

        val rawCaptureRequest = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)

        if (imageReader.surface == null) {
            Log.e("CameraRepositoryImpl", "captureRAW: imageReader surface is null!")
            return
        }

        rawCaptureRequest.addTarget(imageReader.surface)

        try {
            cameraCaptureSession.capture(rawCaptureRequest.build(), null, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e("CameraRepositoryImpl", "CameraAccessException: ${e.message}")
        }
    }


    override fun isCameraReady(): Boolean {
        return cameraDevice != null && ::imageReader.isInitialized
    }

}