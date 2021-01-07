package app.android.werdna.menari.activities

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.View
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.android.werdna.menari.PoseGraphic
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import app.android.werdna.menari.R

typealias HaiyaListener = (pose: Pose?, imageProxy: ImageProxy?, hello: Boolean) -> Unit

class MainActivity : AppCompatActivity() {

    private class HaiyaAnalyzer(
        private val poseDetector: PoseDetector,
        private val listener: HaiyaListener
    ) : ImageAnalysis.Analyzer {
        private var lastAnalyzedTime = 0L

        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val currentTime = System.currentTimeMillis()
            val mediaImage = imageProxy.image
            if (currentTime - lastAnalyzedTime >= TimeUnit.SECONDS.toMillis(1) && mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                val result = poseDetector.process(image)
                    .addOnSuccessListener { pose ->
                        listener(pose, imageProxy, true)
                        imageProxy.close()
                    }
                    .addOnFailureListener { e ->
                        listener(null, null, true)
                        imageProxy.close()
                    }

                lastAnalyzedTime = currentTime
            } else if (mediaImage != null)  {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                val result = poseDetector.process(image)
                    .addOnSuccessListener { pose ->
                        listener(pose, imageProxy, false)
                        imageProxy.close()
                    }
                    .addOnFailureListener { e ->
                        listener(null, null, false)
                        imageProxy.close()
                    }
            }
             else {
                imageProxy.close()
            }
        }
    }

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var poseDetector: PoseDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()


        if (graphics_overlay == null) {
            Toast.makeText(this, "HENGGGGG", Toast.LENGTH_SHORT).show()
            return;
        }

        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()

        poseDetector = PoseDetection.getClient(options)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
            video.setVideoURI(Uri.parse("android.resource://${packageName}/${R.raw.amt}"))
            video.start()
            video.setOnCompletionListener {
                finish()
            }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private val dice = listOf(1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3)
    private var score = 0

    private fun show() {
        val drawable = dice[(1 until dice.size).random() - 1]
        score += if (drawable == 1) 100 else if (drawable == 2) 50 else 0
        scoreView.text = score.toString()

        label.apply {
            setImageDrawable(resources.getDrawable(if (drawable == 1) R.drawable.perfect else if (drawable == 2) R.drawable.good else R.drawable.bad))
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .setDuration(400.toLong())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        hide()
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }

                })
        }
    }

    private fun hide() {
        label.apply {
            alpha = 1f
            visibility = View.VISIBLE

            animate()
                .alpha(0f)
                .setDuration(200.toLong())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        visibility = View.GONE
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                    }

                })
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Grant the permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getScreenSize(): Size {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return Size(viewFinder.width, viewFinder.width)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val screenSize = getScreenSize()
            val imageAnalyzer = ImageAnalysis.Builder()
                .setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(graphics_overlay.width, graphics_overlay.height))
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        HaiyaAnalyzer(poseDetector) { pose, imageProxy, hello ->
//                            if (imageProxy != null) {
//                                mamak.setImageBitmap(imageProxy.image?.toBitmap())
//                            }
                            if (hello) show()
                            graphics_overlay.clear()
                            if (pose != null) {
                                graphics_overlay.add(
                                    PoseGraphic(
                                        graphics_overlay,
                                        pose,
                                        false,
                                        imageProxy!!,
                                        viewFinder.width,
                                        viewFinder.height,
                                        graphics_overlay.height
                                    )
                                )
                            } else {
                                Log.d(TAG, "BO MIA CUI")
                            }
                            graphics_overlay.postInvalidate()
                        })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )


            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}