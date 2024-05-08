package com.feibao.catvalve.util

import android.content.Context
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.label.ImageLabel

const val INDEX_CAT=118

class AnalyzerUtil(private val context: Context) {

    private var _ia: ImageAnalysis? = null

    private val executor get() = ContextCompat.getMainExecutor(context)

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun initAnalysis(detectRes: (List<ImageLabel>) -> Unit): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(Size(1280, 720))
            .build()
            .apply {
                setAnalyzer(executor) {
                    it.image?.findLabels(onComplete = {it.close()}) { labels ->
                        detectRes.invoke(labels)
                    }
                }
            }
    }
}

class CameraUtil(
    private val context: AppCompatActivity,
    private val view: PreviewView,
    private val analysis: ImageAnalysis
) {

    // start to select camera
    fun startCamera(selector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA) {
        val cameraFuture = ProcessCameraProvider.getInstance(context)
        cameraFuture.addListener(
            {
                val provider = cameraFuture.get()
                val preview = Preview.Builder()
                    .build()
                    .apply {
                        setSurfaceProvider(view.surfaceProvider)
                    }

                runCatching {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        context,
                        selector,
                        analysis,
                        preview
                    )
                }
            }, ContextCompat.getMainExecutor(context)
        )
    }
}