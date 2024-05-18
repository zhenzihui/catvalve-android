package com.feibao.catvalve
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feibao.catvalve.databinding.ActivityValveBinding
import com.feibao.catvalve.util.AnalyzerUtil
import com.feibao.catvalve.util.CameraUtil
import com.feibao.catvalve.util.DeviceUtil
import com.feibao.catvalve.util.INDEX_CAT
import com.feibao.catvalve.util.print

class CameraValveActivity : AppCompatActivity() {
    var _binding: ActivityValveBinding? = null
    val binding get() = _binding!!


    var _ia: ImageAnalysis? = null
    val imageAnalysis get() = _ia!!

    var _cs :CameraUtil? = null
    val cameraUtil get() = _cs!!


    val vm: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        _binding = ActivityValveBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        _ia = AnalyzerUtil(this).initAnalysis { labels ->
            // open valve
            if(labels.any { e -> e.index== INDEX_CAT && e.confidence>0.9 }) {
                "detected cat".print()
                DeviceUtil.openValve()
            }
        }

        _cs = CameraUtil(this, binding.cameraPreview, imageAnalysis)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 11)
        } else {
            cameraUtil.startCamera(vm.currentCameraSelector.value!!)
        }

        vm.currentCameraSelector.observe(this) {
            cameraUtil.startCamera(it)
        }

        binding.switchCameraButton.setOnClickListener {
            if(vm.currentCameraSelector.value==CameraSelector.DEFAULT_FRONT_CAMERA) {
                vm.currentCameraSelector.value = CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                vm.currentCameraSelector.value = CameraSelector.DEFAULT_FRONT_CAMERA
            }
        }





    }

//
//
//    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
//    fun initAnalysis(detectRes: (List<ImageLabel>) -> Unit) {
//        _ia = ImageAnalysis.Builder()
//            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//            .setTargetResolution(Size(1280, 720))
//            .build()
//            .apply {
//                setAnalyzer(executor) {
//                    it.image?.findLabels { labels ->
//                        detectRes.invoke(labels)
//                    }
//                    it.close()
//                }
//            }
//    }
//
//    fun startCamera() {
//        initAnalysis {
//            Log.d("label finder", it.map { a -> a.text }.joinToString(","))
//        }
//        val cameraFuture = ProcessCameraProvider.getInstance(this)
//        cameraFuture.addListener(
//            {
//                val provider = cameraFuture.get()
//                val preview = Preview.Builder()
//                    .build()
//                    .apply {
//                        setSurfaceProvider(binding.cameraPreview.surfaceProvider)
//                    }
//
//                runCatching {
//                    provider.unbindAll()
//                    provider.bindToLifecycle(
//                        this,
//                        CameraSelector.DEFAULT_BACK_CAMERA,
//                        imageAnalysis,
//                        preview
//                    )
//                }
//            }, executor
//        )
//
//
//    }
}

class MainActivityViewModel: ViewModel() {
    val currentCameraSelector = MutableLiveData<CameraSelector>().apply { value = CameraSelector.DEFAULT_FRONT_CAMERA }
}