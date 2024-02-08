package se.ndssoft.mycamx

import android.content.ContentValues
import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.camera.camera2.internal.compat.workaround.TargetAspectRatio
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier.ImageClassifierOptions
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.view.TextureRegistry
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** MycamxPlugin */
class MycamxPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, EventChannel.StreamHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var mContext: Context? = null
    private var imageCapture: ImageCapture? = null
    private var activity: ActivityPluginBinding? = null
    private var flutter: FlutterPlugin.FlutterPluginBinding? = null

    private var textureRegistry: TextureRegistry? = null
    private var textureEntry: TextureRegistry.SurfaceTextureEntry? = null
    private var textureId: Long? = 0
    private var event: EventChannel? = null
    private var sink: EventChannel.EventSink? = null
    // private var photoName = "No dog in sight!"
    private var lastMap = mapOf(
        "left" to 0f,
        "top" to 0f,
        "right" to 0f,
        "bottom" to 0f,
        "result" to ""
    )

    // private var byteArraySave: ByteArray? = null // For takePicture2
    private lateinit var backgroundExecutor: ExecutorService


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.flutter = flutterPluginBinding
        mContext = flutterPluginBinding.getApplicationContext()
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "mycamx")
        channel.setMethodCallHandler(this)
        textureRegistry = flutterPluginBinding.textureRegistry
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding
        event = EventChannel(flutter!!.binaryMessenger, "dogChannel")
        event!!.setStreamHandler(this)
    }

    override fun onDetachedFromActivity() {
        this.flutter = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.sink = events
    }

    override fun onCancel(arguments: Any?) {
        sink = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
            if (call.method == "startCamera") {
                //  Log.d(TAG, "***************** will now call startCamera")
                val textureId = startCamera()
                result.success(textureId)
            } else if (call.method == "takePhoto") {
                val photoName = takePhoto()
                result.success(photoName)
                /*     } else if (call.method == "takePhoto2") {
                           with(result) { success(byteArraySave) } */
            } else {
                result.notImplemented()
            }
        }


    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    private fun startCamera(): Long? {
        //  Log.d(TAG, "*****************startCamera called")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(mContext!!)
        cameraProviderFuture.addListener({
         //   backgroundExecutor = Executors.newSingleThreadExecutor()
         //   backgroundExecutor.execute {
         //   val executor = ContextCompat.getMainExecutor(mContext!!)

         //   val executor = Executors.newSingleThreadExecutor()

            // preview
            textureEntry = textureRegistry!!.createSurfaceTexture()
            textureId = textureEntry!!.id()
            val surfaceProvider = Preview.SurfaceProvider { request ->


                val resolution = request.resolution
                Log.d(TAG, "*****************resolution ${resolution.width} ${resolution.height}")
                // resolution 1440 1080
                val texture = textureEntry!!.surfaceTexture()
                texture.setDefaultBufferSize(resolution.width, resolution.height)
                val surface = Surface(texture)
                val executor = ContextCompat.getMainExecutor(mContext!!)
                request.provideSurface(surface, executor) { }
                // request.provideSurface(surface, backgroundExecutor) { }
                Log.d(TAG, "request.provideSurface done")



            }
            val preview = Preview.Builder().build().apply { setSurfaceProvider(surfaceProvider) }

            // imageAnalysis
            val baseOptions = BaseOptions.builder()
                .setDelegate(Delegate.CPU)
                .setModelAssetPath("assets\\efficientdet_lite0.tflite")
                .build()
            val allowList = listOf("dog", "cat", "bear", "teddy bear")
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setCategoryAllowlist(allowList)
                .setBaseOptions(baseOptions)
                .setScoreThreshold(0.5f)
                .setRunningMode(RunningMode.IMAGE)
                .setMaxResults(5)
                .build()
            val objectDetector = ObjectDetector.createFromOptions(mContext, options)

            val baseOptions2 = BaseOptions.builder()
                .setDelegate(Delegate.CPU)
                .setModelAssetPath("assets\\dogs_metadata.tflite")
                //   .setModelAssetPath("assets\\efficientnet_lite0.tflite") //TODO
                .build()
            val options2 = ImageClassifierOptions.builder()
                .setBaseOptions(baseOptions2)
                .setScoreThreshold(0.10f)
                .setRunningMode(RunningMode.IMAGE)
                .setMaxResults(2)
                .build()
            val imageClassifier = ImageClassifier.createFromOptions(mContext, options2)
            val backgroundExecutor = Executors.newSingleThreadExecutor()

            val analyzer = ImageAnalysis.Analyzer { imageProxy ->
                // val map: Map<String, Any>
                val map: Map<String, Any> = se.ndssoft.mycamx.analyzeProxy(
                    imageProxy,
                    objectDetector,
                    imageClassifier,
                )
                if ((map["result"] != lastMap["result"]) || (map["left"] != lastMap["left"])) {
                    lastMap = map.toMap()
                    activity!!.activity.runOnUiThread{
                         sink?.success(map)
                    }
                }
                imageProxy.close()
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setOutputImageRotationEnabled(true)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
              //.setTargetAspectRatio(AspectRatio.RATIO_16_9)
              //.setTargetResolution(Size(1280, 720))
                .build()
            // imageAnalysis.setAnalyzer(executor, analyzer)
            // backgroundExecutor = Executors.newSingleThreadExecutor()
            imageAnalysis.setAnalyzer(backgroundExecutor, analyzer)
            Log.d(TAG, "imageAnalysis.setAnalyzer done")

            // imageCapture
            imageCapture = ImageCapture.Builder().build()

            // Bind use cases to camera
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            try {
                val owner = activity!!.activity as LifecycleOwner
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    owner, cameraSelector, preview, imageCapture, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(mContext!!))
        return textureId
    }


    private fun takePhoto() : String {
        // Log.d(TAG, "takePhoto called")

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return "No photo"

        // Create MediaStore entry.
        val photoName = (lastMap["result"]).toString().split("\n")[0]
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, photoName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (VERSION.SDK_INT > VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                mContext!!.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(mContext!!),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                 //   Log.d(TAG, output.toString())
                 /*   val msg = "Saved photo ${photoName}"
                    Toast.makeText(mContext!!, msg, Toast.LENGTH_SHORT).show() */
                    // Log.d(TAG, msg)
                }
            }
        )
        return photoName
    }

    companion object {
        private const val TAG = "MycamX"
    }
}
